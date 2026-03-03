package com.monetra.domain.model

import java.time.YearMonth

data class ComprehensiveMonthlyReport(
    val month: YearMonth,
    val income: Double,
    val expenses: Double,
    val emis: Double,
    val investments: Double,
    val actualSavings: Double,
    val targetSavings: Double,
    val topCategories: List<CategorySpending>,
    val suggestions: List<SavingSuggestion>,
    val status: FinancialBalanceStatus,
    val emiStressLevel: String,
    val comparison: PreviousMonthComparison? = null
)

data class CategorySpending(
    val category: String,
    val amount: Double
)

data class PreviousMonthComparison(
    val incomeChangePercent: Double,
    val expenseChangePercent: Double,
    val savingsChangePercent: Double,
    val investmentChangePercent: Double
)
