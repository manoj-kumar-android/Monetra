package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.*
import com.monetra.domain.repository.InvestmentRepository
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
 * Investment Motivation Engine
 * 
 * Provides educational suggestions for starting or diversifying investments
 * based on the user's current financial profile.
 */
class AnalyzeInvestmentStatusUseCase @Inject constructor(
    private val investmentRepository: InvestmentRepository,
    private val transactionRepository: TransactionRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val generateReport: GenerateMonthlyFinancialReportUseCase
) {
    operator fun invoke(month: YearMonth): Flow<List<InvestmentSuggestion>> {
        return combine(
            investmentRepository.getInvestments(),
            transactionRepository.getExpenseSumByCategory(month),
            generateReport(month)
        ) { investments, categorySums, report ->
                    val suggestions = mutableListOf<InvestmentSuggestion>()
                    
                    // Check for existing investment patterns
                    val hasSIP = investments.any { it.type == InvestmentType.MUTUAL_FUND } || 
                                 categorySums.keys.any { it.contains("SIP", ignoreCase = true) }
                    
                    val hasFD = investments.any { it.type == InvestmentType.FIXED_DEPOSIT } || 
                                categorySums.keys.any { it.contains("FD", ignoreCase = true) || it.contains("Fixed Deposit", ignoreCase = true) }
                    
                    val hasPPF = categorySums.keys.any { it.contains("PPF", ignoreCase = true) }
                    val hasRD = categorySums.keys.any { it.contains("RD", ignoreCase = true) || it.contains("Recurring Deposit", ignoreCase = true) }

                    val totalIncome = report.income
                    val actualSavings = report.actualSavings
                    val emiRatio = report.emiToIncomeRatio

                    // Base Message if no investments exist
                    if (!hasSIP && !hasFD && !hasPPF && !hasRD) {
                        // Add a primary motivation
                        suggestions.add(
                            InvestmentSuggestion(
                                category = "General",
                                title = "Start Your Journey",
                                message = "You are not investing currently. Even small monthly amounts can help build significant wealth over time.",
                                suitabilityScore = 100
                            )
                        )
                    }

                    // SIP Suggestion: High suitability if there's regular income and some savings
                    if (!hasSIP && totalIncome > 0 && actualSavings > 1000) {
                        suggestions.add(
                            InvestmentSuggestion(
                                category = "SIP",
                                title = "Systematic Investment Plan",
                                message = "A SIP allows you to invest small amounts in mutual funds regularly. It's ideal for building long-term wealth through compounding.",
                                suitabilityScore = if (emiRatio < 30) 90 else 60
                            )
                        )
                    }

                    // PPF Suggestion: High suitability for long-term safety, especially if EMI ratio is high (safe haven)
                    if (!hasPPF && totalIncome > 20000) {
                        suggestions.add(
                            InvestmentSuggestion(
                                category = "PPF",
                                title = "Public Provident Fund",
                                message = "PPF is a safe, government-backed long-term saving scheme with tax benefits. Great for retirement planning.",
                                suitabilityScore = if (emiRatio > 40) 85 else 70
                            )
                        )
                    }

                    // RD Suggestion: Good for disciplined saving if income is moderate
                    if (!hasRD && actualSavings > 500 && actualSavings < 5000) {
                        suggestions.add(
                            InvestmentSuggestion(
                                category = "RD",
                                title = "Recurring Deposit",
                                message = "RD is perfect for reaching short-term goals by depositing a fixed amount monthly with guaranteed interest.",
                                suitabilityScore = 80
                            )
                        )
                    }

                    // FD Suggestion: Good if there is a large surplus (simplified here as saving > 20% income)
                    if (!hasFD && totalIncome > 0 && actualSavings > (totalIncome * 0.25)) {
                        suggestions.add(
                            InvestmentSuggestion(
                                category = "FD",
                                title = "Fixed Deposit",
                                message = "If you have a lump sum amount, an FD offers safety and predictable returns compared to a regular savings account.",
                                suitabilityScore = 75
                            )
                        )
                    }

                    suggestions.sortedByDescending { it.suitabilityScore }
        }.flowOn(Dispatchers.Default)
    }
}
