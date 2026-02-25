package com.monetra.domain.model

data class SmartInsight(
    val title: String,
    val message: String,
    val type: InsightTypes
)

enum class InsightTypes {
    SPENDING_SURGE,
    BUDGET_WARNING,
    OVERSPEND_PREDICTION,
    RECURRING_DETECTED,
    GOAL_ON_TRACK,
    GOAL_AT_RISK,
    INVESTMENT_TIP
}
