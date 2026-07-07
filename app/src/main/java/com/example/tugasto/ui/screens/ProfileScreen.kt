package com.example.tugasto.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tugasto.service.GmailSyncService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoBlueExtraLight
import com.example.tugasto.ui.theme.TuGastoBlueLight
import com.example.tugasto.ui.theme.TuGastoGray400
import com.example.tugasto.ui.theme.TuGastoGray500
import com.example.tugasto.ui.theme.TuGastoGreen
import com.example.tugasto.ui.theme.TuGastoGreenLight
import com.example.tugasto.ui.theme.TuGastoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSetupPin: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val income by viewModel.monthlyIncome.collectAsState()
    val goal by viewModel.emergencyFundGoal.collectAsState()
    val savings by viewModel.currentSavings.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val isDarkModePref by viewModel.isDarkMode.collectAsState()
    val isDarkMode = isDarkModePref ?: androidx.compose.foundation.isSystemInDarkTheme()
    val connectedEmail by viewModel.connectedGmailEmail.collectAsState()
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val txCount by viewModel.transactionCount.collectAsState()
    val daysUsing by viewModel.daysUsing.collectAsState()

    val gmailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                account.email?.let { viewModel.onGmailConnected(it) }
            } catch (e: ApiException) {
                Toast.makeText(context, "Error al conectar Gmail (${e.statusCode})", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showDialog) {
        SettingsDialog(
            initialIncome = income,
            initialGoal = goal,
            initialSavings = savings,
            initialIsPro = isProUser,
            onDismiss = { showDialog = false },
            onSave = { newIncome, newGoal, newSavings, newIsPro ->
                viewModel.savePreferences(newIncome, newGoal, newSavings)
                viewModel.toggleProStatus(newIsPro)
                showDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Mi Perfil",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item { ProfileHeader(isProUser) }
            item { QuickStatsRow(daysUsing, txCount) }
            item {
                GmailConnectionCard(
                    connectedEmail = connectedEmail,
                    isProUser = isProUser,
                    onConnect = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(Scope(GmailSyncService.GMAIL_SCOPE))
                            .build()
                        gmailLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
                    },
                    onDisconnect = {
                        viewModel.disconnectGmail()
                        Toast.makeText(context, "Gmail desconectado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            item {
                SecuritySection(
                    isPinEnabled = isPinEnabled,
                    isBiometricEnabled = viewModel.isBiometricEnabled(),
                    onSetupPin = onSetupPin,
                    onDisablePin = { viewModel.disablePin() },
                    onBiometricChange = { viewModel.setBiometricEnabled(it) }
                )
            }
            item {
                SettingsSection(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { viewModel.toggleDarkMode(it) },
                    onOpenSettings = { showDialog = true },
                    onToast = { Toast.makeText(context, "Próximamente disponible", Toast.LENGTH_SHORT).show() }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Gmail Connection Card ─────────────────────────────────────────────────────

@Composable
private fun GmailConnectionCard(
    connectedEmail: String?,
    isProUser: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isConnected = connectedEmail != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) TuGastoGreenLight else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isConnected) TuGastoGreen.copy(alpha = 0.15f) else TuGastoBlueExtraLight),
                    contentAlignment = Alignment.Center
                ) {
                    // Google "G" simulado con colores
                    Text(
                        "G",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isConnected) TuGastoGreen else TuGastoBlue
                        )
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Detección con Gmail",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (!isProUser) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TuGastoBlue)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "PRO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        if (isConnected) connectedEmail!! else "Detecta Yapes automáticamente desde tu correo",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isConnected) TuGastoGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                if (isConnected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TuGastoGreen, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isConnected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TuGastoRed)
                ) {
                    Text("Desconectar Gmail", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = onConnect,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue),
                    enabled = isProUser
                ) {
                    Text(
                        "G",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isProUser) "Conectar con Google" else "Disponible en Plan Pro",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                if (!isProUser) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Activa el Plan Pro en Ajustes para conectar Gmail y detectar tus Yapes automáticamente.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

// ── Profile Header ────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(isProUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = TuGastoBlue, modifier = Modifier.size(45.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (isProUser) "Usuario Premium" else "Usuario Gratuito",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isProUser) TuGastoGreenLight else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                if (isProUser) "Nivel: Ahorrador Experto" else "Nivel: Aprendiz",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isProUser) TuGastoGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// ── Quick Stats ───────────────────────────────────────────────────────────────

@Composable
private fun QuickStatsRow(daysUsing: Int, txCount: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(modifier = Modifier.weight(1f), value = daysUsing.toString(), label = "Días Usando")
        StatCard(modifier = Modifier.weight(1f), value = txCount.toString(), label = "Transacciones")
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

// ── Security Section ──────────────────────────────────────────────────────────

@Composable
private fun SecuritySection(
    isPinEnabled: Boolean,
    isBiometricEnabled: Boolean,
    onSetupPin: () -> Unit,
    onDisablePin: () -> Unit,
    onBiometricChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                "SEGURIDAD",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Toggle PIN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TuGastoBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPinEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = TuGastoBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Bloqueo con PIN",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        if (isPinEnabled) "PIN activo" else "Protege tu app con un PIN de 4 dígitos",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Switch(
                    checked = isPinEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) onSetupPin() else onDisablePin()
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = TuGastoBlue)
                )
            }

            // Cambiar PIN (solo si está activo)
            if (isPinEnabled) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 20.dp))
                SettingClickableItem(
                    icon = Icons.Default.Lock,
                    title = "Cambiar PIN",
                    tint = TuGastoBlue,
                    onClick = onSetupPin
                )

                // Toggle biométrico
                Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Huella / Face ID",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = onBiometricChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = TuGastoBlue)
                    )
                }
            }
        }
    }
}

