package com.monetra.domain.model

/**
 * Aggregated totals for a single calendar month.
 * Computed inside [GetMonthlySummaryUseCase] — never in the ViewModel or UI.
 */
data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
) {
    /** Derived; no need to store or pass separately. */
    val balance: Double get() = totalIncome - totalExpense
}
