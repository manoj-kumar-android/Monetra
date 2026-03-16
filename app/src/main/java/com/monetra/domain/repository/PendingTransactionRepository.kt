package com.monetra.domain.repository

import com.monetra.domain.model.PendingTransaction
import kotlinx.coroutines.flow.Flow

interface PendingTransactionRepository {
    fun getAllPending(): Flow<List<PendingTransaction>>
    suspend fun getPendingById(id: Long): PendingTransaction?
    suspend fun insertPending(pending: PendingTransaction)
    suspend fun deletePending(id: Long)
    suspend fun isDuplicate(refId: String?, rawText: String): Boolean
}
