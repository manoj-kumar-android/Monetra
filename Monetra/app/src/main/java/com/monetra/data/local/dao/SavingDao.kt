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
        if (existing == null) {
            insertSaving(entity.copy(id = 0, isSynced = true))
        } else if (entity.updatedAt > existing.updatedAt) {
            insertSaving(entity.copy(id = existing.id, isSynced = true))
        }
    }

    @Query("SELECT * FROM savings")
    suspend fun getAllSavingsList(): List<SavingEntity>

    @Query("DELETE FROM savings")
    suspend fun deleteAllSavings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavings(savings: List<SavingEntity>)
}
