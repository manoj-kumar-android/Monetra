package com.monetra.presentation.screen.dashboard

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Transaction
import com.monetra.domain.repository.LoanRepository
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.domain.usecase.BackupValidationResult
import com.monetra.domain.usecase.intelligence.CalculateBurnRateUseCase
import com.monetra.domain.usecase.intelligence.CalculateSafeToSpendUseCase
import com.monetra.domain.usecase.intelligence.DetectRecurringExpensesUseCase
import com.monetra.domain.usecase.intelligence.GetCategoryBudgetsUseCase
import com.monetra.domain.usecase.intelligence.PrepareMonthlyBillsUseCase
import com.monetra.domain.usecase.transaction.GetMonthlySummaryUseCase
import com.monetra.domain.usecase.transaction.GetTransactionsUseCase
import com.monetra.domain.usecase.transaction.GetWeeklySummaryUseCase
import com.monetra.presentation.screen.transactions.CategoryBudgetUiModel
import com.monetra.presentation.screen.transactions.IntelligenceUiModel
import com.monetra.presentation.screen.transactions.RecurringExpenseUiModel
import com.monetra.presentation.screen.transactions.SummaryUiModel
import com.monetra.presentation.screen.transactions.TransactionUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val loanRepository: LoanRepository,
    private val prepareMonthlyBills: PrepareMonthlyBillsUseCase,
    private val cloudBackupRepository: com.monetra.domain.repository.CloudBackupRepository,
    private val validateBackupUseCase: com.monetra.domain.usecase.ValidateBackupUseCase,
    private val driveBackupManager: com.monetra.drivebackup.api.DriveBackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<DashboardEvent>()
    val events = _events.asSharedFlow()

    val isRestoring = cloudBackupRepository.isRestoring.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val selectedMonth = YearMonth.now()

    init {
        viewModelScope.launch {
            observeBackupEvents()
            observeSyncState()
            // Ensure bill instances for the current month exist before observing data
            prepareMonthlyBills(selectedMonth)
            observeDashboardData()
            
            // Attempt initial restore in background
            cloudBackupRepository.runRestore()
        }
    }

    private fun observeSyncState() {
        viewModelScope.launch {
            cloudBackupRepository.syncState.collect { state ->
                if (state is com.monetra.domain.model.SyncState.AccountMismatch) {
                    _events.emit(DashboardEvent.ShowAccountMismatch(state.currentEmail, state.lastSyncedEmail))
                }
            }
        }
    }

    private fun observeBackupEvents() {
        viewModelScope.launch {
            cloudBackupRepository.events.collect { event ->
                if (event is com.monetra.domain.repository.BackupEvent.AuthError) {
                    _events.emit(DashboardEvent.NavigateToWelcome)
                }
            }
        }
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
                monthlyExpenseRepository.getTotalReservedAmountForMonth(selectedMonth),
                loanRepository.getTotalMonthlyEmi(),
                cloudBackupRepository.syncState,
                cloudBackupRepository.accountName,
                cloudBackupRepository.lastBackupTime
            ) { results ->
                val summary = results[0] as com.monetra.domain.model.MonthlySummary
                val weekly = results[1] as com.monetra.domain.model.MonthlySummary
                val sts = results[2] as com.monetra.domain.model.SafeToSpend
                val burn = results[3] as com.monetra.domain.model.BurnRateAnalysis?
                val pref = results[4] as com.monetra.domain.model.UserPreferences
                val rec = results[5] as List<com.monetra.domain.model.RecurringExpense>
                val budgets = results[6] as List<com.monetra.domain.model.CategoryBudget>
                val txs = results[7] as List<Transaction>
                val currentReserved = results[8] as Double
                val totalEmi = results[9] as Double
                val syncState = results[10] as com.monetra.domain.model.SyncState
                val accountName = results[11] as String?
                val lastBackupTime = results[12] as Long?

                // Guard: if no income set, signal user to complete setup
                if (pref.monthlyIncome <= 0.0) {
                    return@combine DashboardUiState.NoSalarySet
                }

                val elapsedDays = (burn?.currentDay ?: 1).coerceAtLeast(1)
                val dailyAvg = (burn?.currentSpend ?: 0.0) / elapsedDays

                // 🏷️ Filter ACTIVE budgets (limit > 0)
                val activeBudgets = budgets.filter { it.limit > 0 }
                val totalBudgetAllocations = activeBudgets.sumOf { it.limit }

                DashboardUiState.Success(
                    rawDailyLimit = sts.dailyLimit,
                    rawFixedCosts = currentReserved,
                    dailySafeToSpend = "₹%,.0f".format(sts.remainingToday),
                    dailyLimit = "₹%,.0f".format(sts.dailyLimit),
                    stsPercent = sts.remainingPercent.coerceIn(0f, 1f),
                    summary = summary.toSummaryUiModel(pref.ownerName),
                    weeklyExpense = "₹%,.0f".format(weekly.totalExpense.coerceAtLeast(0.0)),
                    weeklyActivity = (0..6).map { 0.5f },
                    fixedCosts = "₹%,.0f".format(currentReserved), 
                    income = pref.monthlyIncome,
                    savingsGoal = pref.monthlySavingsGoal,
                    totalEmi = totalEmi,
                    budgetAllocations = totalBudgetAllocations,
                    intelligence = IntelligenceUiModel(
                        dailySafeToSpend = "₹%,.0f".format(sts.remainingToday),
                        projectedMonthEnd = "₹%,.2f".format(burn?.projectedEndMonthSpend ?: 0.0),
                        dailyAverage = "₹%,.2f".format(dailyAvg),
                        comparisonText = burn?.warningMessage ?: "Stable spending velocity",
                        burnRateStatus = if (burn?.isOverspending == true) "Critical" else "Stable"
                    ),
                    budgets = activeBudgets.map { it.toUiModel() },
                    recurringTotal = "₹%,.2f".format(rec.sumOf { it.amount }),
                    recurringItems = rec.map { it.toUiModel() },
                    recentTransactions = txs
                        .take(3)
                        .map { it.toUiItem() },
                    syncStatus = syncState,
                    isBackupEnabled = pref.isBackupEnabled,
                    accountName = accountName,
                    lastBackupTime = lastBackupTime
                )
            }
            .flowOn(Dispatchers.Default)
            .catch { throwable ->
                _uiState.value = DashboardUiState.Error(throwable.localizedMessage ?: "Error")
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private companion object {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    }

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
        formattedTime = java.time.Instant.ofEpochMilli(updatedAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()
            .format(timeFormatter),
        isIncome = type == com.monetra.domain.model.TransactionType.INCOME,
        categoryEmoji = TransactionUiItem.getEmojiForCategory(category)
    )

    private fun com.monetra.domain.model.MonthlySummary.toSummaryUiModel(ownerName: String) = SummaryUiModel(
        ownerName = ownerName,
        formattedBalance = "₹%,.2f".format(balance),
        formattedIncome = "₹%,.2f".format(totalIncome),
        formattedExpense = "₹%,.2f".format(totalExpense),
        formattedReserved = "₹%,.2f".format(reservedAmount),
        formattedAvailable = "₹%,.2f".format(availableBalance)
    )

    private fun com.monetra.domain.model.CategoryBudget.toUiModel() = CategoryBudgetUiModel(
        categoryName = categoryName,
        limit = "₹%,.0f".format(limit),
        spent = "₹%,.0f".format(currentSpent),
        remaining = "₹%,.0f".format(limit - currentSpent),
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
        nextDate = nextExpectedDate.format(dateFormatter),
        isStabilityHigh = isStabilityHigh
    )

    fun onSyncClick(confirmed: Boolean = false) {
        viewModelScope.launch {
            val result = validateBackupUseCase(ignoreBackupCheck = confirmed)
            handleValidationResult(result, confirmed)
        }
    }

    private suspend fun handleValidationResult(result: BackupValidationResult, confirmed: Boolean) {
        when (result) {
            is BackupValidationResult.Success -> {
                cloudBackupRepository.runSync()
            }

            is BackupValidationResult.NotSignedIn -> {
                _events.emit(DashboardEvent.ShowSignIn)
            }

            is BackupValidationResult.PermissionMissing -> {
                // permissionIntent is already observed in Fragment/Compose via ViewModel.recoveryIntent
            }

            is BackupValidationResult.AccountMismatch -> {
                _events.emit(
                    DashboardEvent.ShowAccountMismatch(
                        result.currentEmail,
                        result.syncedEmail
                    )
                )
            }

            is BackupValidationResult.BackupExistsConfirmation -> {
                _events.emit(DashboardEvent.ShowBackupConfirmation(result.email))
            }

            is BackupValidationResult.NoBackupFound -> {
                cloudBackupRepository.runSync()
            }
        }
    }

    fun onSignInClick(activity: Activity) {
        viewModelScope.launch {
            val success = driveBackupManager.authenticate(activity)
            if (success) {
                onSyncClick()
            }
        }
    }

    fun onSignOutClick() {
        viewModelScope.launch { cloudBackupRepository.signOut() }
    }
    
    val recoveryIntent = cloudBackupRepository.recoveryIntent
}
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object NoSalarySet : DashboardUiState
    data class Success(
        val rawDailyLimit: Double,
        val rawFixedCosts: Double,
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
        val budgetAllocations: Double,
        val intelligence: IntelligenceUiModel,
        val budgets: List<CategoryBudgetUiModel>,
        val recurringTotal: String,
        val recurringItems: List<RecurringExpenseUiModel>,
        val recentTransactions: List<TransactionUiItem>,
        val syncStatus: com.monetra.domain.model.SyncState = com.monetra.domain.model.SyncState.Idle,
        val isBackupEnabled: Boolean = false,
        val accountName: String? = null,
        val lastBackupTime: Long? = null
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

sealed interface DashboardEvent {
    data object NavigateToWelcome : DashboardEvent
    data object ShowSignIn : DashboardEvent
    data class ShowAccountMismatch(val currentEmail: String, val lastSyncedEmail: String) : DashboardEvent
    data class ShowBackupConfirmation(val email: String) : DashboardEvent
}
