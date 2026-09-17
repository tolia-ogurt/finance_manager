package com.example.financemanager.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.financemanager.core.data.local.FinanceDatabase
import com.example.financemanager.core.data.local.dao.BudgetDao
import com.example.financemanager.core.data.local.dao.TransactionDao
import com.example.financemanager.core.data.repository.FinanceRepository
import com.example.financemanager.core.data.repository.FinanceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFinanceRepository(
        financeRepositoryImpl: FinanceRepositoryImpl
    ): FinanceRepository

    companion object {
        @Provides
        @Singleton
        fun provideFinanceDatabase(
            @ApplicationContext context: Context
        ): FinanceDatabase {
            return Room.databaseBuilder(
                context,
                FinanceDatabase::class.java,
                "finance_db"
            ).fallbackToDestructiveMigration()
                .build()
        }

        @Provides
        fun provideTransactionDao(database: FinanceDatabase): TransactionDao {
            return database.transactionDao()
        }

        @Provides
        fun provideBudgetDao(database: FinanceDatabase): BudgetDao {
            return database.budgetDao()
        }
    }
}