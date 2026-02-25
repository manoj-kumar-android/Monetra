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
    }

    fun savePreferences() {
        viewModelScope.launch {
            val incomeVal = _income.value.toDoubleOrNull() ?: 0.0
            val savingsVal = _savingsGoal.value.toDoubleOrNull() ?: 0.0
            val nameVal = _name.value.ifBlank { "there" }
            val currentPref = userPreferenceRepo.getUserPreferences().firstOrNull() ?: UserPreferences(ownerName = nameVal, monthlyIncome = 0.0, monthlySavingsGoal = 0.0)
            userPreferenceRepo.saveUserPreferences(
                currentPref.copy(
                    ownerName = nameVal,
                    monthlyIncome = incomeVal,
                    monthlySavingsGoal = savingsVal,
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
