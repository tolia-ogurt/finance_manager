package com.example.financemanager.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.core.data.local.entities.BudgetEntity
import com.example.financemanager.core.data.local.entities.TransactionEntity
import com.example.financemanager.core.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val monthlyBudget: Double = 0.0,
    val spentAmount: Double = 0.0,
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isAddTransactionSheetVisible: Boolean = false,
    val isAdjustBudgetDialogVisible: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _uiState = combine(
        repository.getBudgetForMonth(currentMonthYear()),
        repository.getAllTransactions()
    ) { budget, transactions ->
        val spent = transactions.sumOf { it.amount }
        DashboardUiState(
            monthlyBudget = budget?.monthlyLimit ?: 6000.0,
            spentAmount = spent,
            transactions = transactions,
            isLoading = false
        )
    }

    private val _navigationState = MutableStateFlow(
        Pair(false, false)
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        _uiState,
        _navigationState
    ) { state, nav ->
        state.copy(
            isAddTransactionSheetVisible = nav.first,
            isAdjustBudgetDialogVisible = nav.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private fun currentMonthYear(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.YEAR)}"
    }

    fun showAddTransaction(show: Boolean) {
        _navigationState.value = _navigationState.value.copy(first = show)
    }

    fun showAdjustBudget(show: Boolean) {
        _navigationState.value = _navigationState.value.copy(second = show)
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            repository.setBudget(BudgetEntity(monthYear = currentMonthYear(), monthlyLimit = amount))
            showAdjustBudget(false)
        }
    }

    fun addTransaction(amount: Double, category: String, note: String) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    amount = amount,
                    category = category,
                    timestamp = System.currentTimeMillis(),
                    note = note
                )
            )
            showAddTransaction(false)
        }
    }
}