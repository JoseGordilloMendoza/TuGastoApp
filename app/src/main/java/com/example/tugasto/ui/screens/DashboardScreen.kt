package com.example.tugasto.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.tugasto.ui.theme.TuGastoBg
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoBlueExtraLight
import com.example.tugasto.ui.theme.TuGastoDark
import com.example.tugasto.ui.theme.TuGastoGray100
import com.example.tugasto.ui.theme.TuGastoGray200
import com.example.tugasto.ui.theme.TuGastoGray400
import com.example.tugasto.ui.theme.TuGastoGray500
import com.example.tugasto.ui.theme.TuGastoGray700
import com.example.tugasto.ui.theme.TuGastoGray900
import com.example.tugasto.ui.theme.TuGastoGreen
import com.example.tugasto.ui.theme.TuGastoGreenDark
import com.example.tugasto.ui.theme.TuGastoGreenLight
import com.example.tugasto.ui.theme.TuGastoOrange

private data class CategoryItem(
    val name: String,
    val percent: Int,
    val amount: String,
    val color: Color,
    val icon: ImageVector,
    val badge: String
)

private val categories = listOf(
    CategoryItem("Alimentación", 36, "S/ 450", TuGastoBlue, Icons.Default.LocalDining, "En presupuesto"),
    CategoryItem("Transporte", 24, "S/ 300", TuGastoOrange, Icons.Default.DirectionsBus, "En presupuesto"),
    CategoryItem("Servicios", 20, "S/ 250", TuGastoGreen, Icons.Default.ElectricBolt, "En presupuesto"),
    CategoryItem("Entretenimiento", 20, "S/ 250", Color(0xFF8B5CF6), Icons.Default.ConfirmationNumber, "Revisar"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    analysisViewModel: AnalysisViewModel = hiltViewModel(),
    onUpgradeClick: () -> Unit = {},
    onDetailsClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(1) }
    
    val totalAmount by viewModel.totalAmount.collectAsState()
    val breakdown by viewModel.categoryBreakdown.collectAsState()
    val categorySums by analysisViewModel.categorySums.collectAsState()

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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item { TimeSelector(selectedTab) { selectedTab = it } }

            item { TotalGastadoCard(totalAmount) }

            item { DonutChartCard(totalAmount, breakdown) }

            item { CategoryBreakdownSection(breakdown, onDetailsClick) }

            if (categorySums.isNotEmpty()) {
                item { CategoryChartCard(categorySums) }
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalGastadoCard(totalAmount: Double) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "TOTAL GASTADO (ESTE MES)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "S/ ${String.format(java.util.Locale.US, "%.2f", totalAmount)}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Actualizado hace 5 minutos",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TuGastoGreenLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TuGastoGreenDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "8% vs mes anterior",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TuGastoGreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
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
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                DonutChart(
                    percentages = percentages,
                    colors = chartColors,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "S/ ${String.format(java.util.Locale.US, "%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        "total",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                breakdown.forEachIndexed { i, cat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(chartColors[i], CircleShape)
                        )
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
private fun DonutChart(
    percentages: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        var startAngle = -90f

        percentages.zip(colors).forEach { (pct, color) ->
            val sweep = (pct / 100f) * 360f - 3f
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                topLeft = topLeft,
                size = Size(diameter, diameter)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp, fontWeight = FontWeight.SemiBold
                )
            )
            TextButton(onClick = onDetailsClick) {
                Text(
                    "Ver detalles >",
                    style = MaterialTheme.typography.labelMedium.copy(color = TuGastoBlue)
                )
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
    val icon = when (cat.iconName) {
        "restaurant" -> Icons.Default.LocalDining
        "directions_bus" -> Icons.Default.DirectionsBus
        "movie" -> Icons.Default.ConfirmationNumber
        "home" -> Icons.Default.ElectricBolt
        else -> Icons.Default.LocalDining
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = cat.name, tint = cat.color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cat.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                "${String.format(java.util.Locale.US, "%.0f", cat.percent)}% DEL TOTAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.4.sp
                )
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "S/ ${String.format(java.util.Locale.US, "%.2f", cat.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(Modifier.height(4.dp))
            val badgeText = if (cat.percent > 50) "Revisar" else "En presupuesto"
            val badgeColor = if (badgeText == "En presupuesto") TuGastoGreenDark else Color(0xFF92400E)
            val badgeBg = if (badgeText == "En presupuesto") TuGastoGreenLight else Color(0xFFFEF3C7)
            Text(
                badgeText,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = badgeColor, fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProUpsellBanner(onUpgradeClick: () -> Unit) {
    ElevatedCard(
        onClick = onUpgradeClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TuGastoDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TuGastoBlue.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Análisis Predictivo Pro",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    "Anticípate a tus gastos del próximo mes",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                )
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun CategoryChartCard(categorySums: List<com.example.tugasto.data.local.entity.CategorySum>) {
    val total = categorySums.sumOf { it.totalAmount }
    
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
            
            categorySums.take(4).forEach { catSum ->
                val ratio = if (total > 0) (catSum.totalAmount / total).toFloat() else 0f
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(catSum.colorHex))
                } catch (e: Exception) {
                    TuGastoBlue
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        catSum.categoryName,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = parsedColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "S/ ${String.format(java.util.Locale.US, "%.0f", catSum.totalAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}
