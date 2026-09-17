package com.example.financemanager.core.data.repository

import com.example.financemanager.core.data.local.entities.BudgetEntity
import com.example.financemanager.core.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    suspend fun addTransaction(transaction: TransactionEntity)
    fun getBudgetForMonth(monthYear: String): Flow<BudgetEntity?>
    suspend fun setBudget(budget: BudgetEntity)
}