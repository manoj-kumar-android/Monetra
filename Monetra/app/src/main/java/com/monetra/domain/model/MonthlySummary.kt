package com.monetra.domain.model

/**
 * Aggregated totals for a single calendar month.
 * Computed inside [GetMonthlySummaryUseCase] — never in the ViewModel or UI.
 */
data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val reservedAmount: Double = 0.0
) {
    /** Actual balance based on real transactions. */
    val balance: Double get() = totalIncome - totalExpense
    
    /** Money available after considering reserved/planned expenses. */
    val availableBalance: Double get() = balance - reservedAmount
}
