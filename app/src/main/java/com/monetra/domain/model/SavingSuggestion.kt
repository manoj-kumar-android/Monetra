package com.monetra.domain.model

data class SavingSuggestion(
    val title: String,
    val message: String,
    val potentialSavings: Double = 0.0,
    val type: SuggestionType,
    val priority: SuggestionPriority
)

enum class SuggestionType {
    EXPENSE_REDUCTION,
    DEBT_ADVICE,
    BUDGET_ADJUSTMENT,
    GENERAL
}

enum class SuggestionPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}
