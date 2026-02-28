package com.monetra.data.repository

import com.monetra.data.local.dao.SavingDao
import com.monetra.data.local.entity.toSavingEntity
import com.monetra.domain.model.Saving
import com.monetra.domain.repository.SavingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingRepositoryImpl(
    private val savingDao: SavingDao
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
        savingDao.insertSaving(saving.toSavingEntity())
    }

    override suspend fun deleteSaving(saving: Saving) {
        savingDao.deleteSaving(saving.toSavingEntity())
    }
}
