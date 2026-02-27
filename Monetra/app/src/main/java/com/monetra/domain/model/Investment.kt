package com.monetra.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow

enum class ContributionFrequency {
    ONE_TIME, MONTHLY
}

data class StepChange(
    val amount: Double,
    val effectiveDate: LocalDate
)

data class Investment(
    val id: Long = 0L,
    val name: String,
    val type: InvestmentType,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val amount: Double = 0.0, // Lump sum amount (Principal for ONE_TIME)
    val monthlyAmount: Double = 0.0, // Recurring amount (Principal for MONTHLY)
    val interestRate: Double = 0.0,
    val currentValue: Double = 0.0,
    val frequency: ContributionFrequency,
    val stepChanges: List<StepChange> = emptyList()
) {
    /**
     * Total months elapsed since the investment started.
     */
    fun monthsElapsed(today: LocalDate = LocalDate.now()): Long {
        return ChronoUnit.MONTHS.between(startDate, today).coerceAtLeast(0)
    }

    /**
     * Total years elapsed since the investment started (fractional).
     */
    fun yearsElapsed(today: LocalDate = LocalDate.now()): Double {
        return monthsElapsed(today) / 12.0
    }

    /**
     * Calculates the real total invested and the real current value using compound logic based on the timeline.
     */
    private fun calculateRealWealth(today: LocalDate = LocalDate.now()): Pair<Double, Double> {
        val limitDate = endDate?.let { if (it.isBefore(today)) it else today } ?: today
        if (limitDate.isBefore(startDate)) return Pair(0.0, 0.0)

        var totalInvested = 0.0
        var currentValueAccumulated = 0.0

        val totalMonths = ChronoUnit.MONTHS.between(startDate, limitDate).toInt() + 1

        if (frequency == ContributionFrequency.MONTHLY) {
            val r = interestRate / 12.0 / 100.0
            
            for (i in 0 until totalMonths) {
                val contributionDate = startDate.plusMonths(i.toLong())
                val effectiveAmount = stepChanges
                    .filter { !it.effectiveDate.isAfter(contributionDate) }
                    .maxByOrNull { it.effectiveDate }?.amount ?: monthlyAmount
                
                totalInvested += effectiveAmount
                val remainingMonths = totalMonths - i
                
                if (r > 0) {
                    currentValueAccumulated += effectiveAmount * (1 + r).pow(remainingMonths.toDouble())
                } else {
                    currentValueAccumulated += effectiveAmount
                }
            }
        } else {
            totalInvested = amount
            val yearsElapsed = ChronoUnit.DAYS.between(startDate, limitDate) / 365.25
            val r = interestRate / 100.0
            if (r > 0) {
                currentValueAccumulated = amount * (1 + r).pow(yearsElapsed)
            } else {
                currentValueAccumulated = amount
            }
        }

        return Pair(totalInvested, currentValueAccumulated)
    }

    /**
     * The actual amount of money the user has "put in" to this investment.
     */
    fun calculateTotalInvested(today: LocalDate = LocalDate.now()): Double {
        return calculateRealWealth(today).first
    }

    /**
     * The estimated or actual current value of the investment.
     * Some types are MARKET-based (user edits), others are LOGIC-based (interest rate).
     */
    fun calculateCurrentValue(today: LocalDate = LocalDate.now()): Double {
        // If the user has explicitly set a current value that isn't overridden by logic, use it if market-based
        if (type in listOf(
            InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.GOLD, 
            InvestmentType.MUTUAL_FUND, InvestmentType.OTHER, InvestmentType.REAL_ESTATE
        )) {
            return currentValue
        }

        if (type == InvestmentType.CASH) return calculateRealWealth(today).first
        if (type == InvestmentType.INSURANCE) return calculateRealWealth(today).first

        return calculateRealWealth(today).second
    }

    /**
     * Gains or Losses in absolute value.
     */
    fun calculateTotalReturns(today: LocalDate = LocalDate.now()): Double {
        return calculateCurrentValue(today) - calculateTotalInvested(today)
    }

    /**
     * Percentage ROI.
     */
    fun calculateReturnPercentage(today: LocalDate = LocalDate.now()): Double {
        val invested = calculateTotalInvested(today)
        if (invested <= 0) return 0.0
        return (calculateTotalReturns(today) / invested) * 100.0
    }
}

enum class LiquidityClass {
    LIQUID,         // 100% value
    SEMI_LIQUID,    // Haircut applied (e.g., 70% value)
    LOCKED          // 0% value for emergency runway
}

enum class InvestmentType(
    val displayName: String,
    val emoji: String,
    val defaultFrequency: ContributionFrequency,
    val colorHex: Long,
    val liquidityClass: LiquidityClass,
    val haircut: Double = 0.0
) {
    CASH("Cash / Savings", "💵", ContributionFrequency.ONE_TIME, 0xFF4CAF50, LiquidityClass.LIQUID),
    SIP("Mutual Funds (SIP)", "📈", ContributionFrequency.MONTHLY, 0xFF5856D6, LiquidityClass.SEMI_LIQUID, 10.0),
    MUTUAL_FUND("Mutual Fund (Lump)", "💼", ContributionFrequency.ONE_TIME, 0xFF007AFF, LiquidityClass.SEMI_LIQUID, 10.0),
    STOCK("Stocks", "🏦", ContributionFrequency.ONE_TIME, 0xFF34C759, LiquidityClass.SEMI_LIQUID, 30.0),
    RECURRING_DEPOSIT("RD", "🔄", ContributionFrequency.MONTHLY, 0xFFFF9500, LiquidityClass.LIQUID),
    FIXED_DEPOSIT("FD", "🔒", ContributionFrequency.ONE_TIME, 0xFFFF6B6B, LiquidityClass.SEMI_LIQUID, 5.0),
    PPF("PPF", "🏛", ContributionFrequency.MONTHLY, 0xFF32ADE6, LiquidityClass.LOCKED),
    EPF("EPF", "👷", ContributionFrequency.MONTHLY, 0xFF64D2FF, LiquidityClass.LOCKED),
    GOLD("Gold", "🥇", ContributionFrequency.ONE_TIME, 0xFFFFCC00, LiquidityClass.SEMI_LIQUID, 15.0),
    REAL_ESTATE("Real Estate", "🏠", ContributionFrequency.ONE_TIME, 0xFF8E8E93, LiquidityClass.LOCKED),
    CRYPTO("Crypto", "₿", ContributionFrequency.ONE_TIME, 0xFFF7931A, LiquidityClass.SEMI_LIQUID, 50.0),
    INSURANCE("Insurance", "🛡", ContributionFrequency.MONTHLY, 0xFF30B0C7, LiquidityClass.LOCKED),
    OTHER("Other", "📦", ContributionFrequency.ONE_TIME, 0xFF9E9E9E, LiquidityClass.SEMI_LIQUID, 20.0)
}
