package com.monetra.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow

enum class ContributionFrequency {
    ONE_TIME, MONTHLY
}

data class Investment(
    val id: Long = 0L,
    val name: String,
    val type: InvestmentType,
    val startDate: LocalDate,
    val amount: Double = 0.0, // Lump sum amount (Principal for ONE_TIME)
    val monthlyAmount: Double = 0.0, // Recurring amount (Principal for MONTHLY)
    val interestRate: Double = 0.0,
    val currentValue: Double = 0.0,
    val frequency: ContributionFrequency
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
     * The actual amount of money the user has "put in" to this investment.
     */
    fun calculateTotalInvested(today: LocalDate = LocalDate.now()): Double {
        val months = monthsElapsed(today)
        return when (frequency) {
            ContributionFrequency.ONE_TIME -> amount
            ContributionFrequency.MONTHLY -> monthlyAmount * months
        }
    }

    /**
     * The estimated or actual current value of the investment.
     * Some types are MARKET-based (user edits), others are LOGIC-based (interest rate).
     */
    fun calculateCurrentValue(today: LocalDate = LocalDate.now()): Double {
        return when (type) {
            // Market-based: Return the stored currentValue (which is user-editable)
            InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.GOLD, 
            InvestmentType.MUTUAL_FUND, InvestmentType.OTHER, InvestmentType.REAL_ESTATE -> currentValue

            // Cash: Always equals principal
            InvestmentType.CASH -> amount

            // Insurance: User rule "monthly premium × monthsElapsed"
            InvestmentType.INSURANCE -> monthlyAmount * monthsElapsed(today)

            // Logic-based: Calculate using interest rate
            else -> {
                val months = monthsElapsed(today)
                if (frequency == ContributionFrequency.MONTHLY) {
                    val r = interestRate / 12.0 / 100.0
                    if (r > 0 && months > 0) {
                        // Formula: P * [((1 + r)^n - 1) / r] * (1 + r)
                        monthlyAmount * ((1 + r).pow(months.toDouble()) - 1) / r * (1 + r)
                    } else {
                        monthlyAmount * months.toDouble()
                    }
                } else {
                    // One-time: P * (1 + r)^t
                    amount * (1 + interestRate / 100.0).pow(yearsElapsed(today))
                }
            }
        }
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
