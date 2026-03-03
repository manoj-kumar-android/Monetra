package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.UserPreferences
import com.monetra.domain.repository.UserPreferenceRepository
import javax.inject.Inject

class UpdateUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferenceRepository
) {
    suspend operator fun invoke(
        ownerName: String,
        income: Double,
        savingsGoal: Double,
        onboardingCompleted: Boolean = true
    ) {
        repository.saveUserPreferences(
            UserPreferences(
                ownerName = ownerName,
                monthlyIncome = income,
                monthlySavingsGoal = savingsGoal,
                isOnboardingCompleted = onboardingCompleted
            )
        )
    }
}
