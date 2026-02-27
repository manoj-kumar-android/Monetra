package com.monetra.domain.repository

import com.monetra.domain.model.Refundable
import kotlinx.coroutines.flow.Flow

interface RefundableRepository {
    fun getAllRefundables(): Flow<List<Refundable>>
    fun observeRefundableById(id: Long): Flow<Refundable?>
    suspend fun getRefundableById(id: Long): Refundable?
    suspend fun upsertRefundable(refundable: Refundable): Long
    suspend fun deleteRefundable(refundable: Refundable)
    suspend fun updatePaidStatus(id: Long, isPaid: Boolean)
}
