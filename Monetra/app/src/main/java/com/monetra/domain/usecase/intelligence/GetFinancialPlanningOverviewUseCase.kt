package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.*
import com.monetra.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetFinancialPlanningOverviewUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val loanRepository: LoanRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository,
    private val investmentRepository: InvestmentRepository
) {
    operator fun invoke(): Flow<PlanningOverview> {
        val today = LocalDate.now()
        val currentMonth = YearMonth.now()
        val daysPassed = today.dayOfMonth
        val totalDays = currentMonth.lengthOfMonth()
        val remainingDays = (totalDays - daysPassed + 1).coerceAtLeast(1)

        return combine(
            userPreferenceRepository.getUserPreferences(),
            transactionRepository.getLifetimeIncome(),
            transactionRepository.getLifetimeExpense(),
            transactionRepository.getTotalExpense(currentMonth),
            loanRepository.getTotalMonthlyEmi(),
            transactionRepository.getExpenseSumByCategory(currentMonth),
            monthlyExpenseRepository.getTotalMonthlyExpenseAmount(),
            investmentRepository.getTotalInvestmentValue()
        ) { values ->
            val prefs = values[0] as UserPreferences
            val lifetimeIncome = values[1] as Double
            val lifetimeExpense = values[2] as Double
            val monthlySpent = values[3] as Double
            val totalEmi = values[4] as Double
            val categoryMap = values[5] as Map<String, Double>
            val totalMonthlyExpense = values[6] as Double
            val totalInvestments = values[7] as Double
            
            val salary = prefs.monthlyIncome
            val targetSavings = prefs.monthlySavingsGoal
            
            // --- CARD 1: Monthly Safety ---
            val emiRatio = if (salary > 0) (totalEmi / salary) * 100 else 0.0
            val dailyAvg = if (daysPassed > 0) monthlySpent / daysPassed else 0.0
            val projectedSpend = dailyAvg * totalDays
            val disposableIncome = salary - totalEmi - targetSavings
            val isBurnRisk = projectedSpend > (salary - totalEmi)
            val currentSavingsPosition = salary - monthlySpent - totalEmi
            val savingsGap = (targetSavings - currentSavingsPosition).coerceAtLeast(0.0)
            
            val safetyStatus = when {
                savingsGap > (salary * 0.2) || emiRatio > 50 || isBurnRisk -> SafetyStatus.RED
                savingsGap > 0 || emiRatio > 35 -> SafetyStatus.YELLOW
                else -> SafetyStatus.GREEN
            }
            
            val topCategory = categoryMap.maxByOrNull { it.value }?.key ?: "Expenses"
            val suggestedAction = if (savingsGap > 0) {
                "To stay on track, reduce ₹%,.0f from $topCategory category.".format(savingsGap.coerceAtMost(monthlySpent))
            } else {
                "Great! You are on track to meet your savings goal."
            }
            
            val monthlySafety = MonthlySafetyAnalysis(
                status = safetyStatus,
                savingsGap = savingsGap,
                emiRatio = emiRatio,
                isBurnRisk = isBurnRisk,
                suggestedAction = suggestedAction
            )

            // --- CARD 2: Money Leakage ---
            val sortedCategories = categoryMap.toList().sortedByDescending { it.second }
            val leaks = sortedCategories.take(2).map { CategoryLeak(it.first, it.second) }
            val potentialSaving = (categoryMap.values.sum() * 0.15) // Estimated 15% optimization
            
            val leakageMessage = if (leaks.isNotEmpty()) {
                "You can save ₹%,.0f by reducing ${leaks.first().name} by 15%%.".format(leaks.first().amount * 0.15)
            } else "No major leakage detected yet."
            
            val moneyLeakage = MoneyLeakageAnalysis(
                topCategories = leaks,
                leakageMessage = leakageMessage,
                totalPotentialSaving = potentialSaving
            )

            // --- CARD 3: Emergency Safety ---
            val totalCash = lifetimeIncome - lifetimeExpense
            val essentialExpense = totalEmi + totalMonthlyExpense + (salary * 0.3) // EMI + Fixed + 30% for basic living
            val monthsCovered = if (essentialExpense > 0) totalCash / essentialExpense else 0.0
            
            val emergencyStatus = when {
                monthsCovered < 1.0 -> SafetyStatus.RED
                monthsCovered < 3.0 -> SafetyStatus.YELLOW
                else -> SafetyStatus.GREEN
            }
            
            val emergencySuggestion = if (monthsCovered < 6.0) {
                "Target: 6 months (₹%,.0f). You need ₹%,.0f more.".format(essentialExpense * 6, (essentialExpense * 6 - totalCash).coerceAtLeast(0.0))
            } else null
            
            val emergencySafety = EmergencySafetyAnalysis(
                monthsCovered = monthsCovered,
                status = emergencyStatus,
                suggestion = emergencySuggestion
            )

            // --- CARD 5: 7-Day Control Plan ---
            val remainingMonthlyIncome = (salary - monthlySpent - totalEmi - targetSavings).coerceAtLeast(0.0)
            val weeklyLimit = (remainingMonthlyIncome / remainingDays) * 7
            
            val controlPlan = WeeklyControlPlan(
                weeklyLimit = weeklyLimit,
                highRiskCategory = topCategory
            )

            PlanningOverview(
                monthlySafety = monthlySafety,
                moneyLeakage = moneyLeakage,
                emergencySafety = emergencySafety,
                controlPlan = controlPlan,
                totalEmi = totalEmi,
                totalInvestments = totalInvestments
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
