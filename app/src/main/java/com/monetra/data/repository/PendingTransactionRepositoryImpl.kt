package com.monetra.data.repository

import com.monetra.data.local.dao.PendingTransactionDao
import com.monetra.data.local.entity.toDomainModel
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.PendingTransaction
import com.monetra.domain.repository.PendingTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PendingTransactionRepositoryImpl @Inject constructor(
    private val dao: PendingTransactionDao
) : PendingTransactionRepository {

    override fun getAllPending(): Flow<List<PendingTransaction>> {
        return dao.getAllPending().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getPendingById(id: Long): PendingTransaction? {
        return dao.getPendingById(id)?.toDomainModel()
    }

    override suspend fun insertPending(pending: PendingTransaction) {
        dao.insertPending(pending.toEntity())
    }

    override suspend fun deletePending(id: Long) {
        dao.deletePendingById(id)
    }

    override suspend fun isDuplicate(refId: String?, rawText: String): Boolean {
        if (refId != null && dao.existsByReferenceId(refId)) return true

        // Also check if same text arrived within last 5 minutes (for apps that don't provide refId)
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        return dao.existsByRecentText(rawText, fiveMinutesAgo)
    }
}
