package com.example.tugasto.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
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
    onSetupPin: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToProUpsell: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val pdfUri by viewModel.pdfUri.collectAsState()
    val dataCleared by viewModel.dataCleared.collectAsState()

    LaunchedEffect(pdfUri) {
        pdfUri?.let { uri ->
            viewModel.sharePdf(uri)
        }
    }

    LaunchedEffect(dataCleared) {
        if (dataCleared) {
            Toast.makeText(context, "Todos los datos han sido eliminados", Toast.LENGTH_SHORT).show()
            viewModel.resetDataClearedFlag()
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Borrar todos los datos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("Esta acción eliminará todas tus transacciones permanentemente y no se puede deshacer. ¿Estás seguro?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllData(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TuGastoRed)
                ) { Text("Sí, borrar todo") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    val income by viewModel.monthlyIncome.collectAsState()
    val goal by viewModel.emergencyFundGoal.collectAsState()
    val savings by viewModel.currentSavings.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val isDarkModePref by viewModel.isDarkMode.collectAsState()
    val isDarkMode = isDarkModePref ?: androidx.compose.foundation.isSystemInDarkTheme()
    val connectedEmail by viewModel.connectedGmailEmail.collectAsState()
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val txCount by viewModel.transactionCount.collectAsState()
    val daysUsing by viewModel.daysUsing.collectAsState()

    val cloudAuthState by viewModel.cloudAuthState.collectAsState()
    val cloudOpResult by viewModel.cloudOpResult.collectAsState()
    val isCloudLoading by viewModel.isCloudLoading.collectAsState()
    val isVerifyingPro by viewModel.isVerifyingPro.collectAsState()
    val proVerificationResult by viewModel.proVerificationResult.collectAsState()

    LaunchedEffect(cloudOpResult) {
        when (val result = cloudOpResult) {
            is CloudOpResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.clearCloudOpResult()
            }
            is CloudOpResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.clearCloudOpResult()
            }
            else -> {}
        }
    }

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
        BudgetBottomSheet(
            initialIncome = income,
            initialGoal = goal,
            initialSavings = savings,
            onDismiss = { showDialog = false },
            onSave = { newIncome, newGoal, newSavings ->
                viewModel.savePreferences(newIncome, newGoal, newSavings)
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
            item { ProfileHeader(isProUser, cloudAuthState) }
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
                    isBiometricEnabled = isBiometricEnabled,
                    onSetupPin = onSetupPin,
                    onDisablePin = { viewModel.disablePin() },
                    onBiometricChange = { viewModel.setBiometricEnabled(it) }
                )
            }
            item {
                CloudAccountCard(
                    authState = cloudAuthState,
                    isLoading = isCloudLoading,
                    onSignIn = { email, pass -> viewModel.signIn(email, pass) },
                    onSignUp = { email, pass -> viewModel.signUp(email, pass) },
                    onSignOut = { viewModel.signOut() },
                    onBackup = { viewModel.backupData() },
                    onRestore = { viewModel.restoreData() }
                )
            }
            item {
                ProStatusCard(
                    isProUser = isProUser,
                    isSignedIn = cloudAuthState is CloudAuthState.SignedIn,
                    isVerifying = isVerifyingPro,
                    verificationResult = proVerificationResult,
                    onVerify = { viewModel.refreshProStatus() },
                    onClearResult = { viewModel.clearProVerificationResult() },
                    onNavigateToProUpsell = onNavigateToProUpsell
                )
            }
            item {
                SettingsSection(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { viewModel.toggleDarkMode(it) },
                    onOpenSettings = { showDialog = true },
                    onNavigateToCategories = onNavigateToCategories,
                    onExportPdf = { viewModel.exportPdf() },
                    onDeleteData = { showDeleteConfirm = true }
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

    val cardColor = if (isConnected)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.surface

    val iconBg = if (isConnected)
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
    else
        MaterialTheme.colorScheme.primaryContainer

    val iconTint = if (isConnected)
        MaterialTheme.colorScheme.secondary
    else
        MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.MailOutline,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Detección automática",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (!isProUser) {
                            Spacer(Modifier.width(8.dp))
                            ProBadge()
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    if (isConnected) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = connectedEmail!!.substringBefore("@"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "@gmail.com",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                )
                            )
                        }
                    } else {
                        Text(
                            text = "Lee tus Yapes de Gmail automáticamente",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                isConnected -> {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Desconectar cuenta", fontWeight = FontWeight.SemiBold)
                    }
                }
                isProUser -> {
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            Icons.Default.MailOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Conectar Gmail", fontWeight = FontWeight.SemiBold)
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "Activa el Plan Pro desde Presupuesto y Metas para conectar Gmail y registrar tus Yapes automáticamente.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            "PRO",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

// ── Profile Header ────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(isProUser: Boolean, cloudAuthState: CloudAuthState) {
    val signedIn = cloudAuthState as? CloudAuthState.SignedIn

    val displayName = signedIn?.email
        ?.substringBefore("@")
        ?.replaceFirstChar { it.uppercase() }
        ?: if (isProUser) "Usuario Premium" else "Usuario Gratuito"

    val initial = signedIn?.email?.firstOrNull()?.uppercaseChar()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    if (signedIn != null) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (initial != null) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TuGastoBlue,
                    modifier = Modifier.size(45.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            displayName,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        if (signedIn != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                signedIn.email,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            if (signedIn != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "☁ Sincronizado",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
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
                    colors = appSwitchColors()
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
                        colors = appSwitchColors()
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
    onNavigateToCategories: () -> Unit,
    onExportPdf: () -> Unit,
    onDeleteData: () -> Unit
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
            SettingClickableItem(Icons.Default.ColorLens, "Categorías Personalizadas", Color(0xFF8B5CF6), onClick = onNavigateToCategories)
            SettingClickableItem(Icons.Default.FileDownload, "Exportar PDF", TuGastoBlue, onClick = onExportPdf)
            SettingClickableItem(Icons.Default.DeleteForever, "Borrar todos mis datos", TuGastoRed, onClick = onDeleteData)
        }
    }
}

@Composable
private fun appSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = Color.Transparent,
    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)

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
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = appSwitchColors())
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

