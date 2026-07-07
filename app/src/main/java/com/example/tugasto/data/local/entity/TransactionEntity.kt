package com.example.tugasto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val description: String,
    val categoryId: Int,
    val timestamp: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val isConfirmed: Boolean = true
)
