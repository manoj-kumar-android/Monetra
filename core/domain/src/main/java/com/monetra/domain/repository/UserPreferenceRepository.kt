package com.monetra.domain.repository

import com.monetra.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferenceRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun saveUserPreferences(preferences: UserPreferences)
}
