package com.example.financemanager.core.data.repository

import com.example.financemanager.core.data.local.dao.BudgetDao
import com.example.financemanager.core.data.local.dao.TransactionDao
import com.example.financemanager.core.data.local.entities.BudgetEntity
import com.example.financemanager.core.data.local.entities.TransactionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FinanceRepositoryImplTest {

    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var repository: FinanceRepositoryImpl

    @Before
    fun setup() {
        transactionDao = mockk()
        budgetDao = mockk()
        repository = FinanceRepositoryImpl(transactionDao, budgetDao)
    }

    @Test
    fun `getAllTransactions returns transactions from DAO`() = runTest {
        val transactions = listOf(
            TransactionEntity(id = 1, amount = 100.0, category = "Food", timestamp = 123L, note = "Lunch")
        )
        every { transactionDao.getAllTransactions() } returns flowOf(transactions)

        val result = repository.getAllTransactions().first()

        assertEquals(transactions, result)
    }

    @Test
    fun `getTransactionsInRange returns filtered transactions from DAO`() = runTest {
        val transactions = listOf(
            TransactionEntity(id = 1, amount = 100.0, category = "Food", timestamp = 150L, note = "Lunch")
        )
        every { transactionDao.getTransactionsInRange(100L, 200L) } returns flowOf(transactions)

        val result = repository.getTransactionsInRange(100L, 200L).first()

        assertEquals(transactions, result)
    }

    @Test
    fun `addTransaction calls DAO insert`() = runTest {
        val transaction = TransactionEntity(amount = 50.0, category = "Transport", timestamp = 456L, note = "Bus")
        coEvery { transactionDao.insertTransaction(any()) } returns Unit

        repository.addTransaction(transaction)

        coVerify { transactionDao.insertTransaction(transaction) }
    }

    @Test
    fun `getBudgetForMonth returns budget from DAO`() = runTest {
        val budget = BudgetEntity(monthYear = "09-2026", monthlyLimit = 5000.0)
        every { budgetDao.getBudgetForMonth("09-2026") } returns flowOf(budget)

        val result = repository.getBudgetForMonth("09-2026").first()

        assertEquals(budget, result)
    }

    @Test
    fun `setBudget calls DAO insert`() = runTest {
        val budget = BudgetEntity(monthYear = "10-2026", monthlyLimit = 4000.0)
        coEvery { budgetDao.insertBudget(any()) } returns Unit

        repository.setBudget(budget)

        coVerify { budgetDao.insertBudget(budget) }
    }
}