package com.monetra.data.repository

import com.monetra.data.local.dao.LoanDao
import com.monetra.data.local.entity.toDomainModel
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.Loan
import com.monetra.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor(
    private val dao: LoanDao,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : LoanRepository {
    override fun getAllLoans(): Flow<List<Loan>> {
        return dao.getAllLoans().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertLoan(loan: Loan) {
        val deviceId = syncRepository.getDeviceId()
        
        val existing = if (loan.id != 0L) {
            dao.getLoanById(loan.id)
        } else {
            dao.getLoanByRemoteId(loan.remoteId)
        }
        
        val syncLoan = loan.copy(
            id = existing?.id ?: loan.id,
            remoteId = existing?.remoteId ?: loan.remoteId,
            version = if (existing == null) 1L else existing.version + 1L,
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        dao.insertLoan(syncLoan.toEntity())
        syncRepository.clearTombstone(syncLoan.remoteId)
        syncRepository.setDirty(true)
    }

    override suspend fun updateLoan(loan: Loan) {
        insertLoan(loan)
    }

    override suspend fun deleteLoan(id: Long) {
        dao.getLoanById(id)?.let { entity ->
            syncRepository.markDeleted(entity.remoteId, "LOAN")
            dao.deleteLoan(id)
        }
    }

    override fun getTotalMonthlyEmi(): Flow<Double> {
        return dao.getTotalMonthlyEmi().map { it ?: 0.0 }
    }
}
