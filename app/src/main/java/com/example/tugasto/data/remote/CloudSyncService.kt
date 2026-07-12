package com.example.tugasto.data.remote

import com.example.tugasto.data.local.dao.TuGastoDao
import com.example.tugasto.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncService @Inject constructor(
    private val supabase: SupabaseService,
    private val dao: TuGastoDao
) {
    suspend fun backup(): Result<Int> = runCatching {
        val userId = supabase.userId ?: error("No hay sesión activa")

        val transactions = dao.getAllTransactions().first()
        val categories = dao.getAllCategories().first()
        val catMap = categories.associateBy { it.id }

        val rows = JSONArray()
        transactions.forEach { tx ->
            rows.put(JSONObject().apply {
                put("user_id", userId)
                put("amount", tx.amount)
                put("description", tx.description)
                put("category_name", catMap[tx.categoryId]?.name ?: "Otros")
                put("timestamp", tx.timestamp)
                put("type", tx.type)
            })
        }

        supabase.deleteUserTransactions().getOrThrow()
        if (rows.length() > 0) supabase.insertTransactions(rows).getOrThrow()

        transactions.size
    }

    suspend fun restore(): Result<Int> = runCatching {
        val rows = supabase.fetchUserTransactions().getOrThrow()

        dao.deleteAllTransactions()

        for (i in 0 until rows.length()) {
            val rt = rows.getJSONObject(i)
            val catName = rt.optString("category_name", "Otros")
            val catId = dao.getCategoryByName(catName)?.id ?: 1
            dao.insertTransaction(
                TransactionEntity(
                    amount = rt.getDouble("amount"),
                    description = rt.getString("description"),
                    categoryId = catId,
                    timestamp = rt.getLong("timestamp"),
                    type = rt.optString("type", "EXPENSE")
                )
            )
        }

        rows.length()
    }
}
