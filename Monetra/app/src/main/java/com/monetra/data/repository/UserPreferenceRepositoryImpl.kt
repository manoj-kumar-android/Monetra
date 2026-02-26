package com.monetra.data.repository

import com.monetra.data.local.dao.UserPreferencesDao
import com.monetra.data.local.entity.UserPreferencesEntity
import com.monetra.domain.model.UserPreferences
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferenceRepositoryImpl @Inject constructor(
    private val dao: UserPreferencesDao
) : UserPreferenceRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return dao.getUserPreferences().map { entity ->
            UserPreferences(
                ownerName = entity?.ownerName ?: "",
                monthlyIncome = entity?.monthlyIncome ?: 0.0,
                monthlySavingsGoal = entity?.monthlySavingsGoal ?: 0.0,
                isOnboardingCompleted = entity?.isOnboardingCompleted ?: false,
                projectionRate = entity?.projectionRate ?: 10.0,
                projectionYears = entity?.projectionYears ?: 10
            )
        }
    }

    override suspend fun saveUserPreferences(preferences: UserPreferences) {
        dao.upsertUserPreferences(
            UserPreferencesEntity(
                ownerName = preferences.ownerName,
                monthlyIncome = preferences.monthlyIncome,
                monthlySavingsGoal = preferences.monthlySavingsGoal,
                isOnboardingCompleted = preferences.isOnboardingCompleted,
                projectionRate = preferences.projectionRate,
                projectionYears = preferences.projectionYears
            )
        )
    }
}
