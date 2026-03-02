package com.monetra.domain.model

import java.time.LocalDate

data class Loan(
    val id: Long = 0L,
    override    val remoteId: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val totalPrincipal: Double,
    val annualInterestRate: Double = 0.0, // in %, e.g. 8.5 for 8.5%
    val monthlyEmi: Double,               // calculated and stored
    val startDate: LocalDate,
    val tenureMonths: Int,
    val remainingTenure: Int,
    val category: String = "Personal",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable {
    val remainingBalance: Double
        get() = monthlyEmi * remainingTenure

    val progress: Float
        get() = if (tenureMonths > 0) (tenureMonths - remainingTenure).toFloat() / tenureMonths else 1f

    companion object {
        /** Standard reducing-balance EMI formula */
        fun calculateEmi(principal: Double, annualRatePercent: Double, tenureMonths: Int): Double {
            if (principal <= 0 || tenureMonths <= 0) return 0.0
            if (annualRatePercent <= 0) return principal / tenureMonths // zero-interest simple split
            val monthlyRate = annualRatePercent / 12.0 / 100.0
            val factor = Math.pow(1 + monthlyRate, tenureMonths.toDouble())
            return principal * monthlyRate * factor / (factor - 1)
        }
    }
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
