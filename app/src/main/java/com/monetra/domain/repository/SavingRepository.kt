package com.monetra.domain.repository

import com.monetra.domain.model.Saving
import kotlinx.coroutines.flow.Flow

interface SavingRepository {
    fun getAllSaving(): Flow<List<Saving>>
    fun getTotalSavingAmount(): Flow<Double>
    suspend fun getSavingById(id: Long): Saving?
    suspend fun insertSaving(saving: Saving)
    suspend fun deleteSaving(saving: Saving)
}
