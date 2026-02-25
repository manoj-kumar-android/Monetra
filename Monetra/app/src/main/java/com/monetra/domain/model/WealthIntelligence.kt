package com.monetra.domain.model

data class WealthIntelligence(
    val totalNetWorth: Double,
    val liquidNetWorth: Double, // 100% of Liquid Assets
    val semiLiquidAdjustedValue: Double, // Value after haircuts
    val lockedAssetsValue: Double, // 0% counted for runway
    val emergencyRunwayMonths: Double,
    val totalMonthlySIP: Double,
    val sipAsPercentageOfIncome: Double,
    val assetAllocation: List<AssetAllocationItem>,
    val wealthProjection: WealthProjection,
    val safetyStatus: SafetyStatus,
    val safetyMessage: String,
    val insights: List<WealthInsight>
)

data class AssetAllocationItem(
    val type: InvestmentType,
    val percentage: Double,
    val value: Double
)

data class WealthProjection(
    val expectedValue1Year: Double,
    val expectedValue5Years: Double,
    val monthlyContribution: Double
)

data class WealthInsight(
    val title: String,
    val message: String,
    val type: InsightType
)

enum class InsightType { INFO, WARNING, SUCCESS, ALERT }