// ── Cloud Account Card ────────────────────────────────────────────────────────

@Composable
private fun CloudAccountCard(
    authState: CloudAuthState,
    isLoading: Boolean,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = {
                Text(
                    "Restaurar datos",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text("Esto reemplazará todas tus transacciones locales con las del backup. ¿Continuar?")
            },
            confirmButton = {
                Button(
                    onClick = { onRestore(); showRestoreConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue)
                ) { Text("Sí, restaurar") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (authState) {
                                is CloudAuthState.SignedIn -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = if (authState is CloudAuthState.SignedIn)
                                Icons.Default.CloudDone else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (authState is CloudAuthState.SignedIn)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Cuenta en la Nube",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        when (authState) {
                            is CloudAuthState.SignedIn -> authState.email
                            is CloudAuthState.NotSignedIn -> "Guarda tus datos de forma segura"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (authState is CloudAuthState.SignedIn) {
                    TextButton(onClick = onSignOut) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Salir", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (authState) {
                is CloudAuthState.SignedIn -> {
                    // Backup + Restore buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { if (!isLoading) onBackup() },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue)
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Backup", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                        OutlinedButton(
                            onClick = { if (!isLoading) showRestoreConfirm = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Restaurar", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }

                is CloudAuthState.NotSignedIn -> {
                    // Login form
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = if (showPassword)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            TextButton(
                                onClick = { showPassword = !showPassword },
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    if (showPassword) "Ocultar" else "Ver",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank() && !isLoading)
                                onSignIn(email.trim(), password)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Iniciar sesión",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank() && !isLoading)
                                onSignUp(email.trim(), password)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Crear cuenta nueva",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

            }
        }
    }
}

// ── Budget Bottom Sheet ───────────────────────────────────────────────────────

// ── Pro Status Card ───────────────────────────────────────────────────────────

@Composable
private fun ProStatusCard(
    isProUser: Boolean,
    isSignedIn: Boolean,
    isVerifying: Boolean,
    verificationResult: String?,
    onVerify: () -> Unit,
    onClearResult: () -> Unit,
    onNavigateToProUpsell: () -> Unit
) {
    LaunchedEffect(verificationResult) {
        if (verificationResult != null) {
            kotlinx.coroutines.delay(4000)
            onClearResult()
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isProUser) Icons.Filled.CheckCircle else Icons.Filled.Stars,
                    contentDescription = null,
                    tint = if (isProUser) TuGastoGreen else Color(0xFFD97706),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isProUser) "TuGasto PRO activo" else "Plan Gratuito",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (isProUser) "Todas las funciones desbloqueadas"
                        else "Actualiza para desbloquear funciones avanzadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (verificationResult != null) {
                Spacer(Modifier.height(10.dp))
                val isSuccess = verificationResult.startsWith("¡Ya eres PRO")
                Text(
                    verificationResult,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSuccess) TuGastoGreen else MaterialTheme.colorScheme.error
                )
            }

            if (!isProUser) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onNavigateToProUpsell,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ver planes", style = MaterialTheme.typography.labelMedium)
                    }
                    if (isSignedIn) {
                        Button(
                            onClick = onVerify,
                            enabled = !isVerifying,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Verificar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

private fun Double.toInputStr() =
    if (this <= 0.0) "" else if (this == kotlin.math.floor(this)) this.toInt().toString() else this.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetBottomSheet(
    initialIncome: Double,
    initialGoal: Double,
    initialSavings: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var incomeStr by remember { mutableStateOf(initialIncome.toInputStr()) }
    var goalStr by remember { mutableStateOf(initialGoal.toInputStr()) }
    var savingsStr by remember { mutableStateOf(initialSavings.toInputStr()) }

    val incomeValue = incomeStr.toDoubleOrNull() ?: 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Título
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Presupuesto y Metas",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Configura tus finanzas personales",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Ingreso mensual
            BudgetInputCard(
                icon = Icons.Filled.TrendingUp,
                iconColor = TuGastoGreen,
                title = "Ingreso mensual",
                subtitle = "Tu salario u otros ingresos fijos",
                value = incomeStr,
                onValueChange = { incomeStr = it.filter { c -> c.isDigit() || c == '.' } }
            )

            // Fondo de emergencia + atajos
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BudgetInputCard(
                    icon = Icons.Filled.AccountBalance,
                    iconColor = TuGastoBlue,
                    title = "Fondo de emergencia",
                    subtitle = "Meta recomendada: 3 a 6 meses de gastos",
                    value = goalStr,
                    onValueChange = { goalStr = it.filter { c -> c.isDigit() || c == '.' } }
                )
                if (incomeValue > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "1 mes", 3 to "3 meses", 6 to "6 meses").forEach { (mult, label) ->
                            val preset = (incomeValue * mult).toInputStr()
                            SuggestionChip(
                                onClick = { goalStr = preset },
                                label = {
                                    Text(preset.let { "S/ $it" }, style = MaterialTheme.typography.labelSmall)
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = TuGastoBlue.copy(alpha = 0.08f),
                                    labelColor = TuGastoBlue
                                )
                            )
                        }
                    }
                }
            }

            // Ahorro actual
            BudgetInputCard(
                icon = Icons.Filled.Savings,
                iconColor = Color(0xFF8B5CF6),
                title = "Ahorro actual",
                subtitle = "¿Cuánto tienes ahorrado hoy?",
                value = savingsStr,
                onValueChange = { savingsStr = it.filter { c -> c.isDigit() || c == '.' } }
            )

            // Botón guardar
            Button(
                onClick = {
                    onSave(
                        incomeStr.toDoubleOrNull() ?: initialIncome,
                        goalStr.toDoubleOrNull() ?: initialGoal,
                        savingsStr.toDoubleOrNull() ?: initialSavings
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue)
            ) {
                Text(
                    "Guardar cambios",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun BudgetInputCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                },
                prefix = {
                    Text(
                        "S/  ",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
