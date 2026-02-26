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
            investmentRepository.getTotalInvestmentValue(),
            transactionRepository.getTotalExpenseBetweenDates(today, today)
        ) { values ->
            val prefs = values[0] as UserPreferences
            val lifetimeIncome = values[1] as Double
            val lifetimeExpense = values[2] as Double
            val monthlySpent = values[3] as Double
            val totalEmi = values[4] as Double
            val categoryMap = values[5] as Map<String, Double>
            val totalMonthlyExpense = values[6] as Double
            val totalInvestments = values[7] as Double
            val todaySpent = values[8] as Double
            
            val salary = prefs.monthlyIncome
            val targetSavings = prefs.monthlySavingsGoal
            
            // --- 1: Monthly Guard ---
            val dailyAvg = if (daysPassed > 1) (monthlySpent - todaySpent) / (daysPassed - 1) else 0.0
            val currentSavingsPosition = salary - monthlySpent - totalEmi
            val savingsGap = (targetSavings - currentSavingsPosition).coerceAtLeast(0.0)
            
            val safetyStatus = when {
                savingsGap > (salary * 0.2) -> SafetyStatus.RED
                savingsGap > 0 -> SafetyStatus.YELLOW
                else -> SafetyStatus.GREEN
            }
            
            val topCategory = categoryMap.maxByOrNull { it.value }?.key ?: "Expenses"
            val suggestedAction = if (savingsGap > 0) {
                "Cut back on $topCategory to save ₹%,.0f more.".format(savingsGap)
            } else {
                "Perfect! Your savings goal is safe."
            }
            
            val monthlySafety = MonthlySafetyAnalysis(
                status = safetyStatus,
                savingsGap = savingsGap,
                emiRatio = if (salary > 0) (totalEmi / salary) * 100 else 0.0,
                isBurnRisk = (dailyAvg * totalDays) > (salary - totalEmi),
                suggestedAction = suggestedAction,
                dailyBurnRate = dailyAvg
            )

            // --- 2: Money Leakage ---
            val sortedCategories = categoryMap.toList().sortedByDescending { it.second }
            val leaks = sortedCategories.take(2).map { CategoryLeak(it.first, it.second) }
            
            val moneyLeakage = MoneyLeakageAnalysis(
                topCategories = leaks,
                leakageMessage = if (leaks.isNotEmpty()) "Saving 10%% on ${leaks.first().name} adds back ₹%,.0f.".format(leaks.first().amount * 0.1) else "Clean spending.",
                totalPotentialSaving = (categoryMap.values.sum() * 0.15)
            )

            // --- 3: Essential Stash ---
            val netWorth = lifetimeIncome - lifetimeExpense
            val fixedCosts = totalEmi + totalMonthlyExpense
            val monthlyBuffer = fixedCosts + (salary * 0.3) // Conservative survival
            val runway = if (monthlyBuffer > 0) netWorth / monthlyBuffer else 0.0
            
            val emergencySafety = EmergencySafetyAnalysis(
                monthsCovered = runway,
                status = if (runway >= 6) SafetyStatus.GREEN else if (runway >= 3) SafetyStatus.YELLOW else SafetyStatus.RED,
                suggestion = if (runway < 6) "Needs ₹%,.0f more to reach 6 mos safety.".format((monthlyBuffer * 6) - netWorth) else "You're safe."
            )

            // --- 4: Simple Daily Target ---
            // Available cash from today until end of month after goals.
            val safeLeft = (salary - totalEmi - targetSavings - (monthlySpent - todaySpent)).coerceAtLeast(0.0)
            val dailyTarget = safeLeft / remainingDays
            
            val controlPlan = WeeklyControlPlan(
                weeklyLimit = dailyTarget * 7,
                dailyLimit = dailyTarget,
                remainingToday = (dailyTarget - todaySpent).coerceAtLeast(0.0),
                daysPassedInWeek = today.dayOfWeek.value,
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
