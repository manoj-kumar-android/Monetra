package com.monetra.presentation.screen.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Loan
import com.monetra.domain.usecase.loan.AddLoanUseCase
import com.monetra.domain.usecase.loan.DeleteLoanUseCase
import com.monetra.domain.usecase.loan.GetLoansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val getLoans: GetLoansUseCase,
    private val addLoan: AddLoanUseCase,
    private val deleteLoan: DeleteLoanUseCase
) : ViewModel() {

    private val _loans = getLoans().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val loans: StateFlow<List<Loan>> = _loans

    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name, nameError = null) } }
    fun onPrincipalChange(amount: String) { 
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(principal = sanitized, principalError = null) } 
    }
    fun onEmiChange(amount: String) { 
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(emi = sanitized, emiError = null) } 
    }
    fun onTenureChange(months: String) { 
        val sanitized = months.filter { it.isDigit() }
        _uiState.update { it.copy(tenure = sanitized, tenureError = null) } 
    }
    fun onCategoryChange(category: String) { _uiState.update { it.copy(category = category) } }

    fun onSaveLoan() {
        val state = _uiState.value
        
        var hasError = false
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Please enter a name") }
            hasError = true
        }
        
        val principal = state.principal.toDoubleOrNull() ?: 0.0
        if (principal <= 0) {
            _uiState.update { it.copy(principalError = "Invalid principal") }
            hasError = true
        }

        val emi = state.emi.toDoubleOrNull() ?: 0.0
        if (emi <= 0) {
            _uiState.update { it.copy(emiError = "Invalid EMI") }
            hasError = true
        } else if (emi > principal) {
             _uiState.update { it.copy(emiError = "EMI cannot exceed principal") }
             hasError = true
        }

        val tenure = state.tenure.toIntOrNull() ?: 0
        if (tenure <= 0) {
            _uiState.update { it.copy(tenureError = "Invalid tenure") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            addLoan(
                Loan(
                    name = state.name,
                    totalPrincipal = principal,
                    monthlyEmi = emi,
                    startDate = LocalDate.now(),
                    tenureMonths = tenure,
                    remainingTenure = tenure,
                    category = state.category
                )
            )
            _uiState.update { LoanUiState() } // Reset
            _uiState.update { it.copy(isAddSheetOpen = false) }
        }
    }

    fun onDeleteLoan(loan: Loan) {
        viewModelScope.launch {
            deleteLoan(loan)
        }
    }

    fun toggleAddSheet(isOpen: Boolean) {
        _uiState.update { it.copy(isAddSheetOpen = isOpen) }
    }
}

data class LoanUiState(
    val name: String = "",
    val principal: String = "",
    val emi: String = "",
    val tenure: String = "",
    val category: String = "Personal Loan",
    val isAddSheetOpen: Boolean = false,
    val nameError: String? = null,
    val principalError: String? = null,
    val emiError: String? = null,
    val tenureError: String? = null
)
