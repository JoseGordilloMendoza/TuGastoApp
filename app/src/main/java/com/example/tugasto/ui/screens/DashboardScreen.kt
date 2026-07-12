package com.example.tugasto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasto.ui.theme.TuGastoDark
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoGray200
import com.example.tugasto.ui.theme.TuGastoGreenDark
import com.example.tugasto.ui.theme.TuGastoGreenLight
import com.example.tugasto.ui.theme.TuGastoRed
import com.example.tugasto.ui.theme.TuGastoRedLight
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onUpgradeClick: () -> Unit = {},
    onDetailsClick: () -> Unit = {},
    showCloudBanner: Boolean = false,
    onDismissBanner: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val stats by viewModel.periodStats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Mi Dashboard",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            item { TimeSelector(selectedPeriod) { viewModel.setPeriod(it) } }
            if (showCloudBanner) {
                item { CloudBackupBanner(onDismiss = onDismissBanner, onCreateAccount = onNavigateToProfile) }
            }
            item { TotalGastadoCard(stats.totalAmount, stats.vsLastPercent, selectedPeriod) }
            item { DonutChartCard(stats.totalAmount, stats.categoryBreakdown) }
            item { CategoryBreakdownSection(stats.categoryBreakdown, onDetailsClick) }
            if (stats.categoryBreakdown.isNotEmpty()) {
                item { CategoryChartCard(stats.categoryBreakdown) }
            }
            item { ProUpsellBanner(onUpgradeClick = onUpgradeClick) }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun TimeSelector(selected: Int, onSelect: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        Row {
            listOf("SEMANA", "MES").forEachIndexed { index, label ->
                val isSelected = selected == index
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable(interactionSource = interactionSource, indication = null) { onSelect(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalGastadoCard(totalAmount: Double, vsLastPercent: Double?, period: Int) {
    val label = if (period == 0) "TOTAL GASTADO (ESTA SEMANA)" else "TOTAL GASTADO (ESTE MES)"
    val vsLabel = if (period == 0) "vs sem. ant." else "vs mes ant."

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "S/ ${String.format(Locale.US, "%.2f", totalAmount)}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (vsLastPercent == null) "Sin datos del período anterior"
                    else "Comparado al período anterior",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                if (vsLastPercent != null) {
                    val isUp = vsLastPercent >= 0
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
                            "${String.format(Locale.US, "%.0f", abs(vsLastPercent))}% $vsLabel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isUp) TuGastoRed else TuGastoGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChartCard(totalAmount: Double, breakdown: List<CategoryBreakdown>) {
    val chartColors = if (breakdown.isEmpty()) listOf(TuGastoGray200) else breakdown.map { it.color }
    val percentages = if (breakdown.isEmpty()) listOf(100f) else breakdown.map { it.percent }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "DISTRIBUCIÓN DE GASTOS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp
                )
            )
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.size(160.dp).align(Alignment.CenterHorizontally)) {
                DonutChart(percentages = percentages, colors = chartColors, modifier = Modifier.fillMaxSize())
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "S/ ${String.format(Locale.US, "%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text("total", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                breakdown.take(4).forEachIndexed { i, cat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(chartColors[i], CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            cat.name.split(" ").first(),
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(percentages: List<Float>, colors: List<Color>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        var startAngle = -90f
        percentages.zip(colors).forEach { (pct, color) ->
            val sweep = (pct / 100f) * 360f - 3f
            drawArc(
                color = color, startAngle = startAngle, sweepAngle = sweep, useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                topLeft = topLeft, size = Size(diameter, diameter)
            )
            startAngle += sweep + 3f
        }
    }
}

@Composable
private fun CategoryBreakdownSection(breakdown: List<CategoryBreakdown>, onDetailsClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "DESGLOSE POR CATEGORÍA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            TextButton(onClick = onDetailsClick) {
                Text("Ver detalles >", style = MaterialTheme.typography.labelMedium.copy(color = TuGastoBlue))
            }
        }
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (breakdown.isEmpty()) {
                    Text("No hay gastos registrados.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    breakdown.forEach { cat -> CategoryRow(cat) }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(cat: CategoryBreakdown) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(cat.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconForName(cat.iconName), contentDescription = cat.name, tint = cat.color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cat.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface))
            Text(
                "${String.format(Locale.US, "%.0f", cat.percent)}% DEL TOTAL",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.4.sp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "S/ ${String.format(Locale.US, "%.2f", cat.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )
            Spacer(Modifier.height(4.dp))
            val isOver = cat.percent > 50
            Text(
                if (isOver) "Revisar" else "En presupuesto",
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isOver) Color(0xFFFEF3C7) else TuGastoGreenLight)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isOver) Color(0xFF92400E) else TuGastoGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun CategoryChartCard(breakdown: List<CategoryBreakdown>) {
    val total = breakdown.sumOf { it.amount }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "GASTOS POR CATEGORÍA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp, fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(16.dp))
            breakdown.take(5).forEach { cat ->
                val ratio = if (total > 0) (cat.amount / total).toFloat() else 0f
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        cat.name.split(" ").first(),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.width(84.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = cat.color,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "S/ ${String.format(Locale.US, "%.0f", cat.amount)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CloudBackupBanner(onDismiss: () -> Unit, onCreateAccount: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Sin respaldo en la nube",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                "Tus datos solo existen en este dispositivo. Si desinstalas la app, se perderán.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 28.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = onCreateAccount,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Crear cuenta gratis", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProUpsellBanner(onUpgradeClick: () -> Unit) {
    ElevatedCard(
        onClick = onUpgradeClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TuGastoDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(TuGastoBlue.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Análisis Predictivo Pro", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Text("Anticípate a tus gastos del próximo mes", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)))
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
        }
    }
}

fun iconForName(name: String): ImageVector = when (name) {
    "local_dining" -> Icons.Default.LocalDining
    "directions_bus" -> Icons.Default.DirectionsBus
    "electric_bolt" -> Icons.Default.ElectricBolt
    "confirmation_number" -> Icons.Default.ConfirmationNumber
    "health_and_safety" -> Icons.Default.HealthAndSafety
    "school" -> Icons.Default.School
    "shopping_bag" -> Icons.Default.ShoppingBag
    "home" -> Icons.Default.Home
    "work" -> Icons.Default.Work
    else -> Icons.Default.Category
}
