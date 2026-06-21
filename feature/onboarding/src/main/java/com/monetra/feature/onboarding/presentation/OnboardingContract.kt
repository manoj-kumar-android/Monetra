package com.monetra.feature.onboarding.presentation

import com.monetra.core.mvi.MviEffect
import com.monetra.core.mvi.MviIntent
import com.monetra.core.mvi.MviState
import com.monetra.core.ui.util.UiText
import com.monetra.domain.model.MonthlyExpense

enum class OnboardingStep {
    WELCOME,
    INCOME,
    SAVINGS,
    BILLS
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val monthlyIncome: String = "",
    val monthlySavingsGoal: String = "",
    val billName: String = "",
    val billAmount: String = "",
    val billCategory: String = "Bills",
    val billDueDay: String = "1",
    val bills: List<MonthlyExpense> = emptyList(),
    val error: UiText? = null
) : MviState

sealed interface OnboardingIntent : MviIntent {
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent
    data class UpdateIncome(val income: String) : OnboardingIntent
    data class UpdateSavingsGoal(val goal: String) : OnboardingIntent
    data class UpdateBillName(val name: String) : OnboardingIntent
    data class UpdateBillAmount(val amount: String) : OnboardingIntent
    data class UpdateBillCategory(val category: String) : OnboardingIntent
    data class UpdateBillDueDay(val dueDay: String) : OnboardingIntent
    data object AddBill : OnboardingIntent
    data class DeleteBill(val bill: MonthlyExpense) : OnboardingIntent
    data object CompleteOnboarding : OnboardingIntent
}

sealed interface OnboardingEffect : MviEffect {
    data object NavigationToDashboard : OnboardingEffect
    data class ShowToast(val message: UiText) : OnboardingEffect
}
