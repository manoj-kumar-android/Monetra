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
}
