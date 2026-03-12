package com.monetra.domain.model

import java.time.LocalDate

data class TransactionFilters(
    val query: String? = null,
    val type: TransactionType? = null,
    val categories: List<String>? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)

data class TransactionSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netAmount: Double
)
