package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.*
import com.monetra.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.YearMonth
import javax.inject.Inject

class SimulateFinancialScenarioUseCase @Inject constructor(
    private val generateReport: GenerateMonthlyFinancialReportUseCase,
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(month: YearMonth, params: SimulationParams): Flow<SimulationResult?> {
        return subscriptionRepository.getSubscriptionStatus().flatMapLatest { subscription ->
            // Made free for now
            // if (!subscription.isPremium) {
            //     return@flatMapLatest flowOf(null)
            // }

            generateReport(month).flatMapLatest { report ->
                val projectedIncome = report.income + params.salaryDelta
                val projectedEmis = report.totalEmis + params.newEmiAmount
                val projectedInvestments = report.totalInvestments + params.newSipAmount
                val projectedTargetSavings = report.targetSavings + params.savingsTargetDelta
                
                // Living expenses remain same in basic simulation
                val projectedExpenses = report.totalExpenses
                
                val totalMandatoryOutflows = projectedExpenses + projectedEmis
                val projectedActualSavings = projectedIncome - totalMandatoryOutflows
                val projectedSavingsGap = (projectedTargetSavings - projectedActualSavings).coerceAtLeast(0.0)

                // Recalculate Ratios
                val emiRatio = if (projectedIncome > 0) (projectedEmis / projectedIncome) * 100 else 0.0
                val investmentRatio = if (projectedIncome > 0) (projectedInvestments / projectedIncome) * 100 else 0.0
                
                // Calculate Status
                val status = when {
                    projectedIncome <= 0 -> FinancialBalanceStatus.RISK
                    emiRatio > 40.0 || projectedActualSavings < 0 -> FinancialBalanceStatus.RISK
                    emiRatio > 30.0 || projectedActualSavings < projectedTargetSavings -> FinancialBalanceStatus.MODERATE
                    else -> FinancialBalanceStatus.HEALTHY
                }

                // Simplified Health Score for What-If
                var score = 50
                if (projectedActualSavings >= projectedTargetSavings) score += 30
                else if (projectedActualSavings > 0) score += 15
                
                if (emiRatio < 20) score += 20
                else if (emiRatio < 35) score += 10
                
                if (investmentRatio > 15) score += 20
                else if (investmentRatio > 5) score += 10
                
                if (projectedActualSavings < 0) score -= 40
                
                val result = SimulationResult(
                    month = month,
                    currentIncome = report.income,
                    currentExpenses = report.totalExpenses,
                    currentEmis = report.totalEmis,
                    currentInvestments = report.totalInvestments,
                    projectedIncome = projectedIncome,
                    projectedExpenses = projectedExpenses,
                    projectedEmis = projectedEmis,
                    projectedInvestments = projectedInvestments,
                    projectedSavings = projectedActualSavings,
                    projectedSavingsGap = projectedSavingsGap,
                    projectedEmiRatio = emiRatio,
                    projectedInvestmentRatio = investmentRatio,
                    projectedHealthScore = score.coerceIn(0, 100),
                    projectedStatus = status
                )
                
                flowOf(result)
            }
        }
    }
}
