package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.PersonalityType
import com.monetra.domain.model.SpendingPersonality
import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.BudgetRepository
import com.monetra.domain.repository.TransactionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.DayOfWeek
import java.time.YearMonth
import javax.inject.Inject

class AnalyzeSpendingBehaviorUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val detectRecurringExpenses: DetectRecurringExpensesUseCase
) {
    operator fun invoke(month: YearMonth): Flow<SpendingPersonality> {
        return combine(
            transactionRepository.getTransactions(month),
            budgetRepository.getCategoryBudgets(month),
            userPreferenceRepository.getUserPreferences(),
            detectRecurringExpenses()
        ) { transactions, budgets, preferences, recurring ->
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
            val totalSpent = expenses.sumOf { it.amount }
            val income = preferences.monthlyIncome

            // 1. Weekend vs Weekday analysis
            val weekendSpending = expenses
                .filter { it.date.dayOfWeek == DayOfWeek.SATURDAY || it.date.dayOfWeek == DayOfWeek.SUNDAY }
                .sumOf { it.amount }
            val weekendRatio = if (totalSpent > 0) weekendSpending / totalSpent else 0.0

            // 2. Subscription load
            val recurringTotal = recurring.sumOf { it.amount }
            val subRatio = if (income > 0) recurringTotal / income else 0.0

            // 3. Budget Adherence
            val adherenceRatio = if (budgets.isNotEmpty()) {
                budgets.count { it.currentSpent <= it.limit }.toDouble() / budgets.size
            } else 1.0

            // 4. Late-night spending (dummy logic as Transaction doesn't have time yet, but we'll reserve the slot)
            // For now, we'll use a placeholder or assume 0 until we add time to Transaction model.
            val lateNightRatio = 0.0

            val personalityType = when {
                subRatio > 0.25 -> PersonalityType.SUBSCRIPTION_HEAVY
                weekendRatio > 0.45 -> PersonalityType.WEEKEND_SPENDER
                adherenceRatio < 0.50 -> PersonalityType.IMPULSIVE_SPENDER
                adherenceRatio >= 0.80 && totalSpent <= income -> PersonalityType.CONTROLLED_PLANNER
                else -> PersonalityType.BALANCED_OVRALL
            }

            SpendingPersonality(
                type = personalityType,
                description = when (personalityType) {
                    PersonalityType.SUBSCRIPTION_HEAVY -> "You have a high reliance on recurring services. While convenient, these can drain your savings over time."
                    PersonalityType.WEEKEND_SPENDER -> "Your spending spikes significantly during the weekend. Most of your disposable income goes into leisure and social activities."
                    PersonalityType.IMPULSIVE_SPENDER -> "You often exceed your planned budgets. Rapid spending and unplanned purchases are common patterns."
                    PersonalityType.CONTROLLED_PLANNER -> "You are highly disciplined with your finances, consistently staying within your defined limits."
                    PersonalityType.BALANCED_OVRALL -> "Your spending is well-distributed. You maintain a good balance between planning and social spending."
                },
                keyStrengths = when (personalityType) {
                    PersonalityType.SUBSCRIPTION_HEAVY -> listOf("Service loyalty", "Predictable expenses")
                    PersonalityType.WEEKEND_SPENDER -> listOf("Social engagement", "Controlled weekday spending")
                    PersonalityType.IMPULSIVE_SPENDER -> listOf("High utility from spending", "Supporting diverse categories")
                    PersonalityType.CONTROLLED_PLANNER -> listOf("Budget discipline", "Future-oriented", "High savings potential")
                    PersonalityType.BALANCED_OVRALL -> listOf("Adaptability", "Economic stability")
                },
                areasOfImprovement = when (personalityType) {
                    PersonalityType.SUBSCRIPTION_HEAVY -> listOf("Audit unused subscriptions", "Check for redundant services")
                    PersonalityType.WEEKEND_SPENDER -> listOf("Set a weekend specific budget", "Look for lower-cost leisure")
                    PersonalityType.IMPULSIVE_SPENDER -> listOf("Implement the 24-hour rule", "Reduce ad-hoc shopping")
                    PersonalityType.CONTROLLED_PLANNER -> listOf("Ensure you're enjoying your wealth", "Don't be too restrictive")
                    PersonalityType.BALANCED_OVRALL -> listOf("Optimize for higher savings rates")
                }
            )
        }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    }
}
