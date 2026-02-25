package com.monetra.domain.model

import java.time.LocalDate

data class FinancialGoal(
    val id: Long = 0L,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: LocalDate?,
    val category: GoalCategory
)

enum class GoalCategory {
    SAVINGS,
    INVESTMENT,
    DEBT_REPAYMENT,
    PURCHASE,
    EMERGENCY_FUND,
    RETIREMENT
}
