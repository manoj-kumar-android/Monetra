package com.monetra.domain.model

data class PortfolioData(
    // Raw inputs
    val monthlyIncome: Double,
    val currentSavings: Double,
    val totalInvestmentValue: Double,
    val totalLoanRemaining: Double,
    val totalMonthlyEmi: Double,
    val totalMonthlyExpenses: Double,
    val totalMonthlyInvestment: Double,

    // Calculated
    val netWorth: Double,
    val freeMoney: Double,
    val financialScore: FinancialScore,
    val wealthProjection: PortfolioProjection,

    val hasData: Boolean
)

data class PortfolioProjection(
    val monthlyContribution: Double,
    val years: Int,
    val annualRatePercent: Double,
    val totalInvested: Double,
    val totalReturns: Double,
    val projectedValue: Double
)

enum class FinancialScore(val label: String, val emoji: String) {
    POOR("Poor", "😟"),
    AVERAGE("Average", "😐"),
    GOOD("Good", "😊"),
    EXCELLENT("Excellent", "🌟")
}

fun Double.toFinancialScore(): FinancialScore = when {
    this < 0.10 -> FinancialScore.POOR
    this < 0.20 -> FinancialScore.AVERAGE
    this < 0.35 -> FinancialScore.GOOD
    else -> FinancialScore.EXCELLENT
}
