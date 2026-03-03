package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.PendingDeleteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingDeleteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingDeleteEntity)

    @Query("DELETE FROM pending_deletes WHERE entityId = :entityId AND entityType = :entityType")
    suspend fun cancelDelete(entityId: Long, entityType: String)

    @Query("SELECT * FROM pending_deletes WHERE createdAt < :cutoff")
    suspend fun getStaleDeletes(cutoff: Long): List<PendingDeleteEntity>

    @Query("DELETE FROM pending_deletes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM pending_deletes")
    fun observeAll(): Flow<List<PendingDeleteEntity>>

    @Query("SELECT entityId FROM pending_deletes WHERE entityType = :entityType")
    fun getPendingIdsForType(entityType: String): Flow<List<Long>>

    @Query("SELECT * FROM pending_deletes WHERE entityId = :entityId AND entityType = :entityType")
    suspend fun getPendingEntry(entityId: Long, entityType: String): PendingDeleteEntity?
}
