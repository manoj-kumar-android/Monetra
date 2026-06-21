package com.monetra.feature.onboarding.presentation

import androidx.lifecycle.viewModelScope
import com.monetra.core.mvi.MviViewModel
import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.UserPreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository
) : MviViewModel<OnboardingIntent, OnboardingState, OnboardingEffect>(OnboardingState()) {

    override fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.NextStep -> handleNextStep()
            is OnboardingIntent.PreviousStep -> handlePreviousStep()
            is OnboardingIntent.UpdateIncome -> {
                updateState { copy(monthlyIncome = intent.income, error = null) }
            }

            is OnboardingIntent.UpdateSavingsGoal -> {
                updateState { copy(monthlySavingsGoal = intent.goal, error = null) }
            }

            is OnboardingIntent.UpdateBillName -> {
                updateState { copy(billName = intent.name) }
            }

            is OnboardingIntent.UpdateBillAmount -> {
                updateState { copy(billAmount = intent.amount) }
            }

            is OnboardingIntent.UpdateBillCategory -> {
                updateState { copy(billCategory = intent.category) }
            }

            is OnboardingIntent.UpdateBillDueDay -> {
                updateState { copy(billDueDay = intent.dueDay) }
            }

            is OnboardingIntent.AddBill -> handleAddBill()
            is OnboardingIntent.DeleteBill -> handleDeleteBill(intent.bill)
            is OnboardingIntent.CompleteOnboarding -> handleCompleteOnboarding()
        }
    }

    private fun handleNextStep() {
        val nextStep = when (currentState.currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.INCOME
            OnboardingStep.INCOME -> {
                val income = currentState.monthlyIncome.toDoubleOrNull()
                if (income == null || income <= 0) {
                    updateState { copy(error = "Please enter a valid monthly income.") }
                    return
                }
                OnboardingStep.SAVINGS
            }

            OnboardingStep.SAVINGS -> {
                val savings = currentState.monthlySavingsGoal.toDoubleOrNull()
                val income = currentState.monthlyIncome.toDoubleOrNull() ?: 0.0
                if (savings == null || savings < 0) {
                    updateState { copy(error = "Please enter a valid savings goal.") }
                    return
                }
                if (savings > income) {
                    updateState { copy(error = "Savings goal cannot be greater than monthly income.") }
                    return
                }
                OnboardingStep.BILLS
            }

            OnboardingStep.BILLS -> {
                handleCompleteOnboarding()
                return
            }
        }
        updateState { copy(currentStep = nextStep, error = null) }
    }

    private fun handlePreviousStep() {
        val prevStep = when (currentState.currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME
            OnboardingStep.INCOME -> OnboardingStep.WELCOME
            OnboardingStep.SAVINGS -> OnboardingStep.INCOME
            OnboardingStep.BILLS -> OnboardingStep.SAVINGS
        }
        updateState { copy(currentStep = prevStep, error = null) }
    }

    private fun handleAddBill() {
        val name = currentState.billName.trim()
        val amount = currentState.billAmount.toDoubleOrNull()
        val category = currentState.billCategory.trim()
        val dueDay = currentState.billDueDay.toIntOrNull() ?: 1

        if (name.isEmpty()) {
            sendEffect(OnboardingEffect.ShowToast("Please enter a bill name."))
            return
        }
        if (amount == null || amount <= 0) {
            sendEffect(OnboardingEffect.ShowToast("Please enter a valid bill amount."))
            return
        }
        if (dueDay !in 1..31) {
            sendEffect(OnboardingEffect.ShowToast("Due day must be between 1 and 31."))
            return
        }

        val newBill = MonthlyExpense(
            name = name,
            amount = amount,
            category = if (category.isEmpty()) "Bills" else category,
            dueDay = dueDay
        )

        updateState {
            copy(
                bills = bills + newBill,
                billName = "",
                billAmount = "",
                billCategory = "Bills",
                billDueDay = "1"
            )
        }
    }

    private fun handleDeleteBill(bill: MonthlyExpense) {
        updateState {
            copy(bills = bills.filterNot { it.remoteId == bill.remoteId })
        }
    }

    private fun handleCompleteOnboarding() {
        viewModelScope.launch {
            try {
                val currentPrefs = userPreferenceRepository.getUserPreferences().first()
                val updatedPrefs = currentPrefs.copy(
                    monthlyIncome = currentState.monthlyIncome.toDoubleOrNull() ?: 0.0,
                    monthlySavingsGoal = currentState.monthlySavingsGoal.toDoubleOrNull() ?: 0.0,
                    isOnboardingCompleted = true
                )
                userPreferenceRepository.saveUserPreferences(updatedPrefs)
                monthlyExpenseRepository.insertAll(currentState.bills)
                sendEffect(OnboardingEffect.NavigationToDashboard)
            } catch (e: Exception) {
                updateState { copy(error = e.message ?: "Failed to save onboarding data") }
            }
        }
    }
}
