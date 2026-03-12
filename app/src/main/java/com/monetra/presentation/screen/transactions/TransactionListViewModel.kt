package com.monetra.presentation.screen.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import com.monetra.domain.model.TransactionFilters
import com.monetra.domain.model.TransactionSummary
import com.monetra.domain.usecase.transaction.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.monetra.data.worker.PendingDeleteManager
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val getPagedTransactions: GetPagedTransactionsUseCase,
    private val getFilterSummary: GetFilterSummaryUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val getTransactionById: GetTransactionByIdUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val calculateSafeToSpend: com.monetra.domain.usecase.intelligence.CalculateSafeToSpendUseCase,
    private val calculateBurnRate: com.monetra.domain.usecase.intelligence.CalculateBurnRateUseCase,
    private val updateUserPreferences: com.monetra.domain.usecase.intelligence.UpdateUserPreferencesUseCase,
    private val getPreferences: com.monetra.domain.repository.UserPreferenceRepository,
    private val getCategoryBudgets: com.monetra.domain.usecase.intelligence.GetCategoryBudgetsUseCase,
    private val updateCategoryBudget: com.monetra.domain.usecase.intelligence.UpdateCategoryBudgetUseCase,
    private val detectRecurringExpenses: com.monetra.domain.usecase.intelligence.DetectRecurringExpensesUseCase,
    private val getUsedCategories: GetUsedCategoriesUseCase,
    private val getAmountRange: GetAmountRangeUseCase,
    private val pendingDeleteManager: PendingDeleteManager
) : ViewModel() {

    // ── Events ────────────────────────────────────────────────────────────

    private val _events = Channel<ExpenseEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── Internal driving state ────────────────────────────────────────────
    
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow<TransactionType?>(null)
    val filterCategories = MutableStateFlow<List<String>>(emptyList())
    val filterStartDate = MutableStateFlow<LocalDate?>(null)
    val filterEndDate = MutableStateFlow<LocalDate?>(null)
    val filterMinAmount = MutableStateFlow<Double?>(null)
    val filterMaxAmount = MutableStateFlow<Double?>(null)

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val _pendingDeleteIds = pendingDeleteManager.getPendingIds("TRANSACTION")

    // Dynamic categories based on type
    val availableCategories: StateFlow<List<String>> = filterType
        .flatMapLatest { getUsedCategories(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Database amount range for the slider
    val databaseAmountRange: StateFlow<Pair<Double, Double>> = getAmountRange()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0 to 100000.0)

    val activeFilters = combine(
        listOf(
            searchQuery.debounce(300),
            filterType,
            filterCategories,
            filterStartDate,
            filterEndDate,
            filterMinAmount,
            filterMaxAmount
        )
    ) { arr ->
        val query = arr[0] as String
        val type = arr[1] as? TransactionType
        val categories = arr[2] as List<String>
        val start = arr[3] as? LocalDate
        val end = arr[4] as? LocalDate
        val min = arr[5] as? Double
        val max = arr[6] as? Double

        TransactionFilters(
            query = query.takeIf { it.isNotBlank() },
            type = type,
            categories = categories.takeIf { it.isNotEmpty() },
            startDate = start,
            endDate = end,
            minAmount = min,
            maxAmount = max
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionFilters())

    val pagedTransactions: Flow<PagingData<TransactionHistoryItem>> = activeFilters
        .flatMapLatest { filters ->
            getPagedTransactions(filters)
                .map { pagingData ->
                    pagingData.map { tx -> TransactionHistoryItem.Transaction(tx.toUiItem()) }
                        .insertSeparators { before, after ->
                            val beforeMonth = before?.uiItem?.fullDate?.let { YearMonth.from(it) }
                            val afterMonth = after?.uiItem?.fullDate?.let { YearMonth.from(it) }
                            
                            if (afterMonth != null && (beforeMonth == null || beforeMonth != afterMonth)) {
                                TransactionHistoryItem.MonthHeader(
                                    afterMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
                                )
                            } else {
                                null
                            }
                        }
                }
        }
        .cachedIn(viewModelScope)

    val filterSummary = activeFilters
        .flatMapLatest { getFilterSummary(it) }
        .map { summary ->
            SummaryUiModel(
                formattedBalance = "₹%,.2f".format(summary.totalIncome - summary.totalExpense),
                formattedIncome = "₹%,.2f".format(summary.totalIncome),
                formattedExpense = "₹%,.2f".format(summary.totalExpense),
                netAmount = summary.netAmount
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryUiModel(formattedBalance="₹0.00", formattedIncome="₹0.00", formattedExpense="₹0.00"))

    val incomeInput = MutableStateFlow("")
    val isIncomeDialogOpen = MutableStateFlow(false)

    val budgetCategoryInput = MutableStateFlow("General")
    val budgetLimitInput = MutableStateFlow("")
    val isBudgetDialogOpen = MutableStateFlow(false)

    // ── Reactive pipeline ─────────────────────────────────────────────────
    // All reactive state is derived from activeFilters above.

    // ── Domain → UI model mapping ─────────────────────────────────────────
    // Private to this file. Formatting logic lives here, not in composables.

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

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
        formattedTime = java.time.Instant.ofEpochMilli(updatedAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()
            .format(timeFormatter),
        isIncome = type == TransactionType.INCOME,
        categoryEmoji = TransactionUiItem.getEmojiForCategory(category),
        fullDate = date
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

    fun clearAllFilters() {
        filterType.value = null
        filterCategories.value = emptyList()
        filterStartDate.value = null
        filterEndDate.value = null
        filterMinAmount.value = null
        filterMaxAmount.value = null
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onFilterTypeChanged(type: TransactionType?) {
        filterType.value = type
    }

    fun onCategorySelected(category: String) {
        val currentList = filterCategories.value
        if (category in currentList) {
            filterCategories.value = currentList - category
        } else {
            filterCategories.value = currentList + category
        }
    }

    fun removeCategoryFilter(category: String) {
        filterCategories.value -= category
    }

    fun onDateRangeSelected(start: LocalDate?, end: LocalDate?) {
        filterStartDate.value = start
        filterEndDate.value = end
    }

    fun onAmountRangeChanged(min: Double?, max: Double?) {
        filterMinAmount.value = min
        filterMaxAmount.value = max
    }

    fun clearDateFilter() {
        filterStartDate.value = null
        filterEndDate.value = null
    }

    fun onResetMonth() {
        selectedMonth.value = YearMonth.now()
    }

    fun onFilterSelected(filter: TransactionFilter) {
        // Compatibility for old UI if needed
        when(filter) {
            TransactionFilter.ALL -> filterType.value = null
            TransactionFilter.INCOME -> filterType.value = TransactionType.INCOME
            TransactionFilter.EXPENSE -> filterType.value = TransactionType.EXPENSE
        }
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

    private var lastDeletedId: Long = 0L

    fun onDeleteClick(id: Long) {
        viewModelScope.launch {
            try {
                val tx = getTransactionById(id) ?: return@launch
                lastDeletedId = id
                pendingDeleteManager.requestDelete(id, tx.remoteId, "TRANSACTION")
                _events.send(ExpenseEvent.ShowUndoSnackbar("Transaction deleted"))
            } catch (e: Exception) {
                _events.send(ExpenseEvent.ShowError("Could not delete transaction"))
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            pendingDeleteManager.cancelDelete(lastDeletedId, "TRANSACTION")
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
        // Paging 3 automatically refreshes if the database changes.
        // If a manual trigger is needed, we could add a pull-to-refresh mechanism.
    }
}
