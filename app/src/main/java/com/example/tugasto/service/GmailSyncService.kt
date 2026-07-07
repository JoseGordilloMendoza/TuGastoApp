package com.example.tugasto.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.tugasto.data.local.entity.TransactionEntity
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GmailSyncService(private val context: Context) {

    companion object {
        const val YAPE_SENDER = "notificaciones@yape.pe"
        const val GMAIL_SCOPE = "https://www.googleapis.com/auth/gmail.readonly"
        private const val MIN_AMOUNT = 10.0
        private const val GMAIL_API = "https://gmail.googleapis.com/gmail/v1"
        private const val TAG = "GmailSyncService"
        private const val PREFS_SYNC = "gmail_sync"
        private const val KEY_PROCESSED_IDS = "processed_ids"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
            GoogleAuthUtil.getToken(context, account.account!!, "oauth2:$GMAIL_SCOPE")
        } catch (e: Exception) {
            Log.e(TAG, "Token error: ${e.message}")
            null
        }
    }

    suspend fun syncYapeEmails(): List<TransactionEntity> {
        val token = getAccessToken() ?: return emptyList()

        return withContext(Dispatchers.IO) {
            try {
                val query = "from:$YAPE_SENDER newer_than:1d"
                val url = "$GMAIL_API/users/me/messages?q=${Uri.encode(query)}&maxResults=20"

                val listResp = httpClient.newCall(
                    Request.Builder().url(url)
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                ).execute().use { it.body?.string() ?: return@withContext emptyList() }

                val messageArray = JSONObject(listResp).optJSONArray("messages")
                    ?: return@withContext emptyList()

                val processedIds = getProcessedIds()
                val result = mutableListOf<TransactionEntity>()

                for (i in 0 until messageArray.length()) {
                    val msgId = messageArray.getJSONObject(i).getString("id")
                    if (msgId in processedIds) continue

                    val msgResp = httpClient.newCall(
                        Request.Builder()
                            .url("$GMAIL_API/users/me/messages/$msgId?format=full")
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                    ).execute().use { it.body?.string() }

                    if (msgResp == null) continue

                    val msgJson = JSONObject(msgResp)
                    val body = extractEmailBody(msgJson)
                    val timestamp = msgJson.optLong("internalDate", System.currentTimeMillis())

                    parseYapeEmail(body, timestamp)?.let {
                        result.add(it)
                        markAsProcessed(msgId)
                    }
                }

                result
            } catch (e: Exception) {
                Log.e(TAG, "Sync error: ${e.message}")
                emptyList()
            }
        }
    }

    private fun extractEmailBody(msgJson: JSONObject): String {
        val payload = msgJson.optJSONObject("payload")
            ?: return msgJson.optString("snippet", "")

        // Non-multipart email
        val directData = payload.optJSONObject("body")?.optString("data", "")
        if (!directData.isNullOrBlank()) return decodeBase64(directData)

        // Multipart: prefer text/plain, fallback text/html
        val parts = payload.optJSONArray("parts")
            ?: return msgJson.optString("snippet", "")

        var htmlFallback = ""
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            val data = part.optJSONObject("body")?.optString("data", "") ?: continue
            if (data.isBlank()) continue
            when (part.optString("mimeType")) {
                "text/plain" -> return decodeBase64(data)
                "text/html"  -> htmlFallback = decodeBase64(data)
            }
        }

        return htmlFallback.ifBlank { msgJson.optString("snippet", "") }
    }

    private fun decodeBase64(data: String): String = try {
        val bytes = android.util.Base64.decode(
            data.replace('-', '+').replace('_', '/'),
            android.util.Base64.DEFAULT
        )
        String(bytes, Charsets.UTF_8)
    } catch (e: Exception) { data }

    private fun parseYapeEmail(body: String, timestamp: Long): TransactionEntity? {
        val isSent = body.contains("yapear exitosamente", ignoreCase = true)
        val isReceived = body.contains("te han yapeado", ignoreCase = true)
            || body.contains("te yapeo", ignoreCase = true)

        if (!isSent && !isReceived) return null

        // Formato real: "S/\t15.75" — \s+ cubre tab y espacios
        val amount = Regex("""S/\s+([\d,\.]+)""")
            .find(body)?.groupValues?.get(1)
            ?.replace(",", ".")
            ?.toDoubleOrNull() ?: return null

        if (amount < MIN_AMOUNT) return null

        val name = if (isSent) {
            Regex("""Nombre del Beneficiario\s+(.+)""")
                .find(body)?.groupValues?.get(1)?.trim() ?: "Desconocido"
        } else {
            Regex("""Yapero\s+(.+)""")
                .find(body)?.groupValues?.get(1)?.trim() ?: "Desconocido"
        }

        return TransactionEntity(
            amount = amount,
            description = "${if (isSent) "Yape a" else "Yape recibido de"} $name",
            categoryId = 1,
            timestamp = timestamp,
            type = if (isSent) "EXPENSE" else "INCOME",
            isConfirmed = false
        )
    }

    private fun getProcessedIds(): Set<String> {
        return context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
            .getStringSet(KEY_PROCESSED_IDS, emptySet()) ?: emptySet()
    }

    private fun markAsProcessed(id: String) {
        val prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_PROCESSED_IDS, emptySet())?.toMutableSet()
            ?: mutableSetOf()
        current.add(id)
        // Acota a 500 IDs para no crecer indefinidamente
        val toSave = if (current.size > 500) mutableSetOf(id) else current
        prefs.edit().putStringSet(KEY_PROCESSED_IDS, toSave).apply()
    }

    fun disconnect() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(GMAIL_SCOPE))
            .build()
        GoogleSignIn.getClient(context, gso).signOut()
        context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
