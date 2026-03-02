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

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getLoanById(id: Long): LoanEntity?

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
        val shouldOverwrite = when {
            existing == null -> true
            entity.version > existing.version -> true
            entity.version < existing.version -> false
            entity.updatedAt > existing.updatedAt -> true
            entity.updatedAt < existing.updatedAt -> false
            else -> entity.deviceId > existing.deviceId
        }

        if (shouldOverwrite) {
            val id = existing?.id ?: 0L
            insertLoan(entity.copy(id = id, isSynced = true))
        }
    }

    @Query("SELECT * FROM loans")
    suspend fun getAllLoansForBackUp(): List<LoanEntity>

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoans(loans: List<LoanEntity>)
}
