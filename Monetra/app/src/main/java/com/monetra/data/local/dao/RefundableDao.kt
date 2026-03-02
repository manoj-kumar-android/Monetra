package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.RefundableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RefundableDao {
    @Query("SELECT * FROM refundable ORDER BY dueDate ASC")
    fun getAllRefundables(): Flow<List<RefundableEntity>>

    @Query("SELECT * FROM refundable WHERE id = :id")
    suspend fun getRefundableById(id: Long): RefundableEntity?

    @Query("SELECT * FROM refundable WHERE id = :id")
    fun getRefundableFlowById(id: Long): Flow<RefundableEntity?>

    @Upsert
    suspend fun upsertRefundable(refundable: RefundableEntity): Long

    @Delete
    suspend fun deleteRefundable(refundable: RefundableEntity)

    @Query("UPDATE refundable SET isPaid = :isPaid WHERE id = :id")
    suspend fun updatePaidStatus(id: Long, isPaid: Boolean)

    @Query("SELECT * FROM refundable WHERE isSynced = 0")
    suspend fun getUnsyncedRefundables(): List<RefundableEntity>

    @Query("SELECT * FROM refundable WHERE remoteId = :remoteId")
    suspend fun getRefundableByRemoteId(remoteId: String): RefundableEntity?

    @Query("UPDATE refundable SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSynced(remoteIds: List<String>)

    suspend fun upsertSync(entity: RefundableEntity) {
        val existing = getRefundableByRemoteId(entity.remoteId)
        if (existing == null) {
            upsertRefundable(entity.copy(id = 0, isSynced = true))
        } else if (entity.updatedAt > existing.updatedAt) {
            upsertRefundable(entity.copy(id = existing.id, isSynced = true))
        }
    }

    @Query("SELECT * FROM refundable")
    suspend fun getAllRefundablesList(): List<RefundableEntity>

    @Query("DELETE FROM refundable")
    suspend fun deleteAllRefundables()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRefundables(refundables: List<RefundableEntity>)
}
