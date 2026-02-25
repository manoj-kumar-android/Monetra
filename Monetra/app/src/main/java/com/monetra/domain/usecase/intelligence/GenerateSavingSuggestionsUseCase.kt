package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.*
import com.monetra.domain.repository.SubscriptionRepository
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.time.YearMonth
import javax.inject.Inject

/**
 * Saving Suggestion Engine
 * 
 * Analyzes financial behavior to provide actionable tips for closing the savings gap
 * and maintaining healthy financial ratios.
 */
class GenerateSavingSuggestionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val generateReport: GenerateMonthlyFinancialReportUseCase
) {
    operator fun invoke(month: YearMonth): Flow<List<SavingSuggestion>> {
        return combine(
            generateReport(month),
            transactionRepository.getExpenseSumByCategory(month)
        ) { report, categorySums ->
                    val suggestions = mutableListOf<SavingSuggestion>()
                    
                    // 1. Handle EMI Stress
                    if (report.emiToIncomeRatio > 50.0) {
                        suggestions.add(
                            SavingSuggestion(
                                title = "Debt Restructuring Advice",
                                message = "Your EMI payments exceed 50% of your income. Consider consolidating loans or opting for a longer tenure to reduce immediate monthly stress.",
                                type = SuggestionType.DEBT_ADVICE,
                                priority = SuggestionPriority.CRITICAL
                            )
                        )
                    } else if (report.emiToIncomeRatio > 40.0) {
                        suggestions.add(
                            SavingSuggestion(
                                title = "High Debt Burden",
                                message = "Your debt ratio is higher than recommended. Avoid taking new loans until your current EMIs are reduced.",
                                type = SuggestionType.DEBT_ADVICE,
                                priority = SuggestionPriority.HIGH
                            )
                        )
                    }

                    // 2. Analyze Entertainment Spending
                    val entertainmentSpending = categorySums.entries
                        .filter { it.key.equals("Entertainment", ignoreCase = true) || it.key.equals("Leisure", ignoreCase = true) }
                        .sumOf { it.value }
                    
                    val entertainmentRatio = if (report.income > 0) (entertainmentSpending / report.income) * 100 else 0.0
                    
                    if (entertainmentRatio > 20.0) {
                        suggestions.add(
                            SavingSuggestion(
                                title = "Entertainment Budget Tip",
                                message = "You've spent %.1f%% of your salary on entertainment. Reducing this to 10%% could save you ₹%,.0f monthly.".format(entertainmentRatio, entertainmentSpending * 0.5),
                                potentialSavings = entertainmentSpending * 0.5,
                                type = SuggestionType.BUDGET_ADJUSTMENT,
                                priority = SuggestionPriority.MEDIUM
                            )
                        )
                    }

                    // 3. Address Savings Gap with Category Reduction
                    if (report.savingsGap > 0) {
                        val nonEssentialCategories = listOf("Food", "Dining", "Shopping", "Lifestyle", "Travel", "Misc")
                        val topSpending = categorySums.entries
                            .filter { entry -> 
                                nonEssentialCategories.any { it.equals(entry.key, ignoreCase = true) }
                            }
                            .sortedByDescending { it.value }
                            .take(2)

                        topSpending.forEachIndexed { index, entry ->
                            val reductionPercent = if (index == 0) 0.15 else 0.10 // 15% for top 1, 10% for top 2
                            val potentialCut = entry.value * reductionPercent
                            
                            suggestions.add(
                                SavingSuggestion(
                                    title = "Optimize ${entry.key} Spent",
                                    message = "You can reduce ${entry.key} spending by ₹%,.0f (around ${ (reductionPercent * 100).toInt() }%%) to help reach your savings goal of ₹%,.0f.".format(potentialCut, report.targetSavings),
                                    potentialSavings = potentialCut,
                                    type = SuggestionType.EXPENSE_REDUCTION,
                                    priority = if (index == 0) SuggestionPriority.HIGH else SuggestionPriority.MEDIUM
                                )
                            )
                        }
                    }

                    // 4. Investment Encouragement (if income exists and investment ratio is low)
                    if (report.income > 0 && report.investmentRatio < 10.0 && report.actualSavings > 0) {
                        suggestions.add(
                            SavingSuggestion(
                                title = "Power of Compounding",
                                message = "You have surplus cash. Starting an SIP of just ₹%,.0f could significantly grow your wealth over time.".format(report.actualSavings * 0.5),
                                type = SuggestionType.GENERAL,
                                priority = SuggestionPriority.LOW
                            )
                        )
                    }

                    suggestions
        }.flowOn(Dispatchers.Default)
    }
}
