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
    @Query("SELECT * FROM bill_instances WHERE billId = :billId")
    suspend fun getAllInstancesForBillList(billId: Long): List<BillInstanceEntity>

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

    @Query("DELETE FROM bill_instances WHERE id = :id")
    suspend fun deleteBillInstanceById(id: Long)

    @Query("SELECT * FROM monthly_expenses WHERE isSynced = 0")
    suspend fun getUnsyncedExpenses(): List<MonthlyExpenseEntity>

    @Query("SELECT * FROM monthly_expenses WHERE remoteId = :remoteId")
    suspend fun getExpenseByRemoteId(remoteId: String): MonthlyExpenseEntity?

    @Query("UPDATE monthly_expenses SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSyncedExpenses(remoteIds: List<String>)

    suspend fun upsertSyncExpense(entity: MonthlyExpenseEntity) {
        val existing = getExpenseByRemoteId(entity.remoteId)
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
            insertMonthlyExpense(entity.copy(id = id, isSynced = true))
        }
    }

    @Query("SELECT * FROM bill_instances WHERE isSynced = 0")
    suspend fun getUnsyncedInstances(): List<BillInstanceEntity>

    @Query("SELECT * FROM bill_instances WHERE remoteId = :remoteId")
    suspend fun getInstanceByRemoteId(remoteId: String): BillInstanceEntity?

    @Query("UPDATE bill_instances SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSyncedInstances(remoteIds: List<String>)

    suspend fun upsertSyncInstance(entity: BillInstanceEntity) {
        val existing = getInstanceByRemoteId(entity.remoteId)
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
            insertBillInstance(entity.copy(id = id, isSynced = true))
        }
    }

    @Query("SELECT * FROM monthly_expenses")
    suspend fun getAllMonthlyExpensesList(): List<MonthlyExpenseEntity>

    @Query("DELETE FROM monthly_expenses")
    suspend fun deleteAllMonthlyExpenses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMonthlyExpenses(expenses: List<MonthlyExpenseEntity>)

    @Query("SELECT * FROM bill_instances")
    suspend fun getAllBillInstances(): List<BillInstanceEntity>

    @Query("DELETE FROM bill_instances")
    suspend fun deleteAllBillInstances()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBillInstances(instances: List<BillInstanceEntity>)
}
