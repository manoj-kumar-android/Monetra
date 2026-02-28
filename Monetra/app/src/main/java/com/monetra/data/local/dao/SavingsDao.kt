package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.SavingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings")
    fun getAllSavings(): Flow<List<SavingsEntity>>

    @Query("SELECT SUM(amount) FROM savings")
    fun getTotalSavingsAmount(): Flow<Double?>

    @Query("SELECT * FROM savings WHERE id = :id")
    suspend fun getSavingsById(id: Long): SavingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavings(savings: SavingsEntity)

    @Delete
    suspend fun deleteSavings(savings: SavingsEntity)
}
