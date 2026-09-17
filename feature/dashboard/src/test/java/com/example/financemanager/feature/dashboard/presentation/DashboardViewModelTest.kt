package com.example.financemanager.feature.dashboard.presentation

import app.cash.turbine.test
import com.example.financemanager.core.data.local.entities.BudgetEntity
import com.example.financemanager.core.data.local.entities.TransactionEntity
import com.example.financemanager.core.data.repository.FinanceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var repository: FinanceRepository
    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        
        // Default mocks for initial state
        every { repository.getBudgetForMonth(any()) } returns flowOf(null)
        every { repository.getAllTransactions() } returns flowOf(emptyList())
        
        viewModel = DashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default budget`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(6000.0, state.monthlyBudget, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState updates when repository emits data`() = runTest {
        val budget = BudgetEntity(monthYear = "9-2026", monthlyLimit = 5000.0)
        val transactions = listOf(
            TransactionEntity(amount = 200.0, category = "Food", timestamp = 0L, note = "")
        )
        
        every { repository.getBudgetForMonth(any()) } returns flowOf(budget)
        every { repository.getAllTransactions() } returns flowOf(transactions)

        // Re-create ViewModel to pick up new flow emissions immediately
        val newViewModel = DashboardViewModel(repository)

        newViewModel.uiState.test {
            val state = awaitItem()
            assertEquals(5000.0, state.monthlyBudget, 0.0)
            assertEquals(200.0, state.spentAmount, 0.0)
            assertEquals(1, state.transactions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showAddTransaction updates state`() = runTest {
        viewModel.uiState.test {
            // Initial item
            awaitItem()
            
            viewModel.showAddTransaction(true)
            assertTrue(awaitItem().isAddTransactionSheetVisible)

            viewModel.showAddTransaction(false)
            assertFalse(awaitItem().isAddTransactionSheetVisible)
        }
    }

    @Test
    fun `setBudget calls repository and closes dialog`() = runTest {
        coEvery { repository.setBudget(any()) } returns Unit
        
        viewModel.setBudget(7000.0)

        coVerify { repository.setBudget(match { it.monthlyLimit == 7000.0 }) }
        assertFalse(viewModel.uiState.value.isAdjustBudgetDialogVisible)
    }

    @Test
    fun `addTransaction calls repository and closes sheet`() = runTest {
        coEvery { repository.addTransaction(any()) } returns Unit
        
        viewModel.addTransaction(150.0, "Transport", "Bus")

        coVerify { 
            repository.addTransaction(match { 
                it.amount == 150.0 && it.category == "Transport" && it.note == "Bus" 
            }) 
        }
        assertFalse(viewModel.uiState.value.isAddTransactionSheetVisible)
    }
}