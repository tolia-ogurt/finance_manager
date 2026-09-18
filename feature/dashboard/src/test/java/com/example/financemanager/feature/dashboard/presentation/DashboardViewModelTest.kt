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
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var repository: FinanceRepository
    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        // Force US Locale for consistent test results
        Locale.setDefault(Locale.US)
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        
        // Default mocks for initial state
        every { repository.getBudgetForMonth(any()) } returns flowOf(null)
        every { repository.getTransactionsInRange(any(), any()) } returns flowOf(emptyList())
        
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
        every { repository.getTransactionsInRange(any(), any()) } returns flowOf(transactions)

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
            awaitItem() // Initial
            
            viewModel.showAddTransaction(true)
            assertTrue(awaitItem().isAddTransactionSheetVisible)

            viewModel.showAddTransaction(false)
            assertFalse(awaitItem().isAddTransactionSheetVisible)
        }
    }

    @Test
    fun `setDateRange updates dateLabel and closes picker`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial

            // Select a specific range (UTC)
            val start = 1725148800000L // Sep 1, 2024 00:00 UTC
            val end = 1725235199000L   // Sep 1, 2024 23:59 UTC (Single day)
            
            viewModel.setDateRange(start, end)
            
            val state = awaitItem()
            // Should be "September 1, 2024" or contain it
            assertTrue("Actual label: ${state.dateLabel}", state.dateLabel.contains("Sep"))
            assertTrue("Actual label: ${state.dateLabel}", state.dateLabel.contains("2024"))
            assertFalse(state.isDatePickerVisible)
        }
    }

    @Test
    fun `selectEntireMonth updates dateLabel to month name`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial

            val sepMillis = 1725148800000L // Some time in Sep 2024
            viewModel.selectEntireMonth(sepMillis)
            
            val state = awaitItem()
            assertEquals("September 2024", state.dateLabel)
            assertFalse(state.isDatePickerVisible)
        }
    }

    @Test
    fun `setBudget calls repository with correct month key`() = runTest {
        coEvery { repository.setBudget(any()) } returns Unit
        
        // Ensure we are in a known month
        val sepStart = 1725148800000L // Sep 1, 2024 UTC
        val sepEnd = 1727654399999L   // Sep 30, 2024 UTC
        viewModel.setDateRange(sepStart, sepEnd)
        
        viewModel.setBudget(7000.0)

        coVerify { repository.setBudget(match { 
            it.monthlyLimit == 7000.0 && it.monthYear == "9-2024" 
        }) }
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