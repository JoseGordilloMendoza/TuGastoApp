package com.example.tugasto.ui.screens

import androidx.lifecycle.ViewModel
import com.example.tugasto.data.local.prefs.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    enum class Mode { SETUP_ENTER, SETUP_CONFIRM, UNLOCK }

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin

    private val _mode = MutableStateFlow(Mode.UNLOCK)
    val mode: StateFlow<Mode> = _mode

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private var pendingPin = ""

    val isPinSet: Boolean get() = prefsRepository.isPinSet()
    val isBiometricEnabled: Boolean get() = prefsRepository.isBiometricEnabled()

    fun startSetup() {
        _mode.value = Mode.SETUP_ENTER
        _pin.value = ""
        _error.value = null
        _success.value = false
        pendingPin = ""
    }

    fun addDigit(d: Char) {
        if (_pin.value.length >= 4) return
        _pin.value += d
        _error.value = null
        if (_pin.value.length == 4) processPin()
    }

    fun deleteDigit() {
        if (_pin.value.isNotEmpty()) _pin.value = _pin.value.dropLast(1)
    }

    fun onBiometricSuccess() {
        _success.value = true
    }

    fun enableBiometric(enabled: Boolean) {
        prefsRepository.setBiometricEnabled(enabled)
    }

    fun disablePin() {
        prefsRepository.clearPin()
    }

    private fun processPin() {
        val entered = _pin.value
        when (_mode.value) {
            Mode.SETUP_ENTER -> {
                pendingPin = entered
                _pin.value = ""
                _mode.value = Mode.SETUP_CONFIRM
            }
            Mode.SETUP_CONFIRM -> {
                if (entered == pendingPin) {
                    prefsRepository.savePin(entered)
                    _success.value = true
                } else {
                    _error.value = "Los PINs no coinciden, intenta de nuevo"
                    _pin.value = ""
                    _mode.value = Mode.SETUP_ENTER
                    pendingPin = ""
                }
            }
            Mode.UNLOCK -> {
                if (prefsRepository.verifyPin(entered)) {
                    _success.value = true
                } else {
                    _error.value = "PIN incorrecto"
                    _pin.value = ""
                }
            }
        }
    }
}
