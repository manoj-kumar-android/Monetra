package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 0")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Upsert
    suspend fun upsertUserPreferences(preferences: UserPreferencesEntity)
}
