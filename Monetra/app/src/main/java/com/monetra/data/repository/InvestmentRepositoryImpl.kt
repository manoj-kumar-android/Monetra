package com.monetra.data.repository

import com.monetra.data.local.dao.InvestmentDao
import com.monetra.data.local.entity.toDomain
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.Investment
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InvestmentRepositoryImpl @Inject constructor(
    private val investmentDao: InvestmentDao,
    private val syncManager: com.monetra.data.sync.SyncManager,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : InvestmentRepository {
    override fun getInvestments(): Flow<List<Investment>> =
        investmentDao.getInvestments().map { entities -> entities.map { it.toDomain() } }

    override fun getTotalInvestmentValue(): Flow<Double> =
        getInvestments().map { list -> list.sumOf { it.calculateCurrentValue() } }

    override suspend fun upsertInvestment(investment: Investment) {
        val deviceId = syncRepository.getDeviceId()
        val syncInvestment = investment.copy(
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        investmentDao.upsertInvestment(syncInvestment.toEntity())
        syncRepository.setDirty(true)
        syncManager.runSync()
    }

    override suspend fun deleteInvestment(id: Long) {
        investmentDao.deleteInvestment(id)
        syncRepository.setDirty(true)
        syncManager.runSync()
    }

    override suspend fun getInvestmentById(id: Long): Investment? =
        investmentDao.getInvestmentById(id)?.toDomain()
}
