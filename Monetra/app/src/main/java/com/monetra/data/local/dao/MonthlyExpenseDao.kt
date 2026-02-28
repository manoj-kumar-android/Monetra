package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.BillInstanceEntity
import com.monetra.data.local.entity.MonthlyExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface MonthlyExpenseDao {
    @Query("SELECT * FROM monthly_expenses")
    fun getAllMonthlyExpenses(): Flow<List<MonthlyExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyExpense(expense: MonthlyExpenseEntity): Long

    @Delete
    suspend fun deleteMonthlyExpense(expense: MonthlyExpenseEntity)

    @Query("SELECT * FROM monthly_expenses WHERE category = :category")
    suspend fun getMonthlyExpensesByCategory(category: String): List<MonthlyExpenseEntity>

    @Query("SELECT * FROM monthly_expenses WHERE id = :id")
    suspend fun getMonthlyExpenseById(id: Long): MonthlyExpenseEntity?

    @Query("SELECT SUM(amount) FROM monthly_expenses")
    fun getTotalMonthlyExpenseAmount(): Flow<Double?>


    // --- BillInstance (Monthly Data) ---
    @Query("SELECT * FROM bill_instances WHERE month = :month")
    fun getInstancesForMonth(month: YearMonth): Flow<List<BillInstanceEntity>>

    @Query("SELECT * FROM bill_instances WHERE billId = :billId AND month = :month")
    suspend fun getInstanceByBillAndMonth(billId: Long, month: YearMonth): BillInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillInstance(instance: BillInstanceEntity)

    @Query("SELECT * FROM bill_instances WHERE id = :id")
    suspend fun getInstanceById(id: Long): BillInstanceEntity?

    @Query("SELECT SUM(amount - paidAmount) FROM bill_instances WHERE month = :month AND status != 'PAID'")
    fun getTotalReservedAmountForMonth(month: YearMonth): Flow<Double?>
    
    @Query("SELECT EXISTS(SELECT 1 FROM bill_instances WHERE billId = :billId AND month = :month)")
    suspend fun hasInstanceForMonth(billId: Long, month: YearMonth): Boolean
}
