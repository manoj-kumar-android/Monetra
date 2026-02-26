package com.monetra.domain.model

data class PlanningOverview(
    val monthlySafety: MonthlySafetyAnalysis,
    val moneyLeakage: MoneyLeakageAnalysis,
    val emergencySafety: EmergencySafetyAnalysis,
    val controlPlan: WeeklyControlPlan,
    val totalEmi: Double,
    val totalInvestments: Double,
    val affordabilityCalculation: AffordabilityAnalysis? = null
)

data class MonthlySafetyAnalysis(
    val status: SafetyStatus,
    val savingsGap: Double,
    val emiRatio: Double,
    val isBurnRisk: Boolean,
    val suggestedAction: String,
    val dailyBurnRate: Double = 0.0
)

enum class SafetyStatus { GREEN, YELLOW, RED }

data class MoneyLeakageAnalysis(
    val topCategories: List<CategoryLeak>,
    val leakageMessage: String,
    val totalPotentialSaving: Double
)

data class CategoryLeak(val name: String, val amount: Double)

data class EmergencySafetyAnalysis(
    val monthsCovered: Double,
    val targetMonths: Int = 6,
    val status: SafetyStatus,
    val suggestion: String?
)

data class WeeklyControlPlan(
    val weeklyLimit: Double,
    val dailyLimit: Double,
    val remainingToday: Double,
    val daysPassedInWeek: Int,
    val highRiskCategory: String?
)

data class AffordabilityAnalysis(
    val amount: Double,
    val status: SafetyStatus,
    val verdict: String,
    val message: String,
    val opportunityCost: String? = null
)
