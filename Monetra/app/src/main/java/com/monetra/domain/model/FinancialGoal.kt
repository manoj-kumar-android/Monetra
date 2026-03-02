package com.monetra.domain.model

import java.time.LocalDate
import kotlinx.serialization.Serializable

data class FinancialGoal(
    val id: Long = 0L,
    override   val remoteId: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: LocalDate?,
    val category: GoalCategory,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable

@Serializable
enum class GoalCategory {
    SAVINGS,
    INVESTMENT,
    DEBT_REPAYMENT,
    PURCHASE,
    EMERGENCY_FUND,
    RETIREMENT
}
