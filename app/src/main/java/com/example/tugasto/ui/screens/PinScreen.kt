package com.example.tugasto.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tugasto.R
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoBlueExtraLight

@Composable
fun PinScreen(
    isSetup: Boolean,
    onSuccess: () -> Unit,
    onCancel: (() -> Unit)? = null,
    onResetPin: (() -> Unit)? = null,
    viewModel: PinViewModel = hiltViewModel()
) {
    val pin by viewModel.pin.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val nuclearResetComplete by viewModel.nuclearResetComplete.collectAsState()
    val context = LocalContext.current
    val activity = context as AppCompatActivity

    var showNuclearDialog by remember { mutableStateOf(false) }

    val biometricAvailable = BiometricManager.from(context)
        .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
        BiometricManager.BIOMETRIC_SUCCESS

    val showBiometric = !isSetup && isBiometricEnabled && biometricAvailable

    LaunchedEffect(Unit) {
        if (isSetup) viewModel.startSetup()
    }

    LaunchedEffect(success) {
        if (success) onSuccess()
    }

    LaunchedEffect(nuclearResetComplete) {
        if (nuclearResetComplete) onSuccess()
    }

    // Auto-trigger biometric on lock screen open
    LaunchedEffect(showBiometric) {
        if (showBiometric) triggerBiometric(activity, "Desbloquear TuGasto", "Usa tu huella o rostro para continuar") {
            viewModel.onBiometricSuccess()
        }
    }

    if (showNuclearDialog) {
        NuclearResetDialog(
            onConfirm = {
                showNuclearDialog = false
                viewModel.nuclearReset()
            },
            onDismiss = { showNuclearDialog = false }
        )
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
            val appIconBitmap = remember {
                val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_tugasto_round)!!
                val size = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
                val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = AndroidCanvas(bmp)
                drawable.setBounds(0, 0, size, size)
                drawable.draw(canvas)
                bmp.asImageBitmap()
            }
            Image(
                painter = BitmapPainter(appIconBitmap),
                contentDescription = "TuGasto",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "TuGasto",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // PIN dots + forgot link
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
            if (!isSetup) {
                TextButton(onClick = {
                    if (isBiometricEnabled && biometricAvailable) {
                        triggerBiometric(activity, "Verificar identidad", "Confirma con biométrico para restablecer tu PIN") {
                            viewModel.resetPinOnly()
                            onResetPin?.invoke()
                        }
                    } else {
                        showNuclearDialog = true
                    }
                }) {
                    Text(
                        "¿Olvidaste tu PIN?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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
                        triggerBiometric(activity, "Desbloquear TuGasto", "Usa tu huella o rostro para continuar") {
                            viewModel.onBiometricSuccess()
                        }
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

@Composable
private fun NuclearResetDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "¿Eliminar todos los datos?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Esta acción eliminará permanentemente todas tus transacciones y quitará el PIN. No se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar todo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun triggerBiometric(
    activity: AppCompatActivity,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        }
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText("Cancelar")
        .build()
    prompt.authenticate(promptInfo)
}
