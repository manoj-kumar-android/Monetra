package com.monetra.domain.model

import java.time.YearMonth

data class MonthlyReport(
    val month: YearMonth,
    val totalSpent: Double,
    val totalIncome: Double,
    val topCategory: String,
    val topCategoryAmount: Double,
    val largestExpenseTitle: String,
    val largestExpenseAmount: Double,
    val previousMonthTotal: Double,
    val savingsRate: Double, // Percentage
    val budgetDisciplineScore: Int, // 0-100
    val budgetAdherenceCount: Int, // How many categories stayed within budget
    val totalBudgetsCount: Int,
    val personality: SpendingPersonality
)
