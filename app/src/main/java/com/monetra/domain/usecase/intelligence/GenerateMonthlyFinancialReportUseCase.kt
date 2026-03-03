package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.FinancialBalanceStatus
import com.monetra.domain.model.MonthlyFinancialReport
import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.TransactionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.YearMonth
import javax.inject.Inject

/**
 * Monthly Planning Engine
 * 
 * This use case calculates the monthly financial health based on income, expenses, EMIs, and investments.
 * 
 * Formulas:
 * 1. Actual Savings = Total Income - (Living Expenses + EMIs)
 *    Note: Investment contributions are considered a form of saving, so they are not subtracted from the "Actual Savings" figure.
 * 
 * 2. Savings Gap = Monthly Savings Goal - Actual Savings
 * 
 * 3. Expense Ratio = (Living Expenses / Total Income) * 100
 *    Healthy target: < 50%
 * 
 * 4. EMI Ratio = (Total EMIs / Total Income) * 100
 *    Healthy target: < 30%. Debt risk: > 40%
 * 
 * 5. Investment Ratio = (Monthly Investment Contributions / Total Income) * 100
 *    Healthy target: > 20%
 */
class GenerateMonthlyFinancialReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) {
    operator fun invoke(month: YearMonth): Flow<MonthlyFinancialReport> {
        return combine(
            transactionRepository.getTransactions(month),
            userPreferenceRepository.getUserPreferences()
        ) { transactions, preferences ->
            val income = preferences.monthlyIncome
            val targetSavings = preferences.monthlySavingsGoal

            // Categorize transactions
            val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
            
            val totalEmis = expenseTransactions
                .filter { it.category.equals("EMI", ignoreCase = true) || it.category.equals("Loan", ignoreCase = true) }
                .sumOf { it.amount }
                
            val totalInvestments = expenseTransactions
                .filter { 
                    it.category.equals("Investment", ignoreCase = true) || 
                    it.category.equals("SIP", ignoreCase = true) || 
                    it.category.equals("Mutual Fund", ignoreCase = true) ||
                    it.category.equals("Stocks", ignoreCase = true)
                }
                .sumOf { it.amount }
                
            val livingExpenses = expenseTransactions
                .filter { 
                    !it.category.equals("EMI", ignoreCase = true) && 
                    !it.category.equals("Loan", ignoreCase = true) &&
                    !it.category.equals("Investment", ignoreCase = true) && 
                    !it.category.equals("SIP", ignoreCase = true) && 
                    !it.category.equals("Mutual Fund", ignoreCase = true) &&
                    !it.category.equals("Stocks", ignoreCase = true)
                }
                .sumOf { it.amount }

            val totalMandatoryOutflows = livingExpenses + totalEmis
            val actualSavings = income - totalMandatoryOutflows
            val savingsGap = (targetSavings - actualSavings).coerceAtLeast(0.0)

            // Ratios (Handle division by zero if income is 0)
            val expenseRatio = if (income > 0) (livingExpenses / income) * 100 else 0.0
            val emiRatio = if (income > 0) (totalEmis / income) * 100 else 0.0
            val investmentRatio = if (income > 0) (totalInvestments / income) * 100 else 0.0

            // Financial Balance Status
            val status = when {
                income <= 0 -> FinancialBalanceStatus.RISK
                emiRatio > 40.0 || actualSavings < 0 -> FinancialBalanceStatus.RISK
                emiRatio > 30.0 || actualSavings < targetSavings -> FinancialBalanceStatus.MODERATE
                else -> FinancialBalanceStatus.HEALTHY
            }

            MonthlyFinancialReport(
                month = month,
                income = income,
                totalExpenses = livingExpenses,
                totalEmis = totalEmis,
                totalInvestments = totalInvestments,
                targetSavings = targetSavings,
                actualSavings = actualSavings,
                savingsGap = savingsGap,
                expenseToIncomeRatio = expenseRatio,
                emiToIncomeRatio = emiRatio,
                investmentRatio = investmentRatio,
                status = status
            )
        }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    }
}
