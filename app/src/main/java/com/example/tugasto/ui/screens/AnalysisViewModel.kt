package com.example.tugasto.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.BuildConfig
import com.example.tugasto.data.local.dao.TuGastoDao
import com.example.tugasto.data.local.entity.TransactionEntity
import com.example.tugasto.data.local.prefs.UserPreferencesRepository
import com.example.tugasto.service.GmailSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.example.tugasto.data.local.entity.CategoryEntity
import com.example.tugasto.data.local.entity.CategorySum

data class AiReport(
    val alert: String?,
    val suggestion: String?,
    val congratulation: String?
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val dao: TuGastoDao,
    private val prefsRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult

    fun forceSyncGmail() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncResult.value = null
            try {
                val service = GmailSyncService(context)
                val transactions = service.syncYapeEmails()
                withContext(Dispatchers.IO) {
                    transactions.forEach { dao.insertTransaction(it) }
                }
                _syncResult.value = if (transactions.isEmpty())
                    "Sin correos nuevos de Yape"
                else
                    "${transactions.size} Yape(s) detectado(s)"
            } catch (e: Exception) {
                _syncResult.value = "Error: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun clearSyncResult() { _syncResult.value = null }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _aiReport = MutableStateFlow<AiReport?>(null)
    val aiReport: StateFlow<AiReport?> = _aiReport

    val categorySums: StateFlow<List<CategorySum>> = dao.getAmountByCategory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val vsLastMonthPercent: StateFlow<Double?> = run {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val thisMonthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, -1)
        val lastMonthStart = cal.timeInMillis

        combine(
            dao.getTotalAmountFrom(thisMonthStart),
            dao.getTotalAmountBetween(lastMonthStart, thisMonthStart)
        ) { current, previous ->
            val c = current ?: 0.0
            val p = previous ?: 0.0
            if (p > 0.0) ((c - p) / p) * 100.0 else null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    val pendingTransactions: StateFlow<List<TransactionEntity>> = dao.getPendingTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<CategoryEntity>> = dao.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Refleja si el usuario tiene Gmail conectado para auto-detección de Yapes
    val isAutoDetectionEnabled: StateFlow<Boolean> = prefsRepository.isAutoDetectionEnabled

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAllCategories()
            dao.insertCategories(defaultCategories())
        }
        viewModelScope.launch {
            categorySums.collect { sums ->
                if (sums.isNotEmpty() && _aiReport.value == null) {
                    generateAiTip(sums)
                }
            }
        }
    }

    private fun defaultCategories() = listOf(
        CategoryEntity(id = 1,  name = "Alimentación",    iconName = "local_dining",       colorHex = "#2563EB"),
        CategoryEntity(id = 2,  name = "Transporte",      iconName = "directions_bus",      colorHex = "#F97316"),
        CategoryEntity(id = 3,  name = "Servicios",       iconName = "electric_bolt",       colorHex = "#22C55E"),
        CategoryEntity(id = 4,  name = "Entretenimiento", iconName = "confirmation_number", colorHex = "#8B5CF6"),
        CategoryEntity(id = 5,  name = "Salud",           iconName = "health_and_safety",   colorHex = "#EF4444"),
        CategoryEntity(id = 6,  name = "Educación",       iconName = "school",              colorHex = "#0EA5E9"),
        CategoryEntity(id = 7,  name = "Ropa y Calzado",  iconName = "shopping_bag",        colorHex = "#EC4899"),
        CategoryEntity(id = 8,  name = "Hogar",           iconName = "home",                colorHex = "#84CC16"),
        CategoryEntity(id = 9,  name = "Trabajo",         iconName = "work",                colorHex = "#F59E0B"),
        CategoryEntity(id = 10, name = "Otros",           iconName = "category",            colorHex = "#6B7280"),
    )

    fun confirmTransaction(transactionId: Int, categoryId: Int, newDescription: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.confirmTransaction(transactionId, categoryId, newDescription)
        }
    }

    fun dismissTransaction(transactionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteTransaction(transactionId)
        }
    }

    fun simulateDetection() {
        viewModelScope.launch(Dispatchers.IO) {
            val mockDescription = listOf("Uber Eats", "Netflix", "Spotify", "Cineplanet", "Bodega Don Pepe").random()
            val mockAmount = (15..80).random().toDouble()
            // Assume category 1 is "Otros" or we just default it, but let's guess based on description for the mockup
            val guessedCategoryId = when (mockDescription) {
                "Uber Eats" -> 1 // Alimentacion
                "Netflix", "Spotify", "Cineplanet" -> 4 // Entretenimiento
                else -> 1
            }
            dao.insertTransaction(
                TransactionEntity(
                    amount = mockAmount,
                    description = mockDescription,
                    categoryId = guessedCategoryId,
                    timestamp = System.currentTimeMillis(),
                    type = "EXPENSE",
                    isConfirmed = false
                )
            )
        }
    }

    // A simple projection logic based on the span of transactions
    val estimatedExpense: StateFlow<Double> = dao.getAllTransactions()
        .map { transactions ->
            if (transactions.isEmpty()) return@map 0.0
            
            val total = transactions.sumOf { it.amount }
            val earliest = transactions.minByOrNull { it.timestamp }?.timestamp ?: System.currentTimeMillis()
            val current = System.currentTimeMillis()
            
            val daysSpan = ((current - earliest) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
            val dailyAverage = total / daysSpan
            
            // Project to a 30-day month
            dailyAverage * 30
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // For the UI, we use the real monthly income to show a balance
    val estimatedBalance: StateFlow<Double> = combine(estimatedExpense, prefsRepository.monthlyIncome) { expense, income ->
        income - expense
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val emergencyFundGoal: StateFlow<Double> = prefsRepository.emergencyFundGoal
    val currentSavings: StateFlow<Double> = prefsRepository.currentSavings
    val isProUser: StateFlow<Boolean> = prefsRepository.isProUser

    private suspend fun generateAiTip(sums: List<CategorySum>) {
        try {
            val total = sums.sumOf { it.totalAmount }
            val categories = sums.map { entry ->
                "${entry.categoryName}: S/ ${entry.totalAmount}"
            }.joinToString(", ")
            
            val prompt = """
                Eres un asesor financiero ejecutivo de alto nivel (Family Office). Un cliente tiene los siguientes gastos registrados en su portafolio este mes:
                Total: S/ $total. 
                Desglose: $categories.
                
                Analiza esto y devuelve ÚNICAMENTE un objeto JSON válido con 3 propiedades:
                1. "alert": Una observación analítica sobre algún patrón de gasto inusual o concentración de riesgo financiero (máximo 20 palabras).
                2. "suggestion": Una recomendación estratégica y profesional para optimizar el flujo de caja (máximo 20 palabras).
                3. "congratulation": Un reconocimiento formal sobre algún aspecto positivo de su gestión financiera (máximo 20 palabras).
                
                Instrucciones críticas:
                - Usa un tono formal, elegante y corporativo.
                - NO uses emojis ni símbolos bajo ninguna circunstancia.
                - Dirígete al usuario con respeto (ej. "Es prudente notar...", "Se sugiere revisar...").
                
                Ejemplo de salida:
                {
                  "alert": "Existe una concentración elevada de capital en la categoría de Alimentación.",
                  "suggestion": "Es prudente reestructurar el presupuesto mensual para optimizar la retención de liquidez.",
                  "congratulation": "Su disciplina en el registro de transacciones refleja una excelente gestión de control."
                }
            """.trimIndent()

            val bodyJson = JSONObject().apply {
                put("model", "llama-3.1-8b-instant")
                put("temperature", 0.3)
                put("response_format", JSONObject().put("type", "json_object"))
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are a helpful assistant that ALWAYS outputs perfectly formatted JSON.")
                    })
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

            val responseJson = JSONObject(responseText)
            val rawContent = responseJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            val resultObj = JSONObject(rawContent)
            _aiReport.value = AiReport(
                alert = resultObj.optString("alert", "Revisa tus categorías con más gastos."),
                suggestion = resultObj.optString("suggestion", "Intenta reducir tus compras impulsivas."),
                congratulation = resultObj.optString("congratulation", "¡Vas por buen camino organizando tu dinero!")
            )

        } catch (e: Exception) {
            _aiReport.value = AiReport(
                alert = "La sincronización de datos para el análisis de cartera se encuentra temporalmente no disponible.",
                suggestion = "Mantenga un control estricto sobre los activos de alta liquidez y gastos recurrentes.",
                congratulation = "El sistema registra su constante dedicación al monitoreo de la plataforma."
            )
        }
    }
}
