package com.monetra.presentation.screen.monthly_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.usecase.intelligence.AddMonthlyExpenseUseCase
import com.monetra.domain.usecase.intelligence.DeleteMonthlyExpenseUseCase
import com.monetra.domain.usecase.intelligence.GetMonthlyExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlyExpenseViewModel @Inject constructor(
    private val getMonthlyExpenses: GetMonthlyExpensesUseCase,
    private val addMonthlyExpense: AddMonthlyExpenseUseCase,
    private val deleteMonthlyExpense: DeleteMonthlyExpenseUseCase
) : ViewModel() {

    private val _expenses = getMonthlyExpenses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses: StateFlow<List<MonthlyExpense>> = _expenses

    private val _uiState = MutableStateFlow(MonthlyExpenseUiState())
    val uiState: StateFlow<MonthlyExpenseUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name) } }
    fun onAmountChange(amount: String) { _uiState.update { it.copy(amount = amount) } }
    fun onCategoryChange(category: String) { _uiState.update { it.copy(category = category) } }

    fun onSaveExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            addMonthlyExpense(
                MonthlyExpense(
                    name = state.name,
                    amount = amount,
                    category = state.category
                )
            )
            _uiState.update { MonthlyExpenseUiState() } // Reset
            _uiState.update { it.copy(isAddSheetOpen = false) }
        }
    }

    fun onDeleteExpense(expense: MonthlyExpense) {
        viewModelScope.launch {
            deleteMonthlyExpense(expense)
        }
    }

    fun toggleAddSheet(isOpen: Boolean) {
        _uiState.update { it.copy(isAddSheetOpen = isOpen) }
    }
}

data class MonthlyExpenseUiState(
    val name: String = "",
    val amount: String = "",
    val category: String = "Utility",
    val isAddSheetOpen: Boolean = false
)
