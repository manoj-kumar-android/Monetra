package com.monetra.domain.model

data class Investment(
    val id: Long = 0L,
    val name: String,
    val type: InvestmentType,
    val currentValuation: Double,
    val investedAmount: Double,
    // NEW: For SIP/RD/recurring investments — amount per month
    val monthlyAmount: Double = 0.0,
    // NEW: true = monthly contribution (SIP, RD etc.), false = lump sum
    val isMonthly: Boolean = false
)

enum class LiquidityClass {
    LIQUID,         // 100% value
    SEMI_LIQUID,    // Haircut applied (e.g., 70% value)
    LOCKED          // 0% value for emergency runway
}

enum class InvestmentType(
    val displayName: String,
    val emoji: String,
    val defaultMonthly: Boolean,
    val colorHex: Long,
    val liquidityClass: LiquidityClass,
    val haircut: Double = 0.0 // Percentage to REDUCE value by (e.g., 30.0 for semi-liquid)
) {
    CASH("Cash / Savings", "💵", false, 0xFF4CAF50, LiquidityClass.LIQUID),
    SIP("Mutual Funds (SIP)", "📈", true,  0xFF5856D6, LiquidityClass.SEMI_LIQUID, 10.0),
    MUTUAL_FUND("Mutual Fund", "💼", false, 0xFF007AFF, LiquidityClass.SEMI_LIQUID, 10.0),
    STOCK("Stocks", "🏦", false, 0xFF34C759, LiquidityClass.SEMI_LIQUID, 30.0),
    RECURRING_DEPOSIT("RD", "🔄", true,  0xFFFF9500, LiquidityClass.LIQUID),
    FIXED_DEPOSIT("FD", "🔒", false, 0xFFFF6B6B, LiquidityClass.SEMI_LIQUID, 5.0),
    PPF("PPF", "🏛", true,  0xFF32ADE6, LiquidityClass.LOCKED),
    EPF("EPF", "👷", true,  0xFF64D2FF, LiquidityClass.LOCKED),
    GOLD("Gold", "🥇", false, 0xFFFFCC00, LiquidityClass.SEMI_LIQUID, 15.0),
    REAL_ESTATE("Real Estate", "🏠", false, 0xFF8E8E93, LiquidityClass.LOCKED),
    CRYPTO("Crypto", "₿", false, 0xFFF7931A, LiquidityClass.SEMI_LIQUID, 50.0),
    INSURANCE("Insurance", "🛡", true,  0xFF30B0C7, LiquidityClass.LOCKED),
    OTHER("Other", "📦", false, 0xFF9E9E9E, LiquidityClass.SEMI_LIQUID, 20.0)
}
