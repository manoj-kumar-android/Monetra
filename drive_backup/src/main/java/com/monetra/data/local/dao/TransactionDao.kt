package com.monetra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.monetra.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource

@Dao
interface TransactionDao {
    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%Y-%m', date) = :yearMonth 
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
        ORDER BY date DESC, id DESC
    """)
    fun getTransactionsByMonth(yearMonth: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
        ORDER BY date DESC
    """)
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE strftime('%Y-%m', date) = :yearMonth 
          AND type = 'INCOME'
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
    """)
    fun getTotalIncomeByMonth(yearMonth: String): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE strftime('%Y-%m', date) = :yearMonth 
          AND type = 'EXPENSE'
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
    """)
    fun getTotalExpenseByMonth(yearMonth: String): Flow<Double?>

    @Query("""
        SELECT category, SUM(amount) as total FROM transactions 
        WHERE strftime('%Y-%m', date) = :yearMonth 
          AND type = 'EXPENSE' 
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
        GROUP BY category
    """)
    fun getExpenseSumByCategory(yearMonth: String): Flow<List<CategorySum>>

    @Query("""
        SELECT category, SUM(amount) as total FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
          AND type = 'EXPENSE' 
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
        GROUP BY category
    """)
    fun getExpenseSumByCategoryBetweenDates(startDate: String, endDate: String): Flow<List<CategorySum>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
          AND type = 'INCOME'
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
    """)
    fun getTotalIncomeBetweenDates(startDate: String, endDate: String): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
          AND type = 'EXPENSE'
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
    """)
    fun getTotalExpenseBetweenDates(startDate: String, endDate: String): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = :type
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
    """)
    fun getLifetimeTotal(type: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE remoteId = :remoteId")
    suspend fun getTransactionByRemoteId(remoteId: String): TransactionEntity?

    @Query("UPDATE transactions SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSynced(remoteIds: List<String>)

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<TransactionEntity>)

    @Query("""
        SELECT * FROM transactions 
        WHERE (:query IS NULL OR 
               title LIKE '%' || :query || '%' OR 
               note LIKE '%' || :query || '%' OR 
               category LIKE '%' || :query || '%' OR 
               CAST(amount AS TEXT) LIKE '%' || :query || '%')
          AND (:transactionType IS NULL OR type = :transactionType)
          AND (:hasCategories = 0 OR category IN (:categories))
          AND (:startDate IS NULL OR date >= :startDate)
          AND (:endDate IS NULL OR date <= :endDate)
          AND (:minAmount IS NULL OR amount >= :minAmount)
          AND (:maxAmount IS NULL OR amount <= :maxAmount)
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
        ORDER BY date DESC, id DESC
    """)
    fun getTransactionsPaged(
        query: String?,
        transactionType: String?,
        categories: List<String>?,
        hasCategories: Boolean,
        startDate: String?,
        endDate: String?,
        minAmount: Double?,
        maxAmount: Double?
    ): PagingSource<Int, TransactionEntity>

    @Query("""
        SELECT 
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
        FROM transactions 
        WHERE (:query IS NULL OR 
               title LIKE '%' || :query || '%' OR 
               note LIKE '%' || :query || '%' OR 
               category LIKE '%' || :query || '%' OR 
               CAST(amount AS TEXT) LIKE '%' || :query || '%')
          AND (:transactionType IS NULL OR type = :transactionType)
          AND (:hasCategories = 0 OR category IN (:categories))
          AND (:startDate IS NULL OR date >= :startDate)
          AND (:endDate IS NULL OR date <= :endDate)
          AND (:minAmount IS NULL OR amount >= :minAmount)
          AND (:maxAmount IS NULL OR amount <= :maxAmount)
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
    """)
    fun getFilterSummary(
        query: String?,
        transactionType: String?,
        categories: List<String>?,
        hasCategories: Boolean,
        startDate: String?,
        endDate: String?,
        minAmount: Double?,
        maxAmount: Double?
    ): Flow<FilterSummary?>

    @Query("""
        SELECT DISTINCT category FROM transactions 
        WHERE (:type IS NULL OR type = :type)
          AND id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
        ORDER BY category ASC
    """)
    fun getUsedCategories(type: String?): Flow<List<String>>

    @Query("""
        SELECT 
            MIN(amount) as minAmount, 
            MAX(amount) as maxAmount 
        FROM transactions
        WHERE id NOT IN (SELECT entityId FROM pending_deletes WHERE entityType = 'TRANSACTION')
    """)
    fun getAmountRange(): Flow<FilterAmountRange?>

    suspend fun upsertSync(entity: TransactionEntity) {
        val existing = getTransactionByRemoteId(entity.remoteId)
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
            insertTransaction(entity.copy(id = id, isSynced = true))
        }
    }
}

data class CategorySum(
    val category: String,
    val total: Double
)

data class FilterSummary(
    val totalIncome: Double?,
    val totalExpense: Double?
)

data class FilterAmountRange(
    val minAmount: Double,
    val maxAmount: Double
)
