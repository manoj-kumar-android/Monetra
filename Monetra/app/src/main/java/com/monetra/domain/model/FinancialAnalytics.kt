package com.monetra.domain.model

import java.time.LocalDate

data class Loan(
    val id: Long = 0L,
    val name: String,
    val totalPrincipal: Double,
    val monthlyEmi: Double,
    val startDate: LocalDate,
    val tenureMonths: Int,
    val remainingTenure: Int,
    val category: String = "Personal"
) {
    val remainingBalance: Double 
        get() = monthlyEmi * remainingTenure
        
    val progress: Float
        get() = if (tenureMonths > 0) (tenureMonths - remainingTenure).toFloat() / tenureMonths else 1f
}

data class BurnRateAnalysis(
    val currentDay: Int,
    val totalDays: Int,
    val currentSpend: Double,
    val projectedEndMonthSpend: Double,
    val isOverspending: Boolean,
    val warningMessage: String?
)

data class SafeToSpend(
    val dailyLimit: Double,
    val remainingToday: Double,
    val monthlyAllowance: Double
) {
    val remainingPercent: Float
        get() = if (dailyLimit > 0) (remainingToday / dailyLimit).toFloat().coerceIn(0f, 1f) else 0f
}
