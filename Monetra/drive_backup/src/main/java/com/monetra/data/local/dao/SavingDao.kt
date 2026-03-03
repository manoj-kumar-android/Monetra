package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.SavingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingDao {
    @Query("SELECT * FROM savings")
    fun getAllSaving(): Flow<List<SavingEntity>>

    @Query("SELECT SUM(amount) FROM savings")
    fun getTotalSavingAmount(): Flow<Double?>

    @Query("SELECT * FROM savings WHERE id = :id")
    suspend fun getSavingById(id: Long): SavingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaving(saving: SavingEntity)

    @Delete
    suspend fun deleteSaving(saving: SavingEntity)

    @Query("SELECT * FROM savings WHERE isSynced = 0")
    suspend fun getUnsyncedSavings(): List<SavingEntity>

    @Query("SELECT * FROM savings WHERE remoteId = :remoteId")
    suspend fun getSavingByRemoteId(remoteId: String): SavingEntity?

    @Query("UPDATE savings SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSynced(remoteIds: List<String>)

    suspend fun upsertSync(entity: SavingEntity) {
        val existing = getSavingByRemoteId(entity.remoteId)
        val shouldOverwrite = when {
            existing == null -> true
            entity.version > existing.version -> true
            entity.version < existing.version -> false
            entity.updatedAt > existing.updatedAt -> true
            entity.updatedAt < existing.updatedAt -> false
            else -> entity.deviceId > existing.deviceId
        }

        if (shouldOverwrite) {
            val id = existing?.id ?: 0L
            insertSaving(entity.copy(id = id, isSynced = true))
        }
    }

    @Query("SELECT * FROM savings")
    suspend fun getAllSavingsList(): List<SavingEntity>

    @Query("DELETE FROM savings")
    suspend fun deleteAllSavings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavings(savings: List<SavingEntity>)
}
