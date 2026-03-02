package com.monetra.data.repository

import com.monetra.data.local.dao.SavingDao
import com.monetra.data.local.entity.toSavingEntity
import com.monetra.domain.model.Saving
import com.monetra.domain.repository.SavingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.monetra.domain.repository.CloudBackupRepository
import javax.inject.Inject

class SavingRepositoryImpl @Inject constructor(
    private val savingDao: SavingDao,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : SavingRepository {

    override fun getAllSaving(): Flow<List<Saving>> {
        return savingDao.getAllSaving().map { entities ->
            entities.map { it.toSaving() }
        }
    }

    override fun getTotalSavingAmount(): Flow<Double> {
        return savingDao.getTotalSavingAmount().map { it ?: 0.0 }
    }

    override suspend fun getSavingById(id: Long): Saving? {
        return savingDao.getSavingById(id)?.toSaving()
    }

    override suspend fun insertSaving(saving: Saving) {
        val deviceId = syncRepository.getDeviceId()
        val syncSaving = saving.copy(
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        savingDao.insertSaving(syncSaving.toSavingEntity())
        syncRepository.setDirty(true)
    }

    override suspend fun deleteSaving(saving: Saving) {
        savingDao.deleteSaving(saving.toSavingEntity())
        syncRepository.setDirty(true)
    }
}
