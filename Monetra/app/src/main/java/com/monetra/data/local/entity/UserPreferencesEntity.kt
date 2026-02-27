package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 0, // Singleton record
    val ownerName: String = "",
    val monthlyIncome: Double = 0.0,
    val monthlySavingsGoal: Double = 0.0,
    val currentSavings: Double = 0.0,
    val isOnboardingCompleted: Boolean = false,
    val projectionRate: Double = 10.0,
    val projectionYears: Int = 10
)
