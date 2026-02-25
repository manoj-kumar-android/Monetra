package com.monetra.domain.repository

import com.monetra.domain.model.Investment
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {
    fun getInvestments(): Flow<List<Investment>>
    fun getTotalInvestmentValue(): Flow<Double>
    suspend fun upsertInvestment(investment: Investment)
    suspend fun deleteInvestment(id: Long)
    suspend fun getInvestmentById(id: Long): Investment?
}
