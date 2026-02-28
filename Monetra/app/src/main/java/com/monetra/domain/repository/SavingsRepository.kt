package com.monetra.domain.repository

import com.monetra.domain.model.Savings
import kotlinx.coroutines.flow.Flow

interface SavingsRepository {
    fun getAllSavings(): Flow<List<Savings>>
    fun getTotalSavingsAmount(): Flow<Double>
    suspend fun getSavingsById(id: Long): Savings?
    suspend fun insertSavings(savings: Savings)
    suspend fun deleteSavings(savings: Savings)
}
