package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments")
    fun getInvestments(): Flow<List<InvestmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvestment(investment: InvestmentEntity)

    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteInvestment(id: Long)

    @Query("SELECT * FROM investments WHERE id = :id")
    suspend fun getInvestmentById(id: Long): InvestmentEntity?
    @Query("SELECT * FROM investments WHERE isSynced = 0")
    suspend fun getUnsyncedInvestments(): List<InvestmentEntity>

    @Query("SELECT * FROM investments WHERE remoteId = :remoteId")
    suspend fun getInvestmentByRemoteId(remoteId: String): InvestmentEntity?

    @Query("UPDATE investments SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSynced(remoteIds: List<String>)

    suspend fun upsertSync(entity: InvestmentEntity) {
        val existing = getInvestmentByRemoteId(entity.remoteId)
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
            upsertInvestment(entity.copy(id = id, isSynced = true))
        }
    }

    @Query("SELECT * FROM investments")
    suspend fun getAllInvestments(): List<InvestmentEntity>

    @Query("DELETE FROM investments")
    suspend fun deleteAllInvestments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInvestments(investments: List<InvestmentEntity>)
}
