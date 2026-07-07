package com.example.tugasto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tugasto.data.local.dao.TuGastoDao
import com.example.tugasto.data.local.entity.CategoryEntity
import com.example.tugasto.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TuGastoDatabase : RoomDatabase() {
    abstract val dao: TuGastoDao
}
