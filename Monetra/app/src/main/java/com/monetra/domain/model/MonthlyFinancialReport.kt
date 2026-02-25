package com.monetra.domain.model

import java.time.YearMonth

data class MonthlyFinancialReport(
    val month: YearMonth,
    val income: Double,
    val totalExpenses: Double,
    val totalEmis: Double,
    val totalInvestments: Double,
    val targetSavings: Double,
    val actualSavings: Double,
    val savingsGap: Double,
    val expenseToIncomeRatio: Double,
    val emiToIncomeRatio: Double,
    val investmentRatio: Double,
    val status: FinancialBalanceStatus
)

enum class FinancialBalanceStatus {
    HEALTHY,
    MODERATE,
    RISK
}
