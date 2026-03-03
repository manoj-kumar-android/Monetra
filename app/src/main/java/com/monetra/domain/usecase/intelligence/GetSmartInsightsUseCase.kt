package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.InsightTypes
import com.monetra.domain.model.SmartInsight
import com.monetra.domain.repository.GoalRepository
import com.monetra.domain.repository.InvestmentRepository
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import javax.inject.Inject

class GetSmartInsightsUseCase @Inject constructor(
    private val calculateBurnRate: CalculateBurnRateUseCase,
    private val getCategoryBudgets: GetCategoryBudgetsUseCase,
    private val detectRecurringExpenses: DetectRecurringExpensesUseCase,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val goalRepository: GoalRepository,
    private val investmentRepository: InvestmentRepository
) {
    operator fun invoke(): Flow<List<SmartInsight>> {
        val now = YearMonth.now()
        return combine(
            combine(
                calculateBurnRate(),
                getCategoryBudgets(now),
                detectRecurringExpenses()
            ) { burn, budgets, recurring -> Triple(burn, budgets, recurring) },
            combine(
                userPreferenceRepository.getUserPreferences(),
                goalRepository.getGoals(),
                investmentRepository.getInvestments()
            ) { preferences, goals, investments -> Triple(preferences, goals, investments) }
        ) { (burn, budgets, recurring), (preferences, goals, investments) ->
            val insights = mutableListOf<SmartInsight>()

            // 1. Spending velocity warning
            if (burn != null && burn.isOverspending) {
                insights.add(
                    SmartInsight(
                        title = "Spending Surge",
                        message = burn.warningMessage ?: "Warning: Current spending velocity will lead to overspending.",
                        type = InsightTypes.SPENDING_SURGE
                    )
                )
            }

            // 2. Budget 85% consumed
            budgets.forEach { budget ->
                if (budget.progress in 0.85f..<1.0f) {
                    insights.add(
                        SmartInsight(
                            title = "Budget Warning",
                            message = "You've used %.0f%% of your %s budget.".format(budget.progress * 100, budget.categoryName),
                            type = InsightTypes.BUDGET_WARNING
                        )
                    )
                }
            }

            // 3 is already handled by burn rate surge above

            // 4. Goal Progress
            goals.forEach { goal ->
                val progress = if (goal.targetAmount > 0) goal.currentAmount / goal.targetAmount else 0.0
                if (progress >= 0.9) {
                    insights.add(
                        SmartInsight(
                            title = "Goal Reached Soon!",
                            message = "You are %d%% away from your %s goal.".format(((1 - progress) * 100).toInt(), goal.title),
                            type = InsightTypes.GOAL_ON_TRACK
                        )
                    )
                }
            }
            
            // 5. Investment Tip
            if (investments.isEmpty()) {
                insights.add(
                    SmartInsight(
                        title = "Start Investing",
                        message = "Grow your wealth by starting with a small SIP.",
                        type = InsightTypes.INVESTMENT_TIP
                    )
                )
            }

            // 4. Recurring expenses detected
            if (recurring.isNotEmpty()) {
                insights.add(
                    SmartInsight(
                        title = "Subscriptions Found",
                        message = "Detected %d recurring payments (₹%,.0f total).".format(recurring.size, recurring.sumOf { it.amount }),
                        type = InsightTypes.RECURRING_DETECTED
                    )
                )
            }

            insights
        }
    }
}
