package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.PendingTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTransactionDao {
    @Query("SELECT * FROM pending_transactions ORDER BY timestamp DESC")
    fun getAllPending(): Flow<List<PendingTransactionEntity>>

    @Query("SELECT * FROM pending_transactions WHERE id = :id")
    suspend fun getPendingById(id: Long): PendingTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(pending: PendingTransactionEntity)

    @Delete
    suspend fun deletePending(pending: PendingTransactionEntity)

    @Query("DELETE FROM pending_transactions WHERE id = :id")
    suspend fun deletePendingById(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM pending_transactions WHERE referenceId = :refId)")
    suspend fun existsByReferenceId(refId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM pending_transactions WHERE rawText = :text AND timestamp > :since)")
    suspend fun existsByRecentText(text: String, since: Long): Boolean
}
