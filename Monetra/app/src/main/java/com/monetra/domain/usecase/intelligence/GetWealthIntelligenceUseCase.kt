package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.*
import com.monetra.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.pow

class GetWealthIntelligenceUseCase @Inject constructor(
    private val investmentRepository: InvestmentRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository,
    private val loanRepository: LoanRepository
) {
    operator fun invoke(): Flow<WealthIntelligence> {
        return combine(
            investmentRepository.getInvestments(),
            userPreferenceRepository.getUserPreferences(),
            monthlyExpenseRepository.getTotalMonthlyExpenseAmount(),
            loanRepository.getTotalMonthlyEmi()
        ) { investments, prefs, fixedCosts, totalEmi ->
            
            val income = prefs.monthlyIncome
            
            // 1. Core Totals
            var totalNetWorth = 0.0
            var totalInvestedCapital = 0.0
            var liquidNetWorth = 0.0
            var semiLiquidAdjustedValue = 0.0
            var lockedAssetsValue = 0.0
            var totalMonthlySIP = 0.0
            val today = java.time.LocalDate.now()

            investments.forEach { inv ->
                val type = inv.type
                val currentValue = inv.calculateCurrentValue(today)
                val investedAmount = inv.calculateTotalInvested(today)
                
                totalNetWorth += currentValue
                totalInvestedCapital += investedAmount
                
                if (inv.frequency == ContributionFrequency.MONTHLY) {
                    totalMonthlySIP += inv.monthlyAmount
                }

                when (type.liquidityClass) {
                    LiquidityClass.LIQUID -> {
                        liquidNetWorth += currentValue
                    }
                    LiquidityClass.SEMI_LIQUID -> {
                        semiLiquidAdjustedValue += currentValue * (1.0 - (type.haircut / 100.0))
                    }
                    LiquidityClass.LOCKED -> {
                        lockedAssetsValue += currentValue
                    }
                }
            }

            val totalReturns = (totalNetWorth - totalInvestedCapital).coerceAtLeast(0.0)

            // 2. Emergency Runway
            val essentialExpenses = totalEmi + fixedCosts + (income * 0.25)
            val runwayBuffer = liquidNetWorth + semiLiquidAdjustedValue
            val runwayMonths = if (essentialExpenses > 0) runwayBuffer / essentialExpenses else 99.0

            // 3. Asset Allocation
            val allocation = investments.groupBy { it.type }
                .map { (type, list) ->
                    val typeValue = list.sumOf { it.calculateCurrentValue(today) }
                    AssetAllocationItem(
                        type = type,
                        value = typeValue,
                        percentage = if (totalNetWorth > 0) (typeValue / totalNetWorth) * 100.0 else 0.0
                    )
                }.sortedByDescending { it.value }

            // 4. Wealth Projection
            val annualRate = prefs.projectionRate / 100.0
            val monthlyRate = annualRate / 12.0
            
            val years = listOf(1, 5, 10, 15, 20, 25, 30)
            val milestones = years.associateWith { y ->
                forecast(totalNetWorth, totalMonthlySIP, monthlyRate, y * 12)
            }
            
            val projectionYears = prefs.projectionYears
            val finalWealthValue = forecast(totalNetWorth, totalMonthlySIP, monthlyRate, projectionYears * 12)
            val totalFutureInvestmentPrincipal = totalNetWorth + (totalMonthlySIP * projectionYears * 12)
            val totalFutureReturnsAmount = (finalWealthValue - totalFutureInvestmentPrincipal).coerceAtLeast(0.0)

            // 5. Safety Status
            val (status, message) = when {
                runwayMonths < 3.0 -> SafetyStatus.RED to "Financial Alert: Your runway is less than 3 months. Focus on building liquid savings."
                runwayMonths < 6.0 -> SafetyStatus.YELLOW to "Financial Caution: You have a moderate buffer, but aim for 6+ months for complete peace of mind."
                else -> SafetyStatus.GREEN to "Financial Fortress: You are well protected. Your wealth can sustain you for ${"%.1f".format(runwayMonths)} months."
            }

            // 6. Smart Insights
            val insights = mutableListOf<WealthInsight>()
            
            if (runwayMonths < 3.0) {
                insights.add(WealthInsight("Low Liquidity", "Your emergency fund is thin. Avoid new EMIs for now.", InsightType.ALERT))
            }
            
            val sipRatio = if (income > 0) (totalMonthlySIP / income) * 100.0 else 0.0
            if (sipRatio > 40.0) {
                insights.add(WealthInsight("High Commitment", "You are investing >40% of income. Ensure you have enough daily cash flow.", InsightType.WARNING))
            } else if (sipRatio < 15.0 && income > 80000) {
                insights.add(WealthInsight("Wealth Builder", "Increase your SIPs to 20%+ to accelerate your path to freedom.", InsightType.INFO))
            }

            val cryptoValue = investments.filter { it.type == InvestmentType.CRYPTO }.sumOf { it.calculateCurrentValue(today) }
            if (totalNetWorth > 0 && (cryptoValue / totalNetWorth) > 0.15) {
                insights.add(WealthInsight("High Volatility", "Crypto is >15% of your wealth. Consider rebalancing to safer assets.", InsightType.WARNING))
            }
            
            if (runwayMonths > 6.0 && liquidNetWorth > (essentialExpenses * 12)) {
                insights.add(WealthInsight("Idle Cash", "You have surplus cash sitting idle. Move some to higher yield investments.", InsightType.SUCCESS))
            }

            WealthIntelligence(
                totalNetWorth = totalNetWorth,
                liquidNetWorth = liquidNetWorth,
                semiLiquidAdjustedValue = semiLiquidAdjustedValue,
                lockedAssetsValue = lockedAssetsValue,
                emergencyRunwayMonths = runwayMonths,
                totalMonthlySIP = totalMonthlySIP,
                sipAsPercentageOfIncome = sipRatio,
                assetAllocation = allocation,
                wealthProjection = WealthProjection(
                    expectedValue1Year = milestones[1] ?: 0.0,
                    expectedValue5Years = milestones[5] ?: 0.0,
                    monthlyContribution = totalMonthlySIP,
                    totalInvested = totalFutureInvestmentPrincipal,
                    totalReturns = totalFutureReturnsAmount,
                    finalWealth = finalWealthValue,
                    yearlyMilestones = milestones,
                    interestRate = prefs.projectionRate,
                    projectionYears = projectionYears
                ),
                safetyStatus = status,
                safetyMessage = message,
                insights = insights.take(3)
            )
        }
    }

    fun forecast(currentValue: Double, monthlySIP: Double, monthlyRate: Double, months: Int): Double {
        // Compound interest formula for lump sum + annuity for SIP
        val futureValueLumpSum = currentValue * (1 + monthlyRate).pow(months)
        val futureValueSIP = if (monthlyRate > 0) {
            monthlySIP * ((1 + monthlyRate).pow(months) - 1) / monthlyRate
        } else {
            monthlySIP * months
        }
        return futureValueLumpSum + futureValueSIP
    }
}
