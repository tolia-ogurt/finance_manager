package com.example.financemanager.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.core.data.local.entities.BudgetEntity
import com.example.financemanager.core.data.local.entities.TransactionEntity
import com.example.financemanager.core.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val monthlyBudget: Double = 0.0,
    val spentAmount: Double = 0.0,
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isAddTransactionSheetVisible: Boolean = false,
    val isAdjustBudgetDialogVisible: Boolean = false,
    val isDatePickerVisible: Boolean = false,
    val dateLabel: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _dateRange = MutableStateFlow(getCurrentMonthRange())

    private val _uiState = _dateRange.flatMapLatest { range ->
        combine(
            repository.getBudgetForMonth(getBudgetMonthKey(range.first)),
            repository.getTransactionsInRange(range.first, range.second)
        ) { budget, transactions ->
            val spent = transactions.sumOf { it.amount }
            DashboardUiState(
                monthlyBudget = budget?.monthlyLimit ?: 6000.0,
                spentAmount = spent,
                transactions = transactions,
                isLoading = false,
                dateLabel = formatDateLabel(range.first, range.second)
            )
        }
    }

    private val _navigationState = MutableStateFlow(
        Triple(false, false, false) // (isSheet, isDialog, isPicker)
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        _uiState,
        _navigationState
    ) { state, nav ->
        state.copy(
            isAddTransactionSheetVisible = nav.first,
            isAdjustBudgetDialogVisible = nav.second,
            isDatePickerVisible = nav.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    private fun getBudgetMonthKey(timestamp: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = timestamp
        return "${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.YEAR)}"
    }

    private fun formatDateLabel(start: Long, end: Long): String {
        // Use UTC for formatting since DatePicker returns UTC timestamps
        val startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = start }
        val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = end }
        
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val dayFormat = SimpleDateFormat("MMM d", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }

        val isStartOfMonth = startCal.get(Calendar.DAY_OF_MONTH) == 1
        val isEndOfMonth = endCal.get(Calendar.DAY_OF_MONTH) == startCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val isSameMonth = startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH) && 
                         startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)

        return when {
            isSameMonth && isStartOfMonth && isEndOfMonth -> {
                monthFormat.format(startCal.time)
            }
            startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR) && 
            startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) -> {
                // Single day selection
                "${dayFormat.format(startCal.time)}, ${yearFormat.format(startCal.time)}"
            }
            else -> {
                "${dayFormat.format(startCal.time)} - ${dayFormat.format(endCal.time)}, ${yearFormat.format(startCal.time)}"
            }
        }
    }

    fun showDatePicker(show: Boolean) {
        _navigationState.value = _navigationState.value.copy(third = show)
    }

    fun setDateRange(start: Long?, end: Long?) {
        if (start != null && end != null) {
            // Adjust end to the end of the day to include all transactions on that day
            val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = end
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            _dateRange.value = Pair(start, endCal.timeInMillis)
        }
        showDatePicker(false)
    }

    fun selectEntireMonth(displayedMonthMillis: Long) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = displayedMonthMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        
        _dateRange.value = Pair(start, end)
        showDatePicker(false)
    }

    fun showAddTransaction(show: Boolean) {
        _navigationState.value = _navigationState.value.copy(first = show)
    }

    fun showAdjustBudget(show: Boolean) {
        _navigationState.value = _navigationState.value.copy(second = show)
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            val key = getBudgetMonthKey(_dateRange.value.first)
            repository.setBudget(BudgetEntity(monthYear = key, monthlyLimit = amount))
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