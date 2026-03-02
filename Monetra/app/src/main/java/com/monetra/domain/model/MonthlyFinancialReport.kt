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
    val status: FinancialBalanceStatus,
    override val remoteId: String = "report_$month",
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable

enum class FinancialBalanceStatus {
    HEALTHY,
    MODERATE,
    RISK
}
