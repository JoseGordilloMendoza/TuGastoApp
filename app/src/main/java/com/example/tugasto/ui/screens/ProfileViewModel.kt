package com.example.tugasto.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.data.local.dao.TuGastoDao
import com.example.tugasto.data.local.prefs.UserPreferencesRepository
import com.example.tugasto.service.GmailSyncService
import com.example.tugasto.service.GmailSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val dao: TuGastoDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val monthlyIncome: StateFlow<Double> = prefsRepository.monthlyIncome
    val emergencyFundGoal: StateFlow<Double> = prefsRepository.emergencyFundGoal
    val currentSavings: StateFlow<Double> = prefsRepository.currentSavings
    val isProUser: StateFlow<Boolean> = prefsRepository.isProUser
    val isDarkMode: StateFlow<Boolean?> = prefsRepository.isDarkMode

    val connectedGmailEmail: StateFlow<String?> = prefsRepository.connectedGmailEmail
    val isAutoDetectionEnabled: StateFlow<Boolean> = prefsRepository.isAutoDetectionEnabled

    val isPinEnabled: StateFlow<Boolean> = prefsRepository.isPinEnabled

    fun isPinSet(): Boolean = prefsRepository.isPinSet()
    fun disablePin() = prefsRepository.clearPin()
    fun setBiometricEnabled(enabled: Boolean) = prefsRepository.setBiometricEnabled(enabled)
    fun isBiometricEnabled(): Boolean = prefsRepository.isBiometricEnabled()

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

    fun savePreferences(income: Double, goal: Double, savings: Double) {
        prefsRepository.updatePreferences(income, goal, savings)
    }

    fun toggleProStatus(isPro: Boolean) {
        prefsRepository.setProUser(isPro)
    }

    fun toggleDarkMode(isDark: Boolean) {
        prefsRepository.setDarkMode(isDark)
    }

    fun toggleAutoDetection(isEnabled: Boolean) {
        prefsRepository.setAutoDetection(isEnabled)
    }

    fun onGmailConnected(email: String) {
        prefsRepository.connectGmail(email)
        GmailSyncWorker.schedule(context)
    }

    fun disconnectGmail() {
        prefsRepository.disconnectGmail()
        GmailSyncWorker.cancel(context)
        GmailSyncService(context).disconnect()
    }
}
