package com.monetra.data.repository

import com.monetra.data.local.dao.LoanDao
import com.monetra.data.local.entity.toDomainModel
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.Loan
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor(
    private val dao: LoanDao,
    private val cloudBackupRepository: CloudBackupRepository
) : LoanRepository {
    override fun getAllLoans(): Flow<List<Loan>> {
        return dao.getAllLoans().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertLoan(loan: Loan) {
        dao.insertLoan(loan.toEntity())
        cloudBackupRepository.scheduleBackup()
    }

    override suspend fun updateLoan(loan: Loan) {
        dao.updateLoan(loan.toEntity())
        cloudBackupRepository.scheduleBackup()
    }

    override suspend fun deleteLoan(id: Long) {
        dao.deleteLoan(id)
        cloudBackupRepository.scheduleBackup()
    }

    override fun getTotalMonthlyEmi(): Flow<Double> {
        return dao.getTotalMonthlyEmi().map { it ?: 0.0 }
    }
}
