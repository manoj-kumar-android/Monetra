package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.BillInstanceEntity
import com.monetra.data.local.entity.MonthlyExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface MonthlyExpenseDao {
    @Query("""
        SELECT * FROM monthly_expenses 
        WHERE id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'MONTHLY_EXPENSE')
    """)
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
    // --- BillInstance (Monthly Data) ---
    @Query("""
        SELECT 
            bi.id, bi.remoteId, bi.billId, bi.month, bi.amount, 
            bi.version, bi.updatedAt, bi.deviceId, bi.isSynced,
            COALESCE(trans.totalPaid, 0.0) AS paidAmount,
            CASE 
                WHEN COALESCE(trans.totalPaid, 0.0) >= bi.amount THEN 'PAID'
                WHEN COALESCE(trans.totalPaid, 0.0) > 0 THEN 'PARTIAL'
                ELSE 'PENDING'
            END AS status
        FROM bill_instances bi
        JOIN monthly_expenses me ON me.id = bi.billId
        LEFT JOIN (
            SELECT t.category, strftime('%Y-%m', t.date) as tMonth, SUM(t.amount) as totalPaid
            FROM transactions t
            WHERE t.type = 'EXPENSE'
              AND t.id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
            GROUP BY t.category, tMonth
        ) trans ON trans.tMonth = bi.month AND trans.category = me.category
        WHERE bi.month = :month
          AND bi.billId NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'MONTHLY_EXPENSE')
    """)
    fun getInstancesForMonth(month: YearMonth): Flow<List<BillInstanceEntity>>

    @Query("""
        SELECT 
            bi.id, bi.remoteId, bi.billId, bi.month, bi.amount, 
            bi.version, bi.updatedAt, bi.deviceId, bi.isSynced,
            COALESCE(trans.totalPaid, 0.0) AS paidAmount,
            CASE 
                WHEN COALESCE(trans.totalPaid, 0.0) >= bi.amount THEN 'PAID'
                WHEN COALESCE(trans.totalPaid, 0.0) > 0 THEN 'PARTIAL'
                ELSE 'PENDING'
            END AS status
        FROM bill_instances bi
        JOIN monthly_expenses me ON me.id = bi.billId
        LEFT JOIN (
            SELECT t.category, strftime('%Y-%m', t.date) as tMonth, SUM(t.amount) as totalPaid
            FROM transactions t
            WHERE t.type = 'EXPENSE'
              AND t.id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
            GROUP BY t.category, tMonth
        ) trans ON trans.tMonth = bi.month AND trans.category = me.category
        WHERE bi.billId = :billId AND bi.month = :month
          AND bi.billId NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'MONTHLY_EXPENSE')
    """)
    suspend fun getInstanceByBillAndMonth(billId: Long, month: YearMonth): BillInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillInstance(instance: BillInstanceEntity)

    @Query("""
        SELECT 
            bi.id, bi.remoteId, bi.billId, bi.month, bi.amount, 
            bi.version, bi.updatedAt, bi.deviceId, bi.isSynced,
            COALESCE(trans.totalPaid, 0.0) AS paidAmount,
            CASE 
                WHEN COALESCE(trans.totalPaid, 0.0) >= bi.amount THEN 'PAID'
                WHEN COALESCE(trans.totalPaid, 0.0) > 0 THEN 'PARTIAL'
                ELSE 'PENDING'
            END AS status
        FROM bill_instances bi
        JOIN monthly_expenses me ON me.id = bi.billId
        LEFT JOIN (
            SELECT t.category, strftime('%Y-%m', t.date) as tMonth, SUM(t.amount) as totalPaid
            FROM transactions t
            WHERE t.type = 'EXPENSE'
              AND t.id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
            GROUP BY t.category, tMonth
        ) trans ON trans.tMonth = bi.month AND trans.category = me.category
        WHERE bi.id = :id
          AND bi.billId NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'MONTHLY_EXPENSE')
    """)
    suspend fun getInstanceById(id: Long?): BillInstanceEntity?

    @Query("""
        SELECT SUM(bi.amount - COALESCE(trans.totalPaid, 0.0))
        FROM bill_instances bi
        JOIN monthly_expenses me ON me.id = bi.billId
        LEFT JOIN (
            SELECT t.category, strftime('%Y-%m', t.date) as tMonth, SUM(t.amount) as totalPaid
            FROM transactions t
            WHERE t.type = 'EXPENSE'
              AND t.id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
            GROUP BY t.category, tMonth
        ) trans ON trans.tMonth = bi.month AND trans.category = me.category
        WHERE bi.month = :month 
          AND (bi.amount - COALESCE(trans.totalPaid, 0.0)) > 0
          AND bi.billId NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'MONTHLY_EXPENSE')
    """)
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

    @Query("""
        SELECT 
            bi.id, bi.remoteId, bi.billId, bi.month, bi.amount, 
            bi.version, bi.updatedAt, bi.deviceId, bi.isSynced,
            COALESCE(trans.totalPaid, 0.0) AS paidAmount,
            CASE 
                WHEN COALESCE(trans.totalPaid, 0.0) >= bi.amount THEN 'PAID'
                WHEN COALESCE(trans.totalPaid, 0.0) > 0 THEN 'PARTIAL'
                ELSE 'PENDING'
            END AS status
        FROM bill_instances bi
        JOIN monthly_expenses me ON me.id = bi.billId
        LEFT JOIN (
            SELECT t.category, strftime('%Y-%m', t.date) as tMonth, SUM(t.amount) as totalPaid
            FROM transactions t
            WHERE t.type = 'EXPENSE'
            GROUP BY t.category, tMonth
        ) trans ON trans.tMonth = bi.month AND trans.category = me.category
        WHERE bi.remoteId = :remoteId
    """)
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

    @Query("""
        SELECT 
            bi.id, bi.remoteId, bi.billId, bi.month, bi.amount, 
            bi.version, bi.updatedAt, bi.deviceId, bi.isSynced,
            COALESCE(trans.totalPaid, 0.0) AS paidAmount,
            CASE 
                WHEN COALESCE(trans.totalPaid, 0.0) >= bi.amount THEN 'PAID'
                WHEN COALESCE(trans.totalPaid, 0.0) > 0 THEN 'PARTIAL'
                ELSE 'PENDING'
            END AS status
        FROM bill_instances bi
        JOIN monthly_expenses me ON me.id = bi.billId
        LEFT JOIN (
            SELECT t.category, strftime('%Y-%m', t.date) as tMonth, SUM(t.amount) as totalPaid
            FROM transactions t
            WHERE t.type = 'EXPENSE'
            GROUP BY t.category, tMonth
        ) trans ON trans.tMonth = bi.month AND trans.category = me.category
    """)
    suspend fun getAllBillInstances(): List<BillInstanceEntity>


    @Query("DELETE FROM bill_instances")
    suspend fun deleteAllBillInstances()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBillInstances(instances: List<BillInstanceEntity>)

}
