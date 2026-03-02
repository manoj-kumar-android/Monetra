package com.monetra.data.repository

import com.monetra.data.local.dao.SavingDao
import com.monetra.data.local.entity.toSavingEntity
import com.monetra.domain.model.Savings
import com.monetra.domain.repository.SavingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.monetra.domain.repository.CloudBackupRepository
import javax.inject.Inject

class SavingsRepositoryImpl @Inject constructor(
    private val savingsDao: SavingDao,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : SavingsRepository {

    override fun getAllSavings(): Flow<List<Savings>> {
        return savingsDao.getAllSaving().map { entities ->
            entities.map { s ->
                Savings(
                    id = s.id,
                    remoteId = s.remoteId,
                    bankName = s.bankName,
                    amount = s.amount,
                    interestRate = s.interestRate,
                    note = s.note,
                    updatedAt = s.updatedAt,
                    deviceId = s.deviceId,
                    isSynced = s.isSynced
                )
            }
        }
    }

    override fun getTotalSavingsAmount(): Flow<Double> {
        return savingsDao.getTotalSavingAmount().map { it ?: 0.0 }
    }

    override suspend fun getSavingsById(id: Long): Savings? {
        return savingsDao.getSavingById(id)?.let { s ->
            Savings(
                id = s.id,
                remoteId = s.remoteId,
                bankName = s.bankName,
                amount = s.amount,
                interestRate = s.interestRate,
                note = s.note,
                updatedAt = s.updatedAt,
                deviceId = s.deviceId,
                isSynced = s.isSynced
            )
        }
    }

    override suspend fun insertSavings(savings: Savings) {
        val deviceId = syncRepository.getDeviceId()
        val syncSavings = savings.copy(
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        savingsDao.insertSaving(syncSavings.toSavingEntity())
        syncRepository.setDirty(true)
    }

    override suspend fun deleteSavings(savings: Savings) {
        savingsDao.deleteSaving(savings.toSavingEntity())
        syncRepository.setDirty(true)
    }
}
