package com.example.tugasto.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.BuildConfig
import com.example.tugasto.data.local.dao.TuGastoDao
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        viewModelScope.launch {
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
        _isLoading.value = true
        try {
            val prompt = """
                Eres un asistente de finanzas personales para usuarios peruanos.
                Extrae el monto y categoría de un gasto escrito en texto libre, incluyendo jerga peruana.

                MONEDA (no afectan el número base): "soles", "sol", "S/", "PEN", "pe", "pesos", "plata", "mosca", "pelos".
                JERGA DE DENOMINACIONES PERUANAS:
                - "luca"/"lucas" = 1 sol cada una → "cinco lucas"=5.0, "veinte lucas"=20.0, "una luca"=1.0
                - "china" = 0.50 soles → "una china"=0.50, "dos chinas"=1.0
                - "rojo" = 200 soles (el billete rojo) → "un rojo"=200.0, "medio rojo"=100.0
                - "pelos" = soles → "diez pelos"=10.0, "quince pelos"=15.0
                NÚMEROS ESCRITOS: "un"/"una"=1, "dos"=2, "tres"=3, "cuatro"=4, "cinco"=5, "seis"=6, "siete"=7, "ocho"=8, "nueve"=9, "diez"=10, "once"=11, "doce"=12, "trece"=13, "catorce"=14, "quince"=15, "dieciséis"=16, "veinte"=20, "veinticinco"=25, "treinta"=30, "cuarenta"=40, "cincuenta"=50, "sesenta"=60, "setenta"=70, "ochenta"=80, "noventa"=90, "cien"=100, "ciento"=1XX, "doscientos"=200.
                MONTO: siempre positivo. Si no hay monto claro usa 0.
                NO ES GASTO: "presté", "me devolvieron", "deposité", "retiro del banco", "transferí" → monto 0.

                CATEGORÍAS y JERGA PERUANA:
                "Alimentación": menú, menú del día, almuerzo, desayuno, cena, lonche, recreo, pollería, cevichería, ceviche, anticucho, chicharrón, papa a la huancaína, lomo saltado, pollo a la brasa, causa, sanguche, pan, panadería, bodega, mercado, feria, minimarket, delivery, pedido, rappi, yummy, chifa, chivito, combo, postre, café, jugos, gaseosa, agua, snack, fruta, verdura, choclo, arroz, fideos
                "Transporte": pasaje, combi, micro, mototaxi, moto, taxi, uber, cabify, beat, indriver, colectivo, tren, metro, bus, combustible, gasolina, gasolinera, estacionamiento, peaje, cochera, bicicleta
                "Servicios": luz, agua, gas, balón de gas, internet, cable, teléfono, recarga, saldo, movistar, claro, entel, bitel, alquiler, renta, mantenimiento, arbitrios, predial, seguro, banco, comisión
                "Entretenimiento": cine, netflix, spotify, youtube premium, juego, videojuego, steam, salida, karaoke, discoteca, bar, tragos, cerveza, chela, fiesta, evento, concierto, partido, casino
                "Salud": doctor, médico, clínica, hospital, farmacia, botica, medicina, pastilla, consulta, análisis, laboratorio, óptica, lentes, dentista, psicólogo
                "Educación": colegio, universidad, instituto, curso, libro, fotocopia, impresión, útiles, lapicero, cuaderno, mochila, matrícula, pensión
                "Ropa y Calzado": ropa, polo, camisa, pantalón, jean, zapatilla, zapato, sandalia, zapatillas, casaca, chompa, terno, vestido, cartera, billetera, accesorio
                "Hogar": mercado, supermercado, limpieza, detergente, escoba, mueble, electrodoméstico, foco, pintura, plomero, técnico, arreglo
                "Trabajo": herramienta, materiales, útiles de oficina, impresión, movilidad laboral
                "Otros": regalo, propina, donación, multa, papeleta, tramite, notaría, mascota, veterinario

                EJEMPLOS:
                "menú 12" → {"monto": 12.0, "categoria": "Alimentación"}
                "combi al centro 1.50" → {"monto": 1.50, "categoria": "Transporte"}
                "recarga movistar veinte soles" → {"monto": 20.0, "categoria": "Servicios"}
                "Netflix mensual 37.90" → {"monto": 37.90, "categoria": "Entretenimiento"}
                "botica 45" → {"monto": 45.0, "categoria": "Salud"}
                "pollería con la familia 85" → {"monto": 85.0, "categoria": "Alimentación"}
                "indriver a la oficina 18" → {"monto": 18.0, "categoria": "Transporte"}
                "balón de gas 50" → {"monto": 50.0, "categoria": "Servicios"}
                "zapatillas nuevas ochenta" → {"monto": 80.0, "categoria": "Ropa y Calzado"}
                "le presté a Juan cien soles" → {"monto": 0, "categoria": "Otros"}
                "chelas con amigos 30" → {"monto": 30.0, "categoria": "Entretenimiento"}
                "delivery rappi 25.50" → {"monto": 25.50, "categoria": "Alimentación"}
                "mototaxi 3" → {"monto": 3.0, "categoria": "Transporte"}
                "taxi cinco lucas" → {"monto": 5.0, "categoria": "Transporte"}
                "menú quince pelos" → {"monto": 15.0, "categoria": "Alimentación"}
                "le presté un rojo a mi hermano" → {"monto": 0, "categoria": "Otros"}
                "combi una china" → {"monto": 0.50, "categoria": "Transporte"}
                "compré ropa con veinte lucas" → {"monto": 20.0, "categoria": "Ropa y Calzado"}

                Responde SOLO con JSON válido, sin texto adicional, sin markdown, sin explicaciones.
                Formato exacto: {"monto": NUMBER, "categoria": "STRING"}

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
        } finally {
            _isLoading.value = false
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
