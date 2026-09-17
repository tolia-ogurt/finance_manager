package com.example.financemanager.core.data.repository

import com.example.financemanager.core.data.local.dao.BudgetDao
import com.example.financemanager.core.data.local.dao.TransactionDao
import com.example.financemanager.core.data.local.entities.BudgetEntity
import com.example.financemanager.core.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FinanceRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) : FinanceRepository {
    override fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    
    override suspend fun addTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    override fun getBudgetForMonth(monthYear: String): Flow<BudgetEntity?> = budgetDao.getBudgetForMonth(monthYear)

    override suspend fun setBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }
}