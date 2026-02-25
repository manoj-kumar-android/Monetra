package com.monetra.domain.repository

import com.monetra.domain.model.Loan
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    fun getAllLoans(): Flow<List<Loan>>
    suspend fun insertLoan(loan: Loan)
    suspend fun updateLoan(loan: Loan)
    suspend fun deleteLoan(id: Long)
    fun getTotalMonthlyEmi(): Flow<Double>
}
