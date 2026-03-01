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
    private val cloudBackupRepository: CloudBackupRepository
) : SavingsRepository {

    override fun getAllSavings(): Flow<List<Savings>> {
        return savingsDao.getAllSaving().map { entities ->
            entities.map { it.toSaving().let { s -> Savings(s.id, s.bankName, s.amount, s.interestRate, s.note) } }
        }
    }

    override fun getTotalSavingsAmount(): Flow<Double> {
        return savingsDao.getTotalSavingAmount().map { it ?: 0.0 }
    }

    override suspend fun getSavingsById(id: Long): Savings? {
        return savingsDao.getSavingById(id)?.toSaving()?.let { s -> Savings(s.id, s.bankName, s.amount, s.interestRate, s.note) }
    }

    override suspend fun insertSavings(savings: Savings) {
        savingsDao.insertSaving(savings.toSavingEntity())
        cloudBackupRepository.scheduleBackup()
    }

    override suspend fun deleteSavings(savings: Savings) {
        savingsDao.deleteSaving(savings.toSavingEntity())
        cloudBackupRepository.scheduleBackup()
    }
}
