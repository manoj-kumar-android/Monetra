package com.monetra.presentation.screen.monthly_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.BillInstance
import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.usecase.intelligence.*
import com.monetra.data.worker.PendingDeleteManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getBillInstances: GetBillInstancesUseCase,
    private val pendingDeleteManager: PendingDeleteManager
) : ViewModel() {

    private val selectedMonth = YearMonth.now()

    private val _expenses = getMonthlyExpenses()
    private val _instances = getBillInstances(selectedMonth)
    private val _pendingDeleteIds = pendingDeleteManager.getPendingIds("MONTHLY_EXPENSE")

    private val _uiState = MutableStateFlow(MonthlyExpenseUiState())
    val uiState: StateFlow<MonthlyExpenseUiState> = _uiState.asStateFlow()

    val billModels: StateFlow<List<BillUiModel>> = combine(_expenses, _instances, _pendingDeleteIds) { rules, instances, pendingIds ->
        rules
            .filter { it.id !in pendingIds } // Hide pending-delete items
            .map { rule ->
                BillUiModel(
                    rule = rule,
                    instance = instances.find { it.billId == rule.id }
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                    id = state.editingId ?: 0L,
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

    fun onEditExpense(expense: MonthlyExpense) {
        _uiState.update {
            it.copy(
                editingId = expense.id,
                name = expense.name,
                amount = if (expense.amount % 1.0 == 0.0) expense.amount.toInt().toString() else expense.amount.toString(),
                category = expense.category,
                dueDay = expense.dueDay,
                isAddSheetOpen = true,
                nameError = null,
                amountError = null
            )
        }
    }

    fun requestDelete(expense: MonthlyExpense) {
        _uiState.update { it.copy(pendingDeleteExpense = expense) }
        viewModelScope.launch {
            pendingDeleteManager.requestDelete(expense.id, expense.remoteId, "MONTHLY_EXPENSE")
        }
    }

    fun undoDelete() {
        val expense = _uiState.value.pendingDeleteExpense ?: return
        _uiState.update { it.copy(pendingDeleteExpense = null) }
        viewModelScope.launch {
            pendingDeleteManager.cancelDelete(expense.id, "MONTHLY_EXPENSE")
        }
    }

    fun toggleAddSheet(isOpen: Boolean) {
        _uiState.update { it.copy(isAddSheetOpen = isOpen) }
        if (!isOpen) _uiState.update { it.copy(editingId = null, name = "", amount = "", nameError = null, amountError = null, dueDay = 1) }
    }
}

data class MonthlyExpenseUiState(
    val editingId: Long? = null,
    val name: String = "",
    val amount: String = "",
    val category: String = "Bills",
    val dueDay: Int = 1,
    val isAddSheetOpen: Boolean = false,
    val nameError: String? = null,
    val amountError: String? = null,
    val pendingDeleteExpense: MonthlyExpense? = null
)
