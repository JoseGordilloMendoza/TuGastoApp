package com.example.tugasto.ui.screens

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoBlueExtraLight

@Composable
fun PinScreen(
    isSetup: Boolean,
    onSuccess: () -> Unit,
    onCancel: (() -> Unit)? = null,
    viewModel: PinViewModel = hiltViewModel()
) {
    val pin by viewModel.pin.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val context = LocalContext.current
    val activity = context as AppCompatActivity

    val showBiometric = !isSetup && viewModel.isBiometricEnabled &&
        BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
        BiometricManager.BIOMETRIC_SUCCESS

    LaunchedEffect(Unit) {
        if (isSetup) viewModel.startSetup()
    }

    LaunchedEffect(success) {
        if (success) onSuccess()
    }

    // Auto-trigger biometric on lock screen open
    LaunchedEffect(showBiometric) {
        if (showBiometric) triggerBiometric(activity, viewModel)
    }

    val subtitle = when {
        isSetup && mode == PinViewModel.Mode.SETUP_ENTER -> "Crea tu PIN de 4 dígitos"
        isSetup && mode == PinViewModel.Mode.SETUP_CONFIRM -> "Confirma tu PIN"
        else -> "Ingresa tu PIN"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(TuGastoBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "TG",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "TuGasto",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // PIN dots
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                repeat(4) { index ->
                    PinDot(filled = index < pin.length)
                }
            }
            Spacer(Modifier.height(16.dp))
            val errorText = error
            if (errorText != null) {
                Text(
                    errorText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            } else {
                Spacer(Modifier.height(20.dp))
            }
        }

        // Number pad
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val rows = listOf(
                listOf('1', '2', '3'),
                listOf('4', '5', '6'),
                listOf('7', '8', '9')
            )
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { digit ->
                        PinButton(label = digit.toString()) { viewModel.addDigit(digit) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (showBiometric) {
                    PinIconButton(icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Biométrico", tint = TuGastoBlue, modifier = Modifier.size(28.dp)) }) {
                        triggerBiometric(activity, viewModel)
                    }
                } else {
                    Spacer(Modifier.size(72.dp))
                }
                PinButton(label = "0") { viewModel.addDigit('0') }
                PinIconButton(icon = { Icon(Icons.Default.Backspace, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) }) {
                    viewModel.deleteDigit()
                }
            }

            Spacer(Modifier.height(8.dp))

            if (isSetup && onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun PinDot(filled: Boolean) {
    val size by animateDpAsState(
        targetValue = if (filled) 18.dp else 16.dp,
        animationSpec = tween(150),
        label = "dot"
    )
    val color by animateColorAsState(
        targetValue = if (filled) TuGastoBlue else TuGastoBlueExtraLight,
        animationSpec = tween(150),
        label = "dotColor"
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun PinButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 26.sp
            )
        )
    }
}

@Composable
private fun PinIconButton(icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

private fun triggerBiometric(activity: AppCompatActivity, viewModel: PinViewModel) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.onBiometricSuccess()
            }
        }
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Desbloquear TuGasto")
        .setSubtitle("Usa tu huella o rostro para continuar")
        .setNegativeButtonText("Usar PIN")
        .build()
    prompt.authenticate(promptInfo)
}
