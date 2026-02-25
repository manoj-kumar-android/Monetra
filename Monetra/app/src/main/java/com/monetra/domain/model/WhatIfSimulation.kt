package com.monetra.domain.model

import java.time.YearMonth

data class SimulationParams(
    val salaryDelta: Double = 0.0,
    val newEmiAmount: Double = 0.0,
    val newSipAmount: Double = 0.0,
    val savingsTargetDelta: Double = 0.0
)

data class SimulationResult(
    val month: YearMonth,
    val currentIncome: Double,
    val currentExpenses: Double,
    val currentEmis: Double,
    val currentInvestments: Double,
    val projectedIncome: Double,
    val projectedExpenses: Double,
    val projectedEmis: Double,
    val projectedInvestments: Double,
    val projectedSavings: Double,
    val projectedSavingsGap: Double,
    val projectedEmiRatio: Double,
    val projectedInvestmentRatio: Double,
    val projectedHealthScore: Int,
    val projectedStatus: FinancialBalanceStatus
)
