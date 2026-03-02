package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteLoan(id: Long)

    @Query("SELECT SUM(monthlyEmi) FROM loans")
    fun getTotalMonthlyEmi(): Flow<Double?>
    @Query("SELECT * FROM loans WHERE isSynced = 0")
    suspend fun getUnsyncedLoans(): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE remoteId = :remoteId")
    suspend fun getLoanByRemoteId(remoteId: String): LoanEntity?

    @Query("UPDATE loans SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSynced(remoteIds: List<String>)

    suspend fun upsertSync(entity: LoanEntity) {
        val existing = getLoanByRemoteId(entity.remoteId)
        if (existing == null) {
            insertLoan(entity.copy(id = 0, isSynced = true))
        } else if (entity.updatedAt > existing.updatedAt) {
            updateLoan(entity.copy(id = existing.id, isSynced = true))
        }
    }

    @Query("SELECT * FROM loans")
    suspend fun getAllLoansForBackUp(): List<LoanEntity>

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoans(loans: List<LoanEntity>)
}
