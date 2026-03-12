package com.monetra.presentation.screen.transactions
import androidx.compose.runtime.Immutable

/**
 * Common interface for items in the transaction history list.
 */
sealed interface TransactionHistoryItem {
    @Immutable
    data class Transaction(val uiItem: TransactionUiItem) : TransactionHistoryItem
    
    @Immutable
    data class MonthHeader(val monthName: String) : TransactionHistoryItem
}

/**
 * Presentation-layer model for a single transaction row.
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
    val formattedTime: String = "",
    val fullDate: java.time.LocalDate? = null // Used for header grouping logic
)

@Immutable
data class SummaryUiModel(
    val ownerName: String = "",
    val formattedBalance: String,
    val formattedIncome: String,
    val formattedExpense: String,
    val formattedReserved: String = "₹0.00",
    val formattedAvailable: String = "₹0.00",
    val netAmount: Double = 0.0
)
