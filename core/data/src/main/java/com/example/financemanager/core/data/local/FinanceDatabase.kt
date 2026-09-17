package com.example.financemanager.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.financemanager.core.data.local.dao.BudgetDao
import com.example.financemanager.core.data.local.dao.TransactionDao
import com.example.financemanager.core.data.local.entities.BudgetEntity
import com.example.financemanager.core.data.local.entities.TransactionEntity

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
}