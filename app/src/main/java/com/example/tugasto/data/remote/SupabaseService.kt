package com.example.tugasto.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.tugasto.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseService @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl get() = BuildConfig.SUPABASE_URL
    private val anonKey get() = BuildConfig.SUPABASE_ANON_KEY

    val accessToken: String? get() = prefs.getString("access_token", null)
    val userId: String? get() = prefs.getString("user_id", null)
    val userEmail: String? get() = prefs.getString("user_email", null)
    val isSignedIn: Boolean get() = accessToken != null

    suspend fun signIn(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/token?grant_type=password")
                .addHeader("apikey", anonKey)
                .post(body)
                .build()

            val responseBody = client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string() ?: ""
                if (!resp.isSuccessful) {
                    val msg = runCatching { JSONObject(raw) }.getOrNull()
                        ?.run { optString("error_description").ifBlank { optString("message") } }
                        ?: "Error ${resp.code}"
                    error(msg)
                }
                raw
            }

            val json = JSONObject(responseBody)
            prefs.edit()
                .putString("access_token", json.getString("access_token"))
                .putString("user_id", json.getJSONObject("user").getString("id"))
                .putString("user_email", email)
                .apply()
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/signup")
                .addHeader("apikey", anonKey)
                .post(body)
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val raw = resp.body?.string() ?: ""
                    val msg = runCatching { JSONObject(raw) }.getOrNull()
                        ?.run { optString("error_description").ifBlank { optString("message") } }
                        ?: "Error ${resp.code}"
                    error(msg)
                }
            }
        }
    }

    fun signOut() {
        prefs.edit().clear().apply()
    }

    suspend fun deleteUserTransactions(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = accessToken ?: error("No hay sesión activa")
            val uid = userId ?: error("No hay sesión activa")

            val request = Request.Builder()
                .url("$baseUrl/rest/v1/transactions_backup?user_id=eq.$uid")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $token")
                .delete()
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) error("Error al limpiar backup (${resp.code})")
            }
        }
    }

    suspend fun insertTransactions(rows: JSONArray): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = accessToken ?: error("No hay sesión activa")

            val request = Request.Builder()
                .url("$baseUrl/rest/v1/transactions_backup")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Prefer", "return=minimal")
                .post(rows.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) error("Error al subir datos (${resp.code})")
            }
        }
    }

    suspend fun fetchUserTransactions(): Result<JSONArray> = withContext(Dispatchers.IO) {
        runCatching {
            val token = accessToken ?: error("No hay sesión activa")
            val uid = userId ?: error("No hay sesión activa")

            val request = Request.Builder()
                .url("$baseUrl/rest/v1/transactions_backup?user_id=eq.$uid&select=*")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string() ?: "[]"
                if (!resp.isSuccessful) error("Error al obtener datos (${resp.code})")
                JSONArray(raw)
            }
        }
    }

    // ── Perfil de usuario y estado Pro ────────────────────────────────────────

    suspend fun ensureUserProfile(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = accessToken ?: error("No hay sesión activa")
            val uid = userId ?: error("No hay sesión activa")

            val body = JSONObject().apply {
                put("id", uid)
                put("email", userEmail ?: "")
                put("is_pro", false)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/rest/v1/user_profiles")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Prefer", "resolution=ignore-duplicates,return=minimal")
                .post(body)
                .build()

            client.newCall(request).execute().use { resp ->
                // 201 = created, 200 = ignored duplicate — ambos son OK
                if (!resp.isSuccessful) error("Error al crear perfil (${resp.code})")
            }
        }
    }

    suspend fun fetchProStatus(): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val token = accessToken ?: error("No hay sesión activa")
            val uid = userId ?: error("No hay sesión activa")

            val request = Request.Builder()
                .url("$baseUrl/rest/v1/user_profiles?id=eq.$uid&select=is_pro")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val raw = client.newCall(request).execute().use { resp ->
                val body = resp.body?.string() ?: "[]"
                if (!resp.isSuccessful) error("Error al verificar estado Pro (${resp.code})")
                body
            }

            val arr = JSONArray(raw)
            if (arr.length() == 0) false
            else arr.getJSONObject(0).optBoolean("is_pro", false)
        }
    }
}
