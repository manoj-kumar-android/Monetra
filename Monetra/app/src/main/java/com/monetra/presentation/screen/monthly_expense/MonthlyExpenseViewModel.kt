package com.monetra.presentation.screen.monthly_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.BillInstance
import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.usecase.intelligence.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class BillUiModel(
    val rule: MonthlyExpense,
    val instance: BillInstance?
)

@HiltViewModel
class MonthlyExpenseViewModel @Inject constructor(
    private val getMonthlyExpenses: GetMonthlyExpensesUseCase,
    private val addMonthlyExpense: AddMonthlyExpenseUseCase,
    private val deleteMonthlyExpense: DeleteMonthlyExpenseUseCase,
    private val prepareMonthlyBills: PrepareMonthlyBillsUseCase,
    private val getBillInstances: GetBillInstancesUseCase
) : ViewModel() {

    private val selectedMonth = YearMonth.now()

    private val _expenses = getMonthlyExpenses()
    private val _instances = getBillInstances(selectedMonth)

    val billModels: StateFlow<List<BillUiModel>> = combine(_expenses, _instances) { rules, instances ->
        rules.map { rule ->
            BillUiModel(
                rule = rule,
                instance = instances.find { it.billId == rule.id }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(MonthlyExpenseUiState())
    val uiState: StateFlow<MonthlyExpenseUiState> = _uiState.asStateFlow()

    private var pendingDeleteItem: MonthlyExpense? = null
    private var deleteJob: Job? = null

    init {
        viewModelScope.launch {
            prepareMonthlyBills(selectedMonth)
        }
    }

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name, nameError = null) } }

    fun onAmountChange(amount: String) {
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(amount = sanitized, amountError = null) }
    }

    fun onCategoryChange(category: String) { _uiState.update { it.copy(category = category) } }
    
    fun onDueDayChange(day: Int) { 
        val sanitized = day.coerceIn(1, 31)
        _uiState.update { it.copy(dueDay = sanitized) } 
    }

    fun onSaveExpense() {
        val state = _uiState.value
        var hasError = false

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            addMonthlyExpense(
                MonthlyExpense(
                    name = state.name.trim(),
                    amount = amount!!,
                    category = state.category,
                    dueDay = state.dueDay
                )
            )
            prepareMonthlyBills(selectedMonth)
            
            _uiState.update { MonthlyExpenseUiState() }
            _uiState.update { it.copy(isAddSheetOpen = false) }
        }
    }

    fun requestDelete(expense: MonthlyExpense) {
        deleteJob?.cancel()
        pendingDeleteItem = null
        _uiState.update { it.copy(pendingDeleteExpense = null) }

        viewModelScope.launch { deleteMonthlyExpense(expense) }

        pendingDeleteItem = expense
        _uiState.update { it.copy(pendingDeleteExpense = expense) }

        deleteJob = viewModelScope.launch {
            delay(10000)
            pendingDeleteItem = null
            _uiState.update { it.copy(pendingDeleteExpense = null) }
        }
    }

    fun undoDelete() {
        deleteJob?.cancel()
        deleteJob = null
        val item = pendingDeleteItem ?: return
        pendingDeleteItem = null
        _uiState.update { it.copy(pendingDeleteExpense = null) }
        viewModelScope.launch { 
            addMonthlyExpense(item)
            prepareMonthlyBills(selectedMonth)
        }
    }

    fun toggleAddSheet(isOpen: Boolean) {
        _uiState.update { it.copy(isAddSheetOpen = isOpen) }
        if (!isOpen) _uiState.update { it.copy(name = "", amount = "", nameError = null, amountError = null, dueDay = 1) }
    }
}

data class MonthlyExpenseUiState(
    val name: String = "",
    val amount: String = "",
    val category: String = "Bills",
    val dueDay: Int = 1,
    val isAddSheetOpen: Boolean = false,
    val nameError: String? = null,
    val amountError: String? = null,
    val pendingDeleteExpense: MonthlyExpense? = null
)
