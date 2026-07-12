package com.example.tugasto.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.data.local.dao.TuGastoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class CategoryBreakdown(
    val name: String,
    val amount: Double,
    val percent: Float,
    val color: Color,
    val iconName: String
)

data class PeriodStats(
    val totalAmount: Double = 0.0,
    val vsLastPercent: Double? = null,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList()
)

// 0 = SEMANA, 1 = MES
private fun periodBounds(period: Int): Triple<Long, Long, Long> {
    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance()

    return when (period) {
        0 -> { // Esta semana vs semana anterior
            val weekAgo = now - 7L * 24 * 60 * 60 * 1000
            val twoWeeksAgo = now - 14L * 24 * 60 * 60 * 1000
            Triple(weekAgo, twoWeeksAgo, weekAgo)
        }
        else -> { // Este mes vs mes anterior
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val thisMonthStart = cal.timeInMillis

            cal.add(Calendar.MONTH, -1)
            val lastMonthStart = cal.timeInMillis

            Triple(thisMonthStart, lastMonthStart, thisMonthStart)
        }
    }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dao: TuGastoDao
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(1)
    val selectedPeriod: StateFlow<Int> = _selectedPeriod

    fun setPeriod(period: Int) {
        _selectedPeriod.value = period
    }

    val periodStats: StateFlow<PeriodStats> = _selectedPeriod.flatMapLatest { period ->
        val (currentStart, prevStart, prevEnd) = periodBounds(period)
        combine(
            dao.getTotalAmountFrom(currentStart),
            dao.getTotalAmountBetween(prevStart, prevEnd),
            dao.getAmountByCategoryFrom(currentStart)
        ) { current, previous, cats ->
            val total = current ?: 0.0
            val prevTotal = previous ?: 0.0
            val vsLast = if (prevTotal > 0.0) ((total - prevTotal) / prevTotal) * 100.0 else null
            val breakdown = if (total == 0.0) emptyList()
            else cats.map { sum ->
                CategoryBreakdown(
                    name = sum.categoryName,
                    amount = sum.totalAmount,
                    percent = ((sum.totalAmount / total) * 100).toFloat(),
                    color = try { Color(android.graphics.Color.parseColor(sum.colorHex)) }
                            catch (e: Exception) { Color.Gray },
                    iconName = sum.iconName
                )
            }
            PeriodStats(total, vsLast, breakdown)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PeriodStats()
    )
}
