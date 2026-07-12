package com.example.tugasto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tugasto.data.local.entity.CategoryEntity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasto.ui.theme.TuGastoBg
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoBlueExtraLight
import com.example.tugasto.ui.theme.TuGastoBlueLight
import com.example.tugasto.ui.theme.TuGastoGray100
import com.example.tugasto.ui.theme.TuGastoGray200
import com.example.tugasto.ui.theme.TuGastoGray400
import com.example.tugasto.ui.theme.TuGastoGray500
import com.example.tugasto.ui.theme.TuGastoGray700
import com.example.tugasto.ui.theme.TuGastoGray900
import com.example.tugasto.ui.theme.TuGastoGreen
import com.example.tugasto.ui.theme.TuGastoGreenDark
import com.example.tugasto.ui.theme.TuGastoGreenLight
import com.example.tugasto.ui.theme.TuGastoRed
import com.example.tugasto.ui.theme.TuGastoRedLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel(),
    onUpgradeClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val estimatedExpense by viewModel.estimatedExpense.collectAsState()
    val estimatedBalance by viewModel.estimatedBalance.collectAsState()
    val vsLastMonthPercent by viewModel.vsLastMonthPercent.collectAsState()
    val aiReport by viewModel.aiReport.collectAsState()
    val emergencyFundGoal by viewModel.emergencyFundGoal.collectAsState()
    val currentSavings by viewModel.currentSavings.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val isAutoDetectionEnabled by viewModel.isAutoDetectionEnabled.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncResult by viewModel.syncResult.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Análisis Pro",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = if (isProUser) 0.dp else 12.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

            item {
                val monthName = java.text.SimpleDateFormat("MMMM", java.util.Locale("es", "PE"))
                    .format(java.util.Date()).uppercase()
                Text(
                    "PRONÓSTICO DE $monthName",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            item { ProjectionCard(estimatedExpense, estimatedBalance, vsLastMonthPercent) }

            item { EmergencyFundCard(emergencyFundGoal, currentSavings, onAjustarMetas = onNavigateToProfile) }

            if (!isAutoDetectionEnabled) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text("Gmail no configurado", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Conecta tu Gmail en Perfil para que TuGasto detecte automáticamente tus Yapes y los registre sin esfuerzo.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer)
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = onNavigateToProfile, modifier = Modifier.align(Alignment.End)) {
                                Text("Configurar Gmail →", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error))
                            }
                        }
                    }
                }
            } else {
                item { GmailSyncCard(isSyncing, syncResult, onSync = { viewModel.forceSyncGmail() }, onDismiss = { viewModel.clearSyncResult() }) }

                if (pendingTransactions.isNotEmpty()) {
                    item {
                        PendingTransactionsInbox(
                            transactions = pendingTransactions,
                            categories = categories,
                            onConfirm = { id, catId, newDesc -> viewModel.confirmTransaction(id, catId, newDesc) },
                            onDismiss = { id -> viewModel.dismissTransaction(id) }
                        )
                    }
                }
            }

            item { AiAssistantCard(aiReport) }

            item { HistoryListItem(onClick = onHistoryClick) }

                item { Spacer(Modifier.height(8.dp)) }
            }

            if (!isProUser) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(TuGastoBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TuGastoBlue,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Función Premium",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Desbloquea proyecciones y el asistente de IA.",
                            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onUpgradeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Actualizar a PRO", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectionCard(estimatedExpense: Double, estimatedBalance: Double, vsLastMonth: Double?) {
    val balancePositive = estimatedBalance >= 0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Izquierda: proyección de gasto
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Proyección a fin de mes",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "S/ ${String.format(java.util.Locale.US, "%.2f", estimatedExpense)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    if (vsLastMonth != null) {
                        val isUp = vsLastMonth >= 0
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isUp) TuGastoRedLight else TuGastoGreenLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isUp) TuGastoRed else TuGastoGreenDark,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "${String.format(java.util.Locale.US, "%.0f", kotlin.math.abs(vsLastMonth))}% vs mes ant.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isUp) TuGastoRed else TuGastoGreenDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    } else {
                        Text(
                            "Sin datos del mes anterior",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(90.dp)
                        .background(MaterialTheme.colorScheme.outline)
                        .align(Alignment.CenterVertically)
                )

                // Derecha: saldo estimado
                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                ) {
                    Text(
                        "Saldo estimado",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "S/ ${String.format(java.util.Locale.US, "%.2f", estimatedBalance)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (balancePositive) MaterialTheme.colorScheme.onSurface else TuGastoRed
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (balancePositive) TuGastoGreenLight else TuGastoRedLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (balancePositive) "Dentro del plan" else "Sobre el presupuesto",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (balancePositive) TuGastoGreenDark else TuGastoRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))
            Text(
                "* Basado en tu promedio de gasto diario actual.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}



@Composable
private fun EmergencyFundCard(goal: Double, savings: Double, onAjustarMetas: () -> Unit = {}) {
    val progress = if (goal > 0) (savings / goal).toFloat().coerceIn(0f, 1f) else 0f
    val percentStr = "${String.format(java.util.Locale.US, "%.0f", progress * 100)}%"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Progreso de Meta: Fondo de Emergencia",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Has ahorrado S/ ${String.format(java.util.Locale.US, "%.0f", savings)} de S/ ${String.format(java.util.Locale.US, "%.0f", goal)}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onAjustarMetas, modifier = Modifier.align(Alignment.End)) {
                Text(
                    "Ajustar metas",
                    style = MaterialTheme.typography.labelMedium.copy(color = TuGastoBlue)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingTransactionsInbox(
    transactions: List<com.example.tugasto.data.local.entity.TransactionEntity>,
    categories: List<CategoryEntity>,
    onConfirm: (Int, Int, String) -> Unit,
    onDismiss: (Int) -> Unit
) {
    var editedDescriptions by remember { mutableStateOf(mapOf<Int, String>()) }
    var selectedCategories by remember { mutableStateOf(mapOf<Int, Int>()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${transactions.size} Yape(s) por confirmar",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(Modifier.height(12.dp))

            transactions.forEach { tx ->
                val currentDesc = editedDescriptions[tx.id] ?: tx.description
                val currentCategoryId = selectedCategories[tx.id] ?: tx.categoryId
                val currentCategory = categories.find { it.id == currentCategoryId }

                var dropdownExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        // Descripción + monto
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = currentDesc,
                                onValueChange = { editedDescriptions = editedDescriptions + (tx.id to it) },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                singleLine = true
                            )
                            Text(
                                "S/ ${String.format(java.util.Locale.US, "%.2f", tx.amount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Dropdown de categoría
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = currentCategory?.name ?: "Seleccionar categoría",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoría", style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = TuGastoBlue,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                cat.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (cat.id == currentCategoryId) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        },
                                        onClick = {
                                            selectedCategories = selectedCategories + (tx.id to cat.id)
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Acciones
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { onDismiss(tx.id) },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    "Ignorar",
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { onConfirm(tx.id, currentCategoryId, currentDesc) },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue)
                            ) {
                                Text(
                                    "Confirmar",
                                    style = MaterialTheme.typography.labelMedium.copy(color = androidx.compose.ui.graphics.Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun AiAssistantCard(aiReport: AiReport?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Observaciones de la IA",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (aiReport == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Analizando patrones de gasto...",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            AiReportItemCard(Icons.Default.Warning, "Alerta", aiReport.alert ?: "")
            AiReportItemCard(Icons.Default.Lightbulb, "Sugerencia", aiReport.suggestion ?: "")
            AiReportItemCard(Icons.Default.CheckCircle, "Reconocimiento", aiReport.congratulation ?: "")
        }
    }
}

@Composable
private fun AiReportItemCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
            )
        }
    }
}

@Composable
private fun GmailSyncCard(
    isSyncing: Boolean,
    syncResult: String?,
    onSync: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TuGastoBlueExtraLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "G",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TuGastoBlue
                            )
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Detección de Yapes",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            if (isSyncing) "Sincronizando..." else "Gmail conectado · sync cada 15 min",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                Button(
                    onClick = onSync,
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue)
                ) {
                    if (isSyncing) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = androidx.compose.ui.graphics.Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sync", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            if (syncResult != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        syncResult,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    TextButton(onClick = onDismiss) {
                        Text("OK", style = MaterialTheme.typography.labelSmall.copy(color = TuGastoBlue))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryListItem(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Ver historial detallado",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    "Análisis de transacciones pasadas",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
