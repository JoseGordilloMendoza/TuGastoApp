package com.example.tugasto.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tugasto_prefs", Context.MODE_PRIVATE)

    private val _monthlyIncome = MutableStateFlow(prefs.getFloat("monthly_income", 3000f).toDouble())
    val monthlyIncome: StateFlow<Double> = _monthlyIncome.asStateFlow()

    private val _emergencyFundGoal = MutableStateFlow(prefs.getFloat("emergency_goal", 5000f).toDouble())
    val emergencyFundGoal: StateFlow<Double> = _emergencyFundGoal.asStateFlow()

    private val _currentSavings = MutableStateFlow(prefs.getFloat("current_savings", 1500f).toDouble())
    val currentSavings: StateFlow<Double> = _currentSavings.asStateFlow()

    private val _isProUser = MutableStateFlow(prefs.getBoolean("is_pro_user", false))
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _isDarkMode = MutableStateFlow(
        if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    // Gmail API — detección automática de Yapes vía correo
    private val _connectedGmailEmail = MutableStateFlow<String?>(
        prefs.getString("gmail_email", null)
    )
    val connectedGmailEmail: StateFlow<String?> = _connectedGmailEmail.asStateFlow()

    val isGmailConnected: StateFlow<Boolean> = MutableStateFlow(
        prefs.getString("gmail_email", null) != null
    ).also { flow ->
        // Se mantiene sincronizado con connectedGmailEmail
    }

    // Mantenemos isAutoDetectionEnabled como alias de isGmailConnected
    // para no romper referencias existentes en ProfileScreen/AnalysisScreen
    val isAutoDetectionEnabled: StateFlow<Boolean> get() = _isGmailConnectedInternal

    private val _isGmailConnectedInternal = MutableStateFlow(
        prefs.getString("gmail_email", null) != null
    )

    fun updatePreferences(income: Double, goal: Double, savings: Double) {
        prefs.edit()
            .putFloat("monthly_income", income.toFloat())
            .putFloat("emergency_goal", goal.toFloat())
            .putFloat("current_savings", savings.toFloat())
            .apply()
        _monthlyIncome.value = income
        _emergencyFundGoal.value = goal
        _currentSavings.value = savings
    }

    fun setProUser(isPro: Boolean) {
        prefs.edit().putBoolean("is_pro_user", isPro).apply()
        _isProUser.value = isPro
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
        _isDarkMode.value = isDark
    }

    fun connectGmail(email: String) {
        prefs.edit().putString("gmail_email", email).apply()
        _connectedGmailEmail.value = email
        _isGmailConnectedInternal.value = true
    }

    fun disconnectGmail() {
        prefs.edit().remove("gmail_email").remove("gmail_token").apply()
        _connectedGmailEmail.value = null
        _isGmailConnectedInternal.value = false
    }

    fun setAutoDetection(isEnabled: Boolean) { /* no-op */ }

    // ── PIN & Biometría ───────────────────────────────────────────────────────

    private val _isPinEnabled = MutableStateFlow(prefs.getString("pin_hash", null) != null)
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    fun isPinSet(): Boolean = prefs.getString("pin_hash", null) != null

    fun savePin(pin: String) {
        prefs.edit().putString("pin_hash", hashPin(pin)).apply()
        _isPinEnabled.value = true
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString("pin_hash", null) ?: return false
        return stored == hashPin(pin)
    }

    fun clearPin() {
        prefs.edit().remove("pin_hash").remove("biometric_enabled").apply()
        _isPinEnabled.value = false
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean("biometric_enabled", false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    private fun hashPin(pin: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
