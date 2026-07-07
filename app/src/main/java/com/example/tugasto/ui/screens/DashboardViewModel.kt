package com.example.tugasto.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.data.local.dao.TuGastoDao
import com.example.tugasto.data.local.entity.CategorySum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardState(
    val totalAmount: Double = 0.0,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList()
)

data class CategoryBreakdown(
    val name: String,
    val amount: Double,
    val percent: Float,
    val color: Color,
    val iconName: String
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dao: TuGastoDao
) : ViewModel() {

    val totalAmount: StateFlow<Double> = dao.getTotalAmount()
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val categoryBreakdown: StateFlow<List<CategoryBreakdown>> = dao.getAmountByCategory()
        .map { list ->
            val total = list.sumOf { it.totalAmount }
            if (total == 0.0) emptyList()
            else {
                list.map { sum ->
                    CategoryBreakdown(
                        name = sum.categoryName,
                        amount = sum.totalAmount,
                        percent = ((sum.totalAmount / total) * 100).toFloat(),
                        color = Color(android.graphics.Color.parseColor(sum.colorHex)),
                        iconName = sum.iconName
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
