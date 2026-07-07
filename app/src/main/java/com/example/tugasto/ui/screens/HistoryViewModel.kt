package com.example.tugasto.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.data.local.dao.TuGastoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TransactionDisplay(
    val id: Int,
    val description: String,
    val amount: Double,
    val timestamp: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dao: TuGastoDao
) : ViewModel() {

    val displayTransactions: StateFlow<List<TransactionDisplay>> = combine(
        dao.getAllTransactions(),
        dao.getAllCategories()
    ) { txs, cats ->
        val catMap = cats.associateBy { it.id }
        txs.map { tx ->
            val cat = catMap[tx.categoryId]
            TransactionDisplay(
                id = tx.id,
                description = tx.description,
                amount = tx.amount,
                timestamp = tx.timestamp,
                categoryName = cat?.name ?: "Otros",
                categoryIcon = cat?.iconName ?: "category",
                categoryColor = cat?.colorHex ?: "#6B7280"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
