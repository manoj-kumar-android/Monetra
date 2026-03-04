package com.monetra.domain.usecase.intelligence

import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferenceRepository
) {
    suspend operator fun invoke(
        ownerName: String,
        income: Double,
        savingsGoal: Double,
        isBackupEnabled: Boolean? = null,
        onboardingCompleted: Boolean = true
    ) {
        val current = repository.getUserPreferences().first()
        repository.saveUserPreferences(
            current.copy(
                ownerName = ownerName,
                monthlyIncome = income,
                monthlySavingsGoal = savingsGoal,
                isBackupEnabled = isBackupEnabled ?: current.isBackupEnabled,
                isOnboardingCompleted = onboardingCompleted
            )
        )
    }
}
