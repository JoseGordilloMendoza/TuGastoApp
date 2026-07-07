package com.example.tugasto.di

import android.app.Application
import androidx.room.Room
import com.example.tugasto.data.local.TuGastoDatabase
import com.example.tugasto.data.local.dao.TuGastoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): TuGastoDatabase {
        return Room.databaseBuilder(
            app,
            TuGastoDatabase::class.java,
            "tugasto_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTuGastoDao(db: TuGastoDatabase): TuGastoDao {
        return db.dao
    }
}
