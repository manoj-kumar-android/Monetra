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
    private val investmentDao: InvestmentDao
) : InvestmentRepository {
    override fun getInvestments(): Flow<List<Investment>> =
        investmentDao.getInvestments().map { entities -> entities.map { it.toDomain() } }

    override fun getTotalInvestmentValue(): Flow<Double> =
        getInvestments().map { list -> list.sumOf { it.currentValuation } }

    override suspend fun upsertInvestment(investment: Investment) {
        investmentDao.upsertInvestment(investment.toEntity())
    }

    override suspend fun deleteInvestment(id: Long) {
        investmentDao.deleteInvestment(id)
    }

    override suspend fun getInvestmentById(id: Long): Investment? =
        investmentDao.getInvestmentById(id)?.toDomain()
}
