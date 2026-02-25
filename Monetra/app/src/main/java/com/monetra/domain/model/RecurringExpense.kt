package com.monetra.domain.model

import java.time.LocalDate

data class RecurringExpense(
    val title: String,
    val amount: Double,
    val category: String,
    val nextExpectedDate: LocalDate,
    val isStabilityHigh: Boolean
)
