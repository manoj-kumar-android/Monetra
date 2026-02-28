package com.monetra.presentation.screen.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import com.monetra.domain.model.BurnRateAnalysis
import com.monetra.domain.model.SafeToSpend
import com.monetra.domain.usecase.transaction.AddTransactionUseCase
import com.monetra.domain.usecase.transaction.DeleteTransactionUseCase
import com.monetra.domain.usecase.transaction.GetMonthlySummaryUseCase
import com.monetra.domain.usecase.transaction.GetTransactionByIdUseCase
import com.monetra.domain.usecase.transaction.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val getMonthlySummary: GetMonthlySummaryUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val getTransactionById: GetTransactionByIdUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val calculateSafeToSpend: com.monetra.domain.usecase.intelligence.CalculateSafeToSpendUseCase,
    private val calculateBurnRate: com.monetra.domain.usecase.intelligence.CalculateBurnRateUseCase,
    private val updateUserPreferences: com.monetra.domain.usecase.intelligence.UpdateUserPreferencesUseCase,
    private val getPreferences: com.monetra.domain.repository.UserPreferenceRepository,
    private val getCategoryBudgets: com.monetra.domain.usecase.intelligence.GetCategoryBudgetsUseCase,
    private val updateCategoryBudget: com.monetra.domain.usecase.intelligence.UpdateCategoryBudgetUseCase,
    private val detectRecurringExpenses: com.monetra.domain.usecase.intelligence.DetectRecurringExpensesUseCase
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Loading)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    // ── Events ────────────────────────────────────────────────────────────

    private val _events = Channel<ExpenseEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── Internal driving state ────────────────────────────────────────────

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val activeFilter = MutableStateFlow(TransactionFilter.ALL)
    private var recentlyDeletedTransaction: Transaction? = null

    val incomeInput = MutableStateFlow("")
    val isIncomeDialogOpen = MutableStateFlow(false)

    val budgetCategoryInput = MutableStateFlow("General")
    val budgetLimitInput = MutableStateFlow("")
    val isBudgetDialogOpen = MutableStateFlow(false)

    val categories = listOf("General", "Food", "Transport", "Shopping", "Entertainment", "Utilities")

    private data class CoreIntelligence(
        val transactions: List<Transaction>,
        val summary: com.monetra.domain.model.MonthlySummary,
        val safeToSpend: com.monetra.domain.model.SafeToSpend,
        val burnRate: BurnRateAnalysis?,
        val preferences: com.monetra.domain.model.UserPreferences,
        val recurring: List<com.monetra.domain.model.RecurringExpense>
    )

    init {
        observeTransactions()
    }

    // ── Reactive pipeline ─────────────────────────────────────────────────

    private fun observeTransactions() {
        viewModelScope.launch {
            combine(
                selectedMonth,
                activeFilter
            ) { month, filter -> month to filter }.flatMapLatest { (month, filter) ->
                    combine(
                        combine(
                            combine(
                                getTransactions(month),
                                getMonthlySummary(month),
                                calculateSafeToSpend()
                            ) { t, s, sts -> Triple(t, s, sts) },
                            combine(
                                calculateBurnRate(),
                                getPreferences.getUserPreferences(),
                                detectRecurringExpenses()
                            ) { burn, pref, rec -> Triple(burn, pref, rec) }
                        ) { (t, s, sts), (burn, pref, rec) -> 
                            CoreIntelligence(t, s, sts, burn, pref, rec)
                        },
                        getCategoryBudgets(month)
                    ) { core, budgets ->
                        val filtered = applyFilter(core.transactions, filter)
                        val grouped = filtered.groupBy { it.date }
                            .toSortedMap(compareByDescending { it })
                            .mapKeys { (date, _) -> formatDateHeader(date) }
                            .mapValues { (_, txs) -> txs.map { it.toUiItem() } }

                        ExpenseUiState.Success(
                            groupedTransactions = grouped,
                            summary = core.summary.toSummaryUiModel(),
                            intelligence = IntelligenceUiModel(
                                dailySafeToSpend = "₹%,.2f".format(core.safeToSpend.remainingToday),
                                projectedMonthEnd = "₹%,.2f".format(core.burnRate?.projectedEndMonthSpend ?: 0.0),
                                dailyAverage = "₹%,.2f".format((core.burnRate?.currentSpend ?: 0.0) / (core.burnRate?.currentDay ?: 1).coerceAtLeast(1)),
                                comparisonText = core.burnRate?.warningMessage ?: "Stable spending trend",
                                burnRateStatus = when {
                                    core.burnRate?.isOverspending == true -> "Critical"
                                    else -> "Stable"
                                }
                            ),
                            budgets = budgets.map { it.toUiModel() },
                            recurringTotal = "₹%,.2f".format(core.recurring.sumOf { item -> item.amount }),
                            recurringItems = core.recurring.map { item -> item.toUiModel() },
                            selectedMonth = month,
                            isCurrentMonth = month == YearMonth.now(),
                            activeFilter = filter,
                        )
                    }
                }.catch { throwable ->
                    _uiState.value = ExpenseUiState.Error(
                        message = throwable.localizedMessage ?: "Something went wrong"
                    )
                }.collect { _uiState.value = it }
        }
    }

    // ── Business logic ────────────────────────────────────────────────────

    private fun applyFilter(
        transactions: List<Transaction>,
        filter: TransactionFilter,
    ): List<Transaction> = when (filter) {
        TransactionFilter.ALL -> transactions
        TransactionFilter.INCOME -> transactions.filter { it.type == TransactionType.INCOME }
        TransactionFilter.EXPENSE -> transactions.filter { it.type == TransactionType.EXPENSE }
    }

    // ── Domain → UI model mapping ─────────────────────────────────────────
    // Private to this file. Formatting logic lives here, not in composables.

    private val headerDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.getDefault())

    private fun formatDateHeader(date: java.time.LocalDate): String {
        val today = java.time.LocalDate.now()
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(headerDateFormatter)
        }
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())

    private fun Transaction.toUiItem() = TransactionUiItem(
        id = id,
        title = title,
        note = note,
        formattedAmount = buildString {
            append(if (type == TransactionType.INCOME) "+" else "−")
            append("₹")
            append("%,.2f".format(amount))
        },
        formattedDate = date.format(dateFormatter),
        isIncome = type == TransactionType.INCOME,
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

    private fun com.monetra.domain.model.MonthlySummary.toSummaryUiModel() = SummaryUiModel(
        formattedBalance = "₹%,.2f".format(balance),
        formattedIncome = "₹%,.2f".format(totalIncome),
        formattedExpense = "₹%,.2f".format(totalExpense),
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
            isAlert -> 0xFFFF3B30 // iOS Red
            isWarning -> 0xFFFF9500 // iOS Orange
            else -> 0xFF34C759 // iOS Green
        }
    )

    private fun com.monetra.domain.model.RecurringExpense.toUiModel() = RecurringExpenseUiModel(
        title = title,
        amount = "₹%,.2f".format(amount),
        nextDate = nextExpectedDate.format(DateTimeFormatter.ofPattern("dd MMM")),
        isStabilityHigh = isStabilityHigh
    )

    // ── Intent handlers ───────────────────────────────────────────────────
    // Callbacks accept primitive IDs — rows never hold the domain model.

    fun onMonthSelected(month: YearMonth) {
        selectedMonth.value = month
    }

    fun onResetMonth() {
        selectedMonth.value = YearMonth.now()
    }

    fun onFilterSelected(filter: TransactionFilter) {
        activeFilter.value = filter
    }

    fun onTransactionClick(id: Long) {
        viewModelScope.launch {
            _events.send(ExpenseEvent.NavigateToEdit(id))
        }
    }

    fun onAddClick() {
        viewModelScope.launch {
            _events.send(ExpenseEvent.NavigateToAdd)
        }
    }

    fun onDeleteClick(id: Long) {
        val currentState = _uiState.value as? ExpenseUiState.Success ?: return
        _uiState.value = currentState.copy(transactionToDelete = id)
    }

    fun dismissDeleteDialog() {
        val currentState = _uiState.value as? ExpenseUiState.Success ?: return
        _uiState.value = currentState.copy(transactionToDelete = null)
    }

    fun confirmDelete() {
        val currentState = _uiState.value as? ExpenseUiState.Success ?: return
        val id = currentState.transactionToDelete ?: return

        viewModelScope.launch {
            try {
                // Keep a reference to the deleted item for undo functionality
                recentlyDeletedTransaction = getTransactionById(id)
                deleteTransaction(id)
                
                // Clear the dialog state
                _uiState.value = currentState.copy(transactionToDelete = null)
                _events.send(ExpenseEvent.ShowUndoSnackbar("Transaction deleted"))
            } catch (e: Exception) {
                _events.send(ExpenseEvent.ShowError("Could not delete transaction"))
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            recentlyDeletedTransaction?.let { tx ->
                addTransaction(tx)
                recentlyDeletedTransaction = null
            }
        }
    }

    fun onSettingsClick() {
        viewModelScope.launch {
            getPreferences.getUserPreferences().collect { prefs ->
                incomeInput.value = prefs.monthlyIncome.toString()
                isIncomeDialogOpen.value = true
            }
        }
    }

    fun dismissIncomeDialog() {
        isIncomeDialogOpen.value = false
    }

    fun saveIncome() {
        val income = incomeInput.value.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            val currentPrefs = getPreferences.getUserPreferences().first()
            updateUserPreferences(
                ownerName = currentPrefs.ownerName,
                income = income,
                savingsGoal = currentPrefs.monthlySavingsGoal
            )
            isIncomeDialogOpen.value = false
        }
    }

    fun onIncomeInputChange(value: String) {
        incomeInput.value = value
    }

    fun onManageBudgetsClick() {
        isBudgetDialogOpen.value = true
    }

    fun dismissBudgetDialog() {
        isBudgetDialogOpen.value = false
    }

    fun onBudgetCategoryChange(category: String) {
        budgetCategoryInput.value = category
    }

    fun onBudgetLimitChange(limit: String) {
        budgetLimitInput.value = limit
    }

    fun saveBudget() {
        val limit = budgetLimitInput.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            updateCategoryBudget(budgetCategoryInput.value, limit)
            isBudgetDialogOpen.value = false
            budgetLimitInput.value = ""
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val currentState = _uiState.value as? ExpenseUiState.Success ?: return@launch
            _uiState.value = currentState.copy(isRefreshing = true)
            
            val updatedState = _uiState.value as? ExpenseUiState.Success ?: return@launch
            _uiState.value = updatedState.copy(isRefreshing = false)
        }
    }
}
