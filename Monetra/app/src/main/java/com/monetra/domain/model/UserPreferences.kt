package com.monetra.domain.model

data class UserPreferences(
    val ownerName: String = "",
    val monthlyIncome: Double,
    val monthlySavingsGoal: Double,
    val isOnboardingCompleted: Boolean = false
)