// ── Settings Section ──────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onToast: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                "AJUSTES GENERALES",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            SettingToggleItem(Icons.Default.DarkMode, "Modo Oscuro", MaterialTheme.colorScheme.onSurfaceVariant, isDarkMode, onDarkModeChange)

            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

            Text(
                "PREFERENCIAS DE CUENTA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            SettingClickableItem(Icons.Default.TrackChanges, "Presupuesto y Metas", TuGastoGreen, onClick = onOpenSettings)
            SettingClickableItem(Icons.Default.ColorLens, "Categorías Personalizadas", Color(0xFF8B5CF6), onClick = onToast)
            SettingClickableItem(Icons.Default.FileDownload, "Exportar a Excel / PDF", MaterialTheme.colorScheme.onSurfaceVariant, onClick = onToast)
            SettingClickableItem(Icons.Default.Lock, "Privacidad y Seguridad", MaterialTheme.colorScheme.onSurfaceVariant, onClick = onToast)
        }
    }
}

@Composable
private fun SettingToggleItem(icon: ImageVector, title: String, tint: Color, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
private fun SettingClickableItem(icon: ImageVector, title: String, tint: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Settings Dialog ───────────────────────────────────────────────────────────

@Composable
private fun SettingsDialog(
    initialIncome: Double, initialGoal: Double, initialSavings: Double, initialIsPro: Boolean,
    onDismiss: () -> Unit, onSave: (Double, Double, Double, Boolean) -> Unit
) {
    var incomeStr by remember { mutableStateOf(initialIncome.toString()) }
    var goalStr by remember { mutableStateOf(initialGoal.toString()) }
    var savingsStr by remember { mutableStateOf(initialSavings.toString()) }
    var isPro by remember { mutableStateOf(initialIsPro) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes de Prototipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activar modo PRO", modifier = Modifier.weight(1f))
                    Switch(checked = isPro, onCheckedChange = { isPro = it }, colors = SwitchDefaults.colors(checkedTrackColor = TuGastoBlue))
                }
                TextField(value = incomeStr, onValueChange = { incomeStr = it }, label = { Text("Ingreso Mensual (S/)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                TextField(value = goalStr, onValueChange = { goalStr = it }, label = { Text("Meta Fondo Emergencia (S/)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                TextField(value = savingsStr, onValueChange = { savingsStr = it }, label = { Text("Ahorro Actual (S/)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(incomeStr.toDoubleOrNull() ?: initialIncome, goalStr.toDoubleOrNull() ?: initialGoal, savingsStr.toDoubleOrNull() ?: initialSavings, isPro) }) {
                Text("Guardar", color = TuGastoBlue)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TuGastoGray500) } }
    )
}
