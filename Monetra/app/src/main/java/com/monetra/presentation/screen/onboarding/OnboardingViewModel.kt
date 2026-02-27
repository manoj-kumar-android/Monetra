package com.monetra.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Loan
import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.model.UserPreferences
import com.monetra.domain.repository.LoanRepository
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.UserPreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferenceRepo: UserPreferenceRepository,
    private val monthlyExpenseRepo: MonthlyExpenseRepository,
    private val loanRepo: LoanRepository
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep = _currentStep.asStateFlow()

    private val _income = MutableStateFlow("")
    val income = _income.asStateFlow()
    
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _savingsGoal = MutableStateFlow("")
    val savingsGoal = _savingsGoal.asStateFlow()

    private val _savingsError = MutableStateFlow<String?>(null)
    val savingsError = _savingsError.asStateFlow()

    val fixedCosts = monthlyExpenseRepo.getAllMonthlyExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loans = loanRepo.getAllLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            userPreferenceRepo.getUserPreferences().firstOrNull()?.let {
                if (it.ownerName.isNotBlank()) {
                    _name.value = it.ownerName
                }
                if (it.monthlyIncome > 0) {
                    _income.value = it.monthlyIncome.toString()
                }
                if (it.monthlySavingsGoal > 0) {
                    _savingsGoal.value = it.monthlySavingsGoal.toString()
                }
            }
        }
    }

    fun nextStep() {
        if (_currentStep.value == 0) {
            val incomeVal = _income.value.toDoubleOrNull() ?: 0.0
            val savingsVal = _savingsGoal.value.toDoubleOrNull() ?: 0.0
            if (savingsVal >= incomeVal && incomeVal > 0) {
                _savingsError.value = "Savings goal must be less than monthly income"
                return
            }
            _savingsError.value = null
        }
        if (_currentStep.value < 3) {
            _currentStep.value += 1
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value -= 1
        }
    }

    fun setIncome(value: String) {
        _income.value = value
    }

    fun setName(value: String) {
        _name.value = value
    }

    fun setSavingsGoal(value: String) {
        _savingsGoal.value = value
        _savingsError.value = null // clear error on change
    }

    fun savePreferences() {
        val incomeVal = _income.value.toDoubleOrNull() ?: 0.0
        val savingsVal = _savingsGoal.value.toDoubleOrNull() ?: 0.0
        // Clamp savings to be strictly less than income before saving
        val clampedSavings = if (savingsVal >= incomeVal && incomeVal > 0) incomeVal * 0.9 else savingsVal
        viewModelScope.launch {
            val nameVal = _name.value.ifBlank { "there" }
            val currentPref = userPreferenceRepo.getUserPreferences().firstOrNull() ?: UserPreferences(ownerName = nameVal, monthlyIncome = 0.0, monthlySavingsGoal = 0.0)
            userPreferenceRepo.saveUserPreferences(
                currentPref.copy(
                    ownerName = nameVal,
                    monthlyIncome = incomeVal,
                    monthlySavingsGoal = clampedSavings,
                    isOnboardingCompleted = true
                )
            )
        }
    }

    fun addQuickFixedCost(name: String, amount: Double) {
        viewModelScope.launch {
            monthlyExpenseRepo.insertMonthlyExpense(
                MonthlyExpense(
                    name = name,
                    amount = amount
                )
            )
        }
    }

    fun addQuickEmi(name: String, amount: Double) {
        viewModelScope.launch {
            loanRepo.insertLoan(
                Loan(
                    name = name,
                    totalPrincipal = amount * 12,
                    monthlyEmi = amount,
                    startDate = java.time.LocalDate.now(),
                    tenureMonths = 12,
                    remainingTenure = 12
                )
            )
        }
    }
}
