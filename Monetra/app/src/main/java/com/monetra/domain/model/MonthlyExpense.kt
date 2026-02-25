package com.monetra.domain.model

data class MonthlyExpense(
    val id: Long = 0L,
    val name: String,
    val amount: Double,
    val category: String = "Utility"
)
