package com.monetra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.monetra.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE strftime('%Y-%m', date) = :yearMonth ORDER BY date DESC, id DESC")
    fun getTransactionsByMonth(yearMonth: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT SUM(amount) FROM transactions WHERE strftime('%Y-%m', date) = :yearMonth AND type = 'INCOME'")
    fun getTotalIncomeByMonth(yearMonth: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE strftime('%Y-%m', date) = :yearMonth AND type = 'EXPENSE'")
    fun getTotalExpenseByMonth(yearMonth: String): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE strftime('%Y-%m', date) = :yearMonth AND type = 'EXPENSE' GROUP BY category")
    fun getExpenseSumByCategory(yearMonth: String): Flow<List<CategorySum>>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'EXPENSE' GROUP BY category")
    fun getExpenseSumByCategoryBetweenDates(startDate: String, endDate: String): Flow<List<CategorySum>>

    @Query("SELECT SUM(amount) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'INCOME'")
    fun getTotalIncomeBetweenDates(startDate: String, endDate: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'EXPENSE'")
    fun getTotalExpenseBetweenDates(startDate: String, endDate: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
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

    suspend fun upsertSync(entity: TransactionEntity) {
        val existing = getTransactionByRemoteId(entity.remoteId)
        if (existing == null) {
            insertTransaction(entity.copy(id = 0, isSynced = true))
        } else if (entity.updatedAt > existing.updatedAt) {
            updateTransaction(entity.copy(id = existing.id, isSynced = true))
        }
    }
}

data class CategorySum(
    val category: String,
    val total: Double
)
