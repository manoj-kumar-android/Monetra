package com.monetra.presentation.screen.transactions

import androidx.compose.runtime.Immutable
import java.time.YearMonth

@Immutable
data class IntelligenceUiModel(
    val dailySafeToSpend: String = "₹0.00",
    val projectedMonthEnd: String = "₹0.00",
    val dailyAverage: String = "₹0.00",
    val comparisonText: String = "",
    val burnRateStatus: String = "Stable"
)

@Immutable
data class CategoryBudgetUiModel(
    val categoryName: String,
    val limit: String,
    val spent: String,
    val remaining: String,
    val progress: Float,
    val status: String, // "Normal", "Warning", "Alert"
    val progressColor: Long // Hex color for the progress bar
)

@Immutable
data class RecurringExpenseUiModel(
    val title: String,
    val amount: String,
    val nextDate: String,
    val isStabilityHigh: Boolean
)

// ── UI state ──────────────────────────────────────────────────────────────

sealed interface ExpenseUiState {

    /** Initial state while the first DB query is in flight. */
    data object Loading : ExpenseUiState

    /**
     * Data is ready and valid.
     *
     * [transactions] holds [TransactionUiItem] — a stable presentation model
     * with pre-formatted strings — rather than the raw domain [Transaction].
     * This keeps every field in the list stable so Compose can skip
     * recomposing rows that haven't changed.
     */
    @Immutable
    data class Success(
        val groupedTransactions: Map<String, List<TransactionUiItem>>,
        val summary: SummaryUiModel,
        val intelligence: IntelligenceUiModel = IntelligenceUiModel(),
        val budgets: List<CategoryBudgetUiModel> = emptyList(),
        val recurringTotal: String = "₹0.00",
        val recurringItems: List<RecurringExpenseUiModel> = emptyList(),
        val selectedMonth: YearMonth,
        val isCurrentMonth: Boolean = true,
        val activeFilter: TransactionFilter,
        val isRefreshing: Boolean = false,
        val transactionToDelete: Long? = null
    ) : ExpenseUiState

    /** [message] is already user-facing — formatted once in the ViewModel. */
    data class Error(val message: String) : ExpenseUiState
}

// ── Filter ────────────────────────────────────────────────────────────────

/** UI-layer enum. Filtering logic lives in the ViewModel, not in composables. */
enum class TransactionFilter { ALL, INCOME, EXPENSE }

// ── One-shot events ───────────────────────────────────────────────────────

/**
 * Side effects that happen exactly once. Emitted via Channel, collected with
 * LaunchedEffect. Never stored in [ExpenseUiState].
 */
sealed interface ExpenseEvent {
    data class NavigateToEdit(val transactionId: Long) : ExpenseEvent
    data object NavigateToAdd : ExpenseEvent
    data class ShowError(val message: String) : ExpenseEvent
    data class ShowUndoSnackbar(val message: String) : ExpenseEvent
}
