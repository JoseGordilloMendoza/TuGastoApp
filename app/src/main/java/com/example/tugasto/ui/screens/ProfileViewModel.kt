package com.example.tugasto.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.data.local.dao.TuGastoDao
import com.example.tugasto.data.local.prefs.UserPreferencesRepository
import com.example.tugasto.data.remote.CloudSyncService
import com.example.tugasto.data.remote.SupabaseService
import com.example.tugasto.service.GmailSyncService
import com.example.tugasto.service.GmailSyncWorker
import com.example.tugasto.service.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// ── Cloud auth state ───────────────────────────────────────────────────────────
sealed class CloudAuthState {
    object NotSignedIn : CloudAuthState()
    data class SignedIn(val email: String) : CloudAuthState()
}

sealed class CloudOpResult {
    object Idle : CloudOpResult()
    data class Success(val message: String) : CloudOpResult()
    data class Error(val message: String) : CloudOpResult()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val dao: TuGastoDao,
    @ApplicationContext private val context: Context,
    private val cloudSyncService: CloudSyncService,
    private val supabaseService: SupabaseService
) : ViewModel() {

    val monthlyIncome: StateFlow<Double> = prefsRepository.monthlyIncome
    val emergencyFundGoal: StateFlow<Double> = prefsRepository.emergencyFundGoal
    val currentSavings: StateFlow<Double> = prefsRepository.currentSavings
    val isProUser: StateFlow<Boolean> = prefsRepository.isProUser
    val isDarkMode: StateFlow<Boolean?> = prefsRepository.isDarkMode

    val connectedGmailEmail: StateFlow<String?> = prefsRepository.connectedGmailEmail
    val isAutoDetectionEnabled: StateFlow<Boolean> = prefsRepository.isAutoDetectionEnabled

    val isPinEnabled: StateFlow<Boolean> = prefsRepository.isPinEnabled

    val isBiometricEnabled: StateFlow<Boolean> = prefsRepository.isBiometricEnabled
    val cloudBannerDismissed: StateFlow<Boolean> = prefsRepository.cloudBannerDismissed

    fun isPinSet(): Boolean = prefsRepository.isPinSet()
    fun disablePin() = prefsRepository.clearPin()
    fun setBiometricEnabled(enabled: Boolean) = prefsRepository.setBiometricEnabled(enabled)
    fun dismissCloudBanner() = prefsRepository.dismissCloudBanner()

    val transactionCount: StateFlow<Int> = dao.getTransactionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val daysUsing: StateFlow<Int> = dao.getFirstTransactionTimestamp()
        .map { firstTimestamp ->
            if (firstTimestamp == null || firstTimestamp == 0L) 0
            else {
                val diffDays = ((System.currentTimeMillis() - firstTimestamp) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 0) 1 else diffDays
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── PDF Export ─────────────────────────────────────────────────────────────

    private val _pdfUri = MutableStateFlow<Uri?>(null)
    val pdfUri: StateFlow<Uri?> = _pdfUri

    fun exportPdf() {
        viewModelScope.launch(Dispatchers.IO) {
            val displayList = combine(
                dao.getAllTransactions(),
                dao.getAllCategories()
            ) { txs, cats ->
                val catMap = cats.associateBy { it.id }
                txs.map { tx ->
                    val cat = catMap[tx.categoryId]
                    TransactionDisplay(
                        id = tx.id,
                        description = tx.description,
                        amount = tx.amount,
                        timestamp = tx.timestamp,
                        categoryName = cat?.name ?: "Otros",
                        categoryIcon = cat?.iconName ?: "category",
                        categoryColor = cat?.colorHex ?: "#6B7280"
                    )
                }
            }.first()
            val uri = PdfExporter.generate(context, displayList)
            _pdfUri.value = uri
        }
    }

    fun sharePdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte TuGasto")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir reporte").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        _pdfUri.value = null
    }

    fun clearPdfUri() { _pdfUri.value = null }

    // ── Borrar todos los datos ─────────────────────────────────────────────────

    private val _dataCleared = MutableStateFlow(false)
    val dataCleared: StateFlow<Boolean> = _dataCleared

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAllTransactions()
            withContext(Dispatchers.Main) { _dataCleared.value = true }
        }
    }

    fun resetDataClearedFlag() { _dataCleared.value = false }

    // ── Preferences ────────────────────────────────────────────────────────────

    fun savePreferences(income: Double, goal: Double, savings: Double) {
        prefsRepository.updatePreferences(income, goal, savings)
    }

    fun toggleProStatus(isPro: Boolean) { prefsRepository.setProUser(isPro) }
    fun toggleDarkMode(isDark: Boolean) { prefsRepository.setDarkMode(isDark) }
    fun toggleAutoDetection(isEnabled: Boolean) { prefsRepository.setAutoDetection(isEnabled) }

    fun onGmailConnected(email: String) {
        prefsRepository.connectGmail(email)
        GmailSyncWorker.schedule(context)
    }

    fun disconnectGmail() {
        prefsRepository.disconnectGmail()
        GmailSyncWorker.cancel(context)
        GmailSyncService(context).disconnect()
    }

    // ── Cloud Auth + Backup ────────────────────────────────────────────────────

    private val _cloudAuthState = MutableStateFlow(
        if (supabaseService.isSignedIn)
            CloudAuthState.SignedIn(supabaseService.userEmail ?: "")
        else
            CloudAuthState.NotSignedIn
    )
    val cloudAuthState: StateFlow<CloudAuthState> = _cloudAuthState.asStateFlow()

    private val _cloudOpResult = MutableStateFlow<CloudOpResult>(CloudOpResult.Idle)
    val cloudOpResult: StateFlow<CloudOpResult> = _cloudOpResult.asStateFlow()

    private val _isCloudLoading = MutableStateFlow(false)
    val isCloudLoading: StateFlow<Boolean> = _isCloudLoading.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isCloudLoading.value = true
            val result = supabaseService.signIn(email, password)
            result.fold(
                onSuccess = {
                    _cloudAuthState.value = CloudAuthState.SignedIn(supabaseService.userEmail ?: email)
                    _cloudOpResult.value = CloudOpResult.Success("Sesión iniciada correctamente")
                    syncProStatusFromCloud()
                },
                onFailure = { e ->
                    _cloudOpResult.value = CloudOpResult.Error(e.message ?: "Error al iniciar sesión")
                }
            )
            _isCloudLoading.value = false
        }
    }

    private fun syncProStatusFromCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            supabaseService.ensureUserProfile()
            supabaseService.fetchProStatus().onSuccess { isPro ->
                if (isPro) prefsRepository.setProUser(true)
            }
        }
    }

    // ── Verificación manual de estado Pro ─────────────────────────────────────

    private val _isVerifyingPro = MutableStateFlow(false)
    val isVerifyingPro: StateFlow<Boolean> = _isVerifyingPro.asStateFlow()

    private val _proVerificationResult = MutableStateFlow<String?>(null)
    val proVerificationResult: StateFlow<String?> = _proVerificationResult.asStateFlow()

    fun refreshProStatus() {
        if (!supabaseService.isSignedIn) {
            _proVerificationResult.value = "Necesitas iniciar sesión en la nube primero"
            return
        }
        viewModelScope.launch {
            _isVerifyingPro.value = true
            supabaseService.fetchProStatus().fold(
                onSuccess = { isPro ->
                    prefsRepository.setProUser(isPro)
                    _proVerificationResult.value = if (isPro)
                        "¡Ya eres PRO! Gracias por tu apoyo."
                    else
                        "Aún no hemos verificado tu pago. ¿Ya hiciste el Yape y escribiste al WhatsApp?"
                },
                onFailure = { e ->
                    _proVerificationResult.value = "Error al verificar: ${e.message}"
                }
            )
            _isVerifyingPro.value = false
        }
    }

    fun clearProVerificationResult() { _proVerificationResult.value = null }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isCloudLoading.value = true
            val result = supabaseService.signUp(email, password)
            result.fold(
                onSuccess = {
                    _cloudOpResult.value = CloudOpResult.Success(
                        "Cuenta creada. Revisa tu email para confirmar y luego inicia sesión."
                    )
                },
                onFailure = { e ->
                    _cloudOpResult.value = CloudOpResult.Error(e.message ?: "Error al crear cuenta")
                }
            )
            _isCloudLoading.value = false
        }
    }

    fun signOut() {
        supabaseService.signOut()
        _cloudAuthState.value = CloudAuthState.NotSignedIn
    }

    fun backupData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            val result = cloudSyncService.backup()
            withContext(Dispatchers.Main) {
                _cloudOpResult.value = result.fold(
                    onSuccess = { count -> CloudOpResult.Success("Backup exitoso: $count transacciones guardadas") },
                    onFailure = { e -> CloudOpResult.Error(e.message ?: "Error al hacer backup") }
                )
                _isCloudLoading.value = false
            }
        }
    }

    fun restoreData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            val result = cloudSyncService.restore()
            withContext(Dispatchers.Main) {
                _cloudOpResult.value = result.fold(
                    onSuccess = { count -> CloudOpResult.Success("Restaurado: $count transacciones recuperadas") },
                    onFailure = { e -> CloudOpResult.Error(e.message ?: "Error al restaurar datos") }
                )
                _isCloudLoading.value = false
            }
        }
    }

    fun clearCloudOpResult() { _cloudOpResult.value = CloudOpResult.Idle }
}
