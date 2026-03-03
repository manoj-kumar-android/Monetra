package com.monetra.domain.model

data class WealthIntelligence(
    val totalNetWorth: Double,
    val totalInvestedCapital: Double,
    val currentTotalReturns: Double,
    val liquidNetWorth: Double, // 100% of Liquid Assets
    val semiLiquidAdjustedValue: Double, // Value after haircuts
    val lockedAssetsValue: Double, // 0% counted for runway
    val emergencyRunwayMonths: Double,
    val totalMonthlySIP: Double,
    val assetAllocation: List<AssetAllocationItem>,
    val wealthProjection: WealthProjection,
    val safetyStatus: SafetyStatus,
    val safetyMessage: String
)

data class AssetAllocationItem(
    val type: InvestmentType,
    val percentage: Double,
    val value: Double
)

data class WealthProjection(
    val monthlyContribution: Double,
    val totalInvested: Double = 0.0,
    val totalReturns: Double = 0.0,
    val finalWealth: Double = 0.0,
    val interestRate: Double = 10.0,
    val projectionYears: Int = 10
)

