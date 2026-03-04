package com.monetra.domain.model

data class UserPreferences(
    val ownerName: String = "",
    val monthlyIncome: Double = 0.0,
    val monthlySavingsGoal: Double = 0.0,
    val currentSavings: Double = 0.0,
    val isOnboardingCompleted: Boolean = false,
    val isBackupEnabled: Boolean = false,
    val projectionRate: Double = 10.0,
    val projectionYears: Int = 10,
    override val remoteId: String = "global_preferences",
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable
