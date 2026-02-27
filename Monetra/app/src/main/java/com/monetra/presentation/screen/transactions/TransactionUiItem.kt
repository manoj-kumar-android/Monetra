package com.monetra.presentation.screen.transactions
import androidx.compose.runtime.Immutable

/**
 * Presentation-layer model for a single transaction row.
 *
 * Why not pass [com.monetra.domain.model.Transaction] directly to the row?
 *
 *   Transaction.date is java.time.LocalDate — a Java class. Compose's
 *   compiler plugin cannot introspect Java classes, so it conservatively
 *   marks them as UNSTABLE. A single unstable field makes the entire
 *   enclosing data class unstable. The result: Compose always recomposes
 *   ExpenseRow, even when nothing visible has changed.
 *
 *   By mapping to TransactionUiItem — whose every field is a Kotlin
 *   primitive or String — the compiler can prove the class is stable and
 *   will skip recomposition of ExpenseRow when the same instance is passed.
 *
 * Pre-formatting rules:
 *   - All display strings (formattedAmount, formattedDate) are computed once
 *     in the ViewModel, not inside the composable.
 *   - The composable receives finished strings and renders them verbatim.
 *   - No formatting, no business logic, no conditional string building
 *     happens inside any @Composable function.
 *
 * Stable field types:
 *   Long    — primitive-backed, stable
 *   String  — immutable, stable
 *   Boolean — primitive-backed, stable
 */
@Immutable
data class TransactionUiItem(
    val id: Long,
    val title: String,
    val note: String,
    val formattedAmount: String,  // e.g. "+₹1,500.00" or "−₹320.00"
    val formattedDate: String,    // e.g. "24 Feb"
    val isIncome: Boolean,        // drives color; avoids passing an enum to the row
    val categoryEmoji: String,
)

@Immutable
data class SummaryUiModel(
    val ownerName: String = "",
    val formattedBalance: String,
    val formattedIncome: String,
    val formattedExpense: String,
    val formattedReserved: String = "₹0.00",
    val formattedAvailable: String = "₹0.00"
)
