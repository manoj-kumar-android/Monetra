package com.monetra.data.repository

import com.monetra.data.local.dao.InvestmentDao
import com.monetra.data.local.entity.toDomain
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.Investment
import com.monetra.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InvestmentRepositoryImpl @Inject constructor(
    private val investmentDao: InvestmentDao,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : InvestmentRepository {
    override fun getInvestments(): Flow<List<Investment>> =
        investmentDao.getInvestments().map { entities -> entities.map { it.toDomain() } }

    override fun getTotalInvestmentValue(): Flow<Double> =
        getInvestments().map { list -> list.sumOf { it.calculateCurrentValue() } }

    override suspend fun upsertInvestment(investment: Investment) {
        val deviceId = syncRepository.getDeviceId()
        
        val existing = if (investment.id != 0L) {
            investmentDao.getInvestmentById(investment.id)
        } else {
            investmentDao.getInvestmentByRemoteId(investment.remoteId)
        }
        
        val syncInvestment = investment.copy(
            id = existing?.id ?: investment.id,
            remoteId = existing?.remoteId ?: investment.remoteId,
            version = if (existing == null) 1L else existing.version + 1L,
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        investmentDao.upsertInvestment(syncInvestment.toEntity())
        syncRepository.clearTombstone(syncInvestment.remoteId)
        syncRepository.setDirty(true)
    }

    override suspend fun deleteInvestment(id: Long) {
        investmentDao.getInvestmentById(id)?.let { entity ->
            syncRepository.markDeleted(entity.remoteId, "INVESTMENT")
            investmentDao.deleteInvestment(id)
        }
    }

    override suspend fun getInvestmentById(id: Long): Investment? =
        investmentDao.getInvestmentById(id)?.toDomain()
}
