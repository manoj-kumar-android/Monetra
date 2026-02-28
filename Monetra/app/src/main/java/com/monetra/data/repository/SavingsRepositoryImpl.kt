package com.monetra.data.repository

import com.monetra.data.local.dao.SavingsDao
import com.monetra.data.local.entity.toSavingsEntity
import com.monetra.domain.model.Savings
import com.monetra.domain.repository.SavingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingsRepositoryImpl(
    private val savingsDao: SavingsDao
) : SavingsRepository {

    override fun getAllSavings(): Flow<List<Savings>> {
        return savingsDao.getAllSavings().map { entities ->
            entities.map { it.toSavings() }
        }
    }

    override fun getTotalSavingsAmount(): Flow<Double> {
        return savingsDao.getTotalSavingsAmount().map { it ?: 0.0 }
    }

    override suspend fun getSavingsById(id: Long): Savings? {
        return savingsDao.getSavingsById(id)?.toSavings()
    }

    override suspend fun insertSavings(savings: Savings) {
        savingsDao.insertSavings(savings.toSavingsEntity())
    }

    override suspend fun deleteSavings(savings: Savings) {
        savingsDao.deleteSavings(savings.toSavingsEntity())
    }
}
