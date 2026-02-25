package com.monetra.domain.model

data class InvestmentSuggestion(
    val category: String,
    val title: String,
    val message: String,
    val suitabilityScore: Int // 0-100 indicating how suitable this is for the user right now
)
