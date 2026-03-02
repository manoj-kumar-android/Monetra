package com.monetra.data.repository

import com.monetra.data.local.dao.UserPreferencesDao
import com.monetra.data.local.entity.UserPreferencesEntity
import com.monetra.domain.model.UserPreferences
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferenceRepositoryImpl @Inject constructor(
    private val dao: UserPreferencesDao,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : UserPreferenceRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return dao.getUserPreferences().map { entity ->
            UserPreferences(
                ownerName = entity?.ownerName ?: "",
                monthlyIncome = entity?.monthlyIncome ?: 0.0,
                monthlySavingsGoal = entity?.monthlySavingsGoal ?: 0.0,
                currentSavings = entity?.currentSavings ?: 0.0,
                isOnboardingCompleted = entity?.isOnboardingCompleted ?: false,
                projectionRate = entity?.projectionRate ?: 10.0,
                projectionYears = entity?.projectionYears ?: 10,
                remoteId = entity?.remoteId ?: "global_preferences",
                updatedAt = entity?.updatedAt ?: 0L,
                deviceId = entity?.deviceId ?: "",
                isSynced = entity?.isSynced ?: false
            )
        }
    }

    override suspend fun saveUserPreferences(preferences: UserPreferences) {
        val deviceId = syncRepository.getDeviceId()
        dao.upsertUserPreferences(
            UserPreferencesEntity(
                remoteId = preferences.remoteId,
                ownerName = preferences.ownerName,
                monthlyIncome = preferences.monthlyIncome,
                monthlySavingsGoal = preferences.monthlySavingsGoal,
                currentSavings = preferences.currentSavings,
                isOnboardingCompleted = preferences.isOnboardingCompleted,
                projectionRate = preferences.projectionRate,
                projectionYears = preferences.projectionYears,
                updatedAt = System.currentTimeMillis(),
                deviceId = deviceId,
                isSynced = false
            )
        )
        syncRepository.setDirty(true)
    }
}
