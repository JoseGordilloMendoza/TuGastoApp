package com.example.tugasto.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.BuildConfig
import com.example.tugasto.data.local.dao.TuGastoDao
import com.example.tugasto.data.local.entity.CategoryEntity
import com.example.tugasto.data.local.entity.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val dao: TuGastoDao
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        viewModelScope.launch {
            dao.insertCategories(
                listOf(
                    CategoryEntity(name = "Alimentación", iconName = "restaurant", colorHex = "#FF5722"),
                    CategoryEntity(name = "Transporte", iconName = "directions_bus", colorHex = "#2196F3"),
                    CategoryEntity(name = "Ocio", iconName = "movie", colorHex = "#9C27B0"),
                    CategoryEntity(name = "Hogar", iconName = "home", colorHex = "#4CAF50")
                )
            )
            _messages.value = listOf(
                ChatMessage(
                    text = "¡Hola! Soy tu Asistente TuGasto. Escribe tus gastos aquí.",
                    isUser = false,
                    time = getCurrentTime()
                )
            )
        }
    }

    fun sendMessage(text: String) {
        _messages.value = _messages.value + ChatMessage(
            text = text.trim(),
            isUser = true,
            time = getCurrentTime()
        )
        viewModelScope.launch {
            processExpenseText(text.trim())
        }
    }

    private suspend fun processExpenseText(input: String) {
        try {
            val prompt = """
                Eres un asistente de finanzas personales para usuarios peruanos.
                El usuario escribe sus gastos en texto libre y tú debes extraer la información.

                REGLAS:
                - "soles", "sol", "S/", "PEN" significan la moneda peruana (no afectan el número)
                - El monto es siempre un número positivo
                - Si no hay monto claro, usa 0
                - Categorías válidas: "Alimentación", "Transporte", "Ocio", "Hogar", "Otros"

                EJEMPLOS:
                "comida 8 soles" → {"monto": 8.0, "categoria": "Alimentación"}
                "pasaje 2.50" → {"monto": 2.50, "categoria": "Transporte"}
                "almuerzo S/ 15" → {"monto": 15.0, "categoria": "Alimentación"}
                "luz 120 soles" → {"monto": 120.0, "categoria": "Hogar"}
                "cine 25" → {"monto": 25.0, "categoria": "Ocio"}

                Responde SOLO con el JSON, sin texto adicional, sin markdown.

                Texto del usuario: "$input"
            """.trimIndent()

            val bodyJson = JSONObject().apply {
                put("model", "llama-3.1-8b-instant")
                put("temperature", 0.1)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }.toString()

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            val responseText = withContext(Dispatchers.IO) {
                httpClient.newCall(request).execute().use { it.body?.string() ?: "" }
            }

            Log.d("TuGasto_Groq", "Response: $responseText")

            val responseJson = JSONObject(responseText)
            if (responseJson.has("error")) {
                val errorMsg = responseJson.getJSONObject("error").optString("message", "Error desconocido")
                Log.e("TuGasto_Groq", "API Error: $errorMsg")
                addAssistantMessage("Error de la API: $errorMsg")
                return
            }

            val rawContent = responseJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            Log.d("TuGasto_Groq", "AI text: $rawContent")

            // Extrae el primer bloque JSON aunque el modelo agregue texto extra
            val jsonMatch = Regex("""\{[^{}]*\}""").find(rawContent)
            val aiText = jsonMatch?.value
                ?: throw Exception("No se encontró JSON en la respuesta: $rawContent")

            val parsed = JSONObject(aiText)
            Log.d("TuGasto_Groq", "Parsed JSON: $parsed")
            val amount = parsed.opt("monto")?.toString()?.toDoubleOrNull() ?: 0.0
            val categoryName = parsed.optString("categoria", "Otros")

            if (amount > 0) {
                val categoryId = dao.getCategoryByName(categoryName)?.id ?: 1
                dao.insertTransaction(
                    TransactionEntity(
                        amount = amount,
                        description = input,
                        categoryId = categoryId,
                        timestamp = System.currentTimeMillis(),
                        type = "EXPENSE"
                    )
                )
                addAssistantMessage(
                    "✓ Registrado\n• Detalle: $input\n• Categoría: $categoryName\n• Monto: S/ $amount",
                    categoryName
                )
            } else {
                addAssistantMessage("No pude detectar el monto. ¿Podrías repetirlo? ej: 'Menú 15.50'")
            }

        } catch (e: Exception) {
            Log.e("TuGasto_Groq", "Error: ${e.message}", e)
            addAssistantMessage("Hubo un error procesando el mensaje. Intenta de nuevo.")
        }
    }

    private fun addAssistantMessage(text: String, confirmLabel: String = "") {
        _messages.value = _messages.value + ChatMessage(
            text = text,
            isUser = false,
            time = getCurrentTime(),
            confirmLabel = confirmLabel
        )
    }

    private fun getCurrentTime() =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
}
