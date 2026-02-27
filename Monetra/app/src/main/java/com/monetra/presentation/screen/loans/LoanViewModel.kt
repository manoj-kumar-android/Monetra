package com.monetra.presentation.screen.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Loan
import com.monetra.domain.usecase.loan.AddLoanUseCase
import com.monetra.domain.usecase.loan.DeleteLoanUseCase
import com.monetra.domain.usecase.loan.GetLoansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

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

    // Undo-delete staging
    private var pendingDeleteLoan: Loan? = null
    private var deleteJob: Job? = null

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onPrincipalChange(amount: String) {
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(principal = sanitized, principalError = null) }
        recalculateEmi()
    }

    fun onInterestRateChange(rate: String) {
        val sanitized = rate.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        // Cap at 100%
        val value = sanitized.toDoubleOrNull()
        if (value != null && value > 100) return
        _uiState.update { it.copy(interestRate = sanitized, interestRateError = null) }
        recalculateEmi()
    }

    fun onTenureChange(months: String) {
        val sanitized = months.filter { it.isDigit() }
        _uiState.update { it.copy(tenure = sanitized, tenureError = null) }
        recalculateEmi()
    }

    fun onStartDateChange(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
        // Also recalc remaining tenure since it depends on start date
        recalculateEmi()
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    /** Reactively recalculate EMI whenever inputs change */
    private fun recalculateEmi() {
        val state = _uiState.value
        val principal = state.principal.toDoubleOrNull() ?: 0.0
        val rate = state.interestRate.toDoubleOrNull() ?: 0.0
        val tenure = state.tenure.toIntOrNull() ?: 0
        val emi = Loan.calculateEmi(principal, rate, tenure)
        _uiState.update { it.copy(calculatedEmi = emi) }
    }

    fun onSaveLoan() {
        val state = _uiState.value
        var hasError = false

        // --- Validations ---
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Loan name is required") }
            hasError = true
        } else if (state.name.trim().length < 2) {
            _uiState.update { it.copy(nameError = "Name must be at least 2 characters") }
            hasError = true
        }

        val principal = state.principal.toDoubleOrNull()
        when {
            principal == null || state.principal.isBlank() ->
                _uiState.update { it.copy(principalError = "Enter the total loan amount") }.also { hasError = true }
            principal <= 0 ->
                _uiState.update { it.copy(principalError = "Amount must be greater than ₹0") }.also { hasError = true }
            principal > 100_000_000 ->
                _uiState.update { it.copy(principalError = "Amount seems too large (max ₹10 Cr)") }.also { hasError = true }
        }

        val rate = state.interestRate.toDoubleOrNull()
        when {
            rate == null || state.interestRate.isBlank() ->
                _uiState.update { it.copy(interestRateError = "Enter interest rate (0 for no interest)") }.also { hasError = true }
            rate < 0 ->
                _uiState.update { it.copy(interestRateError = "Rate cannot be negative") }.also { hasError = true }
            rate > 100 ->
                _uiState.update { it.copy(interestRateError = "Rate cannot exceed 100%") }.also { hasError = true }
        }

        val tenure = state.tenure.toIntOrNull()
        when {
            tenure == null || state.tenure.isBlank() ->
                _uiState.update { it.copy(tenureError = "Enter loan tenure in months") }.also { hasError = true }
            tenure <= 0 ->
                _uiState.update { it.copy(tenureError = "Tenure must be at least 1 month") }.also { hasError = true }
            tenure > 600 ->
                _uiState.update { it.copy(tenureError = "Tenure cannot exceed 600 months (50 years)") }.also { hasError = true }
        }

        if (hasError) return

        // Calculate remaining tenure from start date
        val monthsElapsed = ChronoUnit.MONTHS.between(state.startDate, LocalDate.now()).toInt().coerceAtLeast(0)
        val remainingTenure = (tenure!! - monthsElapsed).coerceAtLeast(0)

        // Final EMI calculation
        val emi = Loan.calculateEmi(principal!!, rate!!, tenure)

        viewModelScope.launch {
            addLoan(
                Loan(
                    name = state.name.trim(),
                    totalPrincipal = principal,
                    annualInterestRate = rate,
                    monthlyEmi = emi,
                    startDate = state.startDate,
                    tenureMonths = tenure,
                    remainingTenure = remainingTenure,
                    category = state.category
                )
            )
            _uiState.update { LoanUiState() }
            _uiState.update { it.copy(isAddSheetOpen = false) }
        }
    }

    fun requestDeleteLoan(loan: Loan) {
        deleteJob?.cancel()
        pendingDeleteLoan = null
        _uiState.update { it.copy(pendingDeleteLoan = null) }

        // Delete immediately from DB so the list refreshes cleanly
        viewModelScope.launch { deleteLoan(loan) }

        // Keep for undo
        pendingDeleteLoan = loan
        _uiState.update { it.copy(pendingDeleteLoan = loan) }

        deleteJob = viewModelScope.launch {
            delay(10000)
            pendingDeleteLoan = null
            _uiState.update { it.copy(pendingDeleteLoan = null) }
        }
    }

    fun undoDeleteLoan() {
        deleteJob?.cancel()
        deleteJob = null
        val item = pendingDeleteLoan ?: return
        pendingDeleteLoan = null
        _uiState.update { it.copy(pendingDeleteLoan = null) }
        // Re-insert back into DB
        viewModelScope.launch { addLoan(item.copy(id = 0)) }
    }

    private fun commitDeleteLoan() {
        pendingDeleteLoan = null
        _uiState.update { it.copy(pendingDeleteLoan = null) }
    }

    fun onDeleteLoan(loan: Loan) {
        viewModelScope.launch { deleteLoan(loan) }
    }

    fun toggleAddSheet(isOpen: Boolean) {
        _uiState.update { it.copy(isAddSheetOpen = isOpen) }
        if (!isOpen) _uiState.update { LoanUiState() } // reset on close
    }
}

data class LoanUiState(
    val name: String = "",
    val principal: String = "",
    val interestRate: String = "",
    val tenure: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val category: String = "Personal Loan",
    val calculatedEmi: Double = 0.0,
    val isAddSheetOpen: Boolean = false,
    // Errors
    val nameError: String? = null,
    val principalError: String? = null,
    val interestRateError: String? = null,
    val tenureError: String? = null,
    // Undo delete
    val pendingDeleteLoan: Loan? = null
)
