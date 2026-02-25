package com.monetra.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Transaction
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.domain.usecase.intelligence.*
import com.monetra.domain.usecase.transaction.GetMonthlySummaryUseCase
import com.monetra.domain.usecase.transaction.GetTransactionsUseCase
import com.monetra.domain.usecase.transaction.GetWeeklySummaryUseCase
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.LoanRepository
import com.monetra.presentation.screen.transactions.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val getMonthlySummary: GetMonthlySummaryUseCase,
    private val calculateSafeToSpend: CalculateSafeToSpendUseCase,
    private val calculateBurnRate: CalculateBurnRateUseCase,
    private val getPreferences: UserPreferenceRepository,
    private val getCategoryBudgets: GetCategoryBudgetsUseCase,
    private val detectRecurringExpenses: DetectRecurringExpensesUseCase,
    private val getWeeklySummary: GetWeeklySummaryUseCase,
    private val monthlyExpenseRepository: MonthlyExpenseRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val selectedMonth = YearMonth.now()

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(
                getMonthlySummary(selectedMonth),
                getWeeklySummary(LocalDate.now()),
                calculateSafeToSpend(),
                calculateBurnRate(),
                getPreferences.getUserPreferences(),
                detectRecurringExpenses(),
                getCategoryBudgets(selectedMonth),
                getTransactions(selectedMonth),
                monthlyExpenseRepository.getTotalMonthlyExpenseAmount(),
                loanRepository.getTotalMonthlyEmi()
            ) { results ->
                val summary = results[0] as com.monetra.domain.model.MonthlySummary
                val weekly = results[1] as com.monetra.domain.model.MonthlySummary
                val sts = results[2] as com.monetra.domain.model.SafeToSpend
                val burn = results[3] as com.monetra.domain.model.BurnRateAnalysis?
                val pref = results[4] as com.monetra.domain.model.UserPreferences
                val rec = results[5] as List<com.monetra.domain.model.RecurringExpense>
                val budgets = results[6] as List<com.monetra.domain.model.CategoryBudget>
                val txs = results[7] as List<com.monetra.domain.model.Transaction>
                val fixedAmount = results[8] as Double
                val totalEmi = results[9] as Double

                // Guard: if no income set, signal user to complete setup
                if (pref.monthlyIncome <= 0.0) {
                    return@combine DashboardUiState.NoSalarySet
                }

                val elapsedDays = (burn?.currentDay ?: 1).coerceAtLeast(1)
                val dailyAvg = (burn?.currentSpend ?: 0.0) / elapsedDays

                DashboardUiState.Success(
                    dailySafeToSpend = "₹%,.0f".format(sts.remainingToday),
                    dailyLimit = "₹%,.0f".format(sts.dailyLimit),
                    stsPercent = sts.remainingPercent.coerceIn(0f, 1f),
                    summary = summary.toSummaryUiModel(pref.ownerName),
                    weeklyExpense = "₹%,.0f".format(weekly.totalExpense.coerceAtLeast(0.0)),
                    weeklyActivity = (0..6).map { 0.5f },
                    fixedCosts = "₹%,.0f".format(fixedAmount.coerceAtLeast(0.0)),
                    income = pref.monthlyIncome,
                    savingsGoal = pref.monthlySavingsGoal,
                    totalEmi = totalEmi,
                    intelligence = IntelligenceUiModel(
                        dailySafeToSpend = "₹%,.0f".format(sts.remainingToday),
                        projectedMonthEnd = "₹%,.2f".format(burn?.projectedEndMonthSpend ?: 0.0),
                        dailyAverage = "₹%,.2f".format(dailyAvg),
                        comparisonText = burn?.warningMessage ?: "Stable spending velocity",
                        burnRateStatus = if (burn?.isOverspending == true) "Critical" else "Stable"
                    ),
                    budgets = budgets.map { it.toUiModel() },
                    recurringTotal = "₹%,.2f".format(rec.sumOf { it.amount }),
                    recurringItems = rec.map { it.toUiModel() },
                    recentTransactions = txs.take(3).map { it.toUiItem() }
                )
            }.catch { throwable ->
                _uiState.value = DashboardUiState.Error(throwable.localizedMessage ?: "Error")
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())

    private fun Transaction.toUiItem() = TransactionUiItem(
        id = id,
        title = title,
        note = note,
        formattedAmount = buildString {
            append(if (type == com.monetra.domain.model.TransactionType.INCOME) "+" else "−")
            append("₹")
            append("%,.2f".format(amount))
        },
        formattedDate = date.format(dateFormatter),
        isIncome = type == com.monetra.domain.model.TransactionType.INCOME,
        categoryEmoji = when (category) {
            "Food" -> "🍔"
            "Transport" -> "🚗"
            "Shopping" -> "🛍️"
            "Entertainment" -> "🎭"
            "Utilities" -> "💡"
            "Salary" -> "💸"
            "Gift" -> "🎁"
            else -> "💰"
        }
    )

    private fun com.monetra.domain.model.MonthlySummary.toSummaryUiModel(ownerName: String) = SummaryUiModel(
        ownerName = ownerName,
        formattedBalance = "₹%,.2f".format(balance),
        formattedIncome = "₹%,.2f".format(totalIncome),
        formattedExpense = "₹%,.2f".format(totalExpense),
    )

    private fun com.monetra.domain.model.CategoryBudget.toUiModel() = CategoryBudgetUiModel(
        categoryName = categoryName,
        limit = "₹%,.0f".format(limit),
        spent = "₹%,.0f".format(currentSpent),
        progress = progress.coerceIn(0f, 1f),
        status = when {
            isAlert -> "Alert"
            isWarning -> "Warning"
            else -> "Normal"
        },
        progressColor = when {
            isAlert -> 0xFFFF3B30
            isWarning -> 0xFFFF9500
            else -> 0xFF34C759
        }
    )

    private fun com.monetra.domain.model.RecurringExpense.toUiModel() = RecurringExpenseUiModel(
        title = title,
        amount = "₹%,.2f".format(amount),
        nextDate = nextExpectedDate.format(DateTimeFormatter.ofPattern("dd MMM")),
        isStabilityHigh = isStabilityHigh
    )
}

@Serializable
data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object NoSalarySet : DashboardUiState
    data class Success(
        val dailySafeToSpend: String,
        val dailyLimit: String,
        val stsPercent: Float,
        val summary: SummaryUiModel,
        val weeklyExpense: String,
        val weeklyActivity: List<Float>,
        val fixedCosts: String,
        val income: Double,
        val savingsGoal: Double,
        val totalEmi: Double,
        val intelligence: IntelligenceUiModel,
        val budgets: List<CategoryBudgetUiModel>,
        val recurringTotal: String,
        val recurringItems: List<RecurringExpenseUiModel>,
        val recentTransactions: List<TransactionUiItem>
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
