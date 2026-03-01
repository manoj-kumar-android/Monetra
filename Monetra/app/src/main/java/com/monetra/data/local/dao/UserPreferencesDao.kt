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

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAllUserPreferences()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUserPreferences(prefs: List<UserPreferencesEntity>)
}
