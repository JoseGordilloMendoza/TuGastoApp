package com.example.tugasto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tugasto.data.local.entity.CategoryEntity
import com.example.tugasto.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TuGastoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE isConfirmed = 1 ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE isConfirmed = 1")
    fun getTransactionCount(): Flow<Int>

    @Query("SELECT MIN(timestamp) FROM transactions WHERE isConfirmed = 1")
    fun getFirstTransactionTimestamp(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Query("SELECT SUM(amount) FROM transactions WHERE isConfirmed = 1")
    fun getTotalAmount(): Flow<Double?>

    @Query("""
        SELECT c.id as categoryId, c.name as categoryName, c.colorHex, c.iconName, SUM(t.amount) as totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.isConfirmed = 1
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    fun getAmountByCategory(): Flow<List<com.example.tugasto.data.local.entity.CategorySum>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("SELECT SUM(amount) FROM transactions WHERE isConfirmed = 1 AND timestamp >= :startTs")
    fun getTotalAmountFrom(startTs: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isConfirmed = 1 AND timestamp >= :startTs AND timestamp < :endTs")
    fun getTotalAmountBetween(startTs: Long, endTs: Long): Flow<Double?>

    @Query("""
        SELECT c.id as categoryId, c.name as categoryName, c.colorHex, c.iconName, SUM(t.amount) as totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.isConfirmed = 1 AND t.timestamp >= :startTs
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    fun getAmountByCategoryFrom(startTs: Long): Flow<List<com.example.tugasto.data.local.entity.CategorySum>>

    @Query("SELECT * FROM transactions WHERE isConfirmed = 0 ORDER BY timestamp DESC")
    fun getPendingTransactions(): Flow<List<TransactionEntity>>
    
    @Query("UPDATE transactions SET isConfirmed = 1, categoryId = :categoryId, description = :newDescription WHERE id = :transactionId")
    suspend fun confirmTransaction(transactionId: Int, categoryId: Int, newDescription: String)
    
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: Int)
}
