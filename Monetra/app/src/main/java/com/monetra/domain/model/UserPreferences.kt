package com.monetra.domain.model

data class UserPreferences(
    val ownerName: String = "",
    val monthlyIncome: Double = 0.0,
    val monthlySavingsGoal: Double = 0.0,
    val currentSavings: Double = 0.0,
    val isOnboardingCompleted: Boolean = false,
    val projectionRate: Double = 10.0,
    val projectionYears: Int = 10
)
