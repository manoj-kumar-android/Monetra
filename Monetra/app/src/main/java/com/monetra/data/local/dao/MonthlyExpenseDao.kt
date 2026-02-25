package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.MonthlyExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyExpenseDao {
    @Query("SELECT * FROM monthly_expenses")
    fun getAllMonthlyExpenses(): Flow<List<MonthlyExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyExpense(expense: MonthlyExpenseEntity)

    @Delete
    suspend fun deleteMonthlyExpense(expense: MonthlyExpenseEntity)

    @Query("SELECT SUM(amount) FROM monthly_expenses")
    fun getTotalMonthlyExpenseAmount(): Flow<Double?>
}
