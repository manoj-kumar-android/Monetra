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
    @Query("SELECT * FROM investments")
    suspend fun getAllInvestments(): List<InvestmentEntity>

    @Query("DELETE FROM investments")
    suspend fun deleteAllInvestments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInvestments(investments: List<InvestmentEntity>)
}
