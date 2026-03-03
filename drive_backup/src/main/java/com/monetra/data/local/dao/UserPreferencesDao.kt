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

    @Query("SELECT * FROM user_preferences")
    suspend fun getAllUserPreferences(): List<UserPreferencesEntity>

    @Query("SELECT * FROM user_preferences WHERE isSynced = 0")
    suspend fun getUnsyncedPreferences(): List<UserPreferencesEntity>

    @Query("UPDATE user_preferences SET isSynced = 1")
    suspend fun markAsSynced()

    suspend fun upsertSync(entity: UserPreferencesEntity) {
        val existing = getAllUserPreferences().firstOrNull()
        val shouldOverwrite = when {
            existing == null -> true
            entity.version > existing.version -> true
            entity.version < existing.version -> false
            entity.updatedAt > existing.updatedAt -> true
            entity.updatedAt < existing.updatedAt -> false
            else -> entity.deviceId > existing.deviceId
        }

        if (shouldOverwrite) {
            upsertUserPreferences(entity.copy(id = 0, isSynced = true))
        }
    }

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAllUserPreferences()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUserPreferences(prefs: List<UserPreferencesEntity>)
}
