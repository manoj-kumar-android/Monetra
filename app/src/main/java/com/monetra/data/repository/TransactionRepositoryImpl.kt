package com.monetra.data.repository

import com.monetra.data.local.dao.TransactionDao
import com.monetra.data.local.entity.toDomainModel
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionFilters
import com.monetra.domain.model.TransactionSummary
import com.monetra.domain.repository.TransactionRepository
import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    @param:ApplicationContext private val context: Context,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : TransactionRepository {

    override fun getTransactions(month: YearMonth): Flow<List<Transaction>> {
        val yearMonthStr = String.format("%04d-%02d", month.year, month.monthValue)
        return dao.getTransactionsByMonth(yearMonthStr).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return dao.getTransactionById(id)?.toDomainModel()
    }

    override fun getTotalIncome(month: YearMonth): Flow<Double> {
        val yearMonthStr = String.format("%04d-%02d", month.year, month.monthValue)
        return dao.getTotalIncomeByMonth(yearMonthStr).map { it ?: 0.0 }
    }

    override fun getTotalExpense(month: YearMonth): Flow<Double> {
        val yearMonthStr = String.format("%04d-%02d", month.year, month.monthValue)
        return dao.getTotalExpenseByMonth(yearMonthStr).map { it ?: 0.0 }
    }

    override fun getTotalIncomeBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Double> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        return dao.getTotalIncomeBetweenDates(startDate.format(formatter), endDate.format(formatter)).map { it ?: 0.0 }
    }

    override fun getTotalExpenseBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Double> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        return dao.getTotalExpenseBetweenDates(startDate.format(formatter), endDate.format(formatter)).map { it ?: 0.0 }
    }

    override fun getExpenseSumByCategoryBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Map<String, Double>> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        return dao.getExpenseSumByCategoryBetweenDates(startDate.format(formatter), endDate.format(formatter)).map { list ->
            list.associate { it.category to it.total }
        }
    }

    override fun getLifetimeIncome(): Flow<Double> {
        return dao.getLifetimeTotal("INCOME").map { it ?: 0.0 }
    }

    override fun getLifetimeExpense(): Flow<Double> {
        return dao.getLifetimeTotal("EXPENSE").map { it ?: 0.0 }
    }

    override fun getExpenseSumByCategory(month: YearMonth): Flow<Map<String, Double>> {
        val yearMonthStr = String.format("%04d-%02d", month.year, month.monthValue)
        return dao.getExpenseSumByCategory(yearMonthStr).map { list ->
            list.associate { it.category to it.total }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        val deviceId = syncRepository.getDeviceId()
        
        // Resolve the actual identity of the record
        val existing = if (transaction.id != 0L) {
            dao.getTransactionById(transaction.id)
        } else {
            dao.getTransactionByRemoteId(transaction.remoteId)
        }
        
        val syncTransaction = transaction.copy(
            id = existing?.id ?: transaction.id, // Prefer database ID if found
            remoteId = existing?.remoteId ?: transaction.remoteId, // PRESERVE existing remoteId
            version = if (existing == null) 1L else existing.version + 1L,
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        dao.insertTransaction(syncTransaction.toEntity())
        syncRepository.clearTombstone(syncTransaction.remoteId)
        syncRepository.setDirty(true)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        // We use the same 'idempotent' logic here to ensure zero duplicates.
        insertTransaction(transaction)
    }

    override suspend fun deleteTransaction(id: Long) {
        dao.getTransactionById(id)?.let { entity ->
            syncRepository.markDeleted(entity.remoteId, "TRANSACTION")
            dao.deleteTransactionById(id)
        }
    }

    override fun getTransactionsPaged(filters: TransactionFilters): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                dao.getTransactionsPaged(
                    query = filters.query,
                    transactionType = filters.type?.name,
                    categories = filters.categories,
                    hasCategories = filters.categories != null && filters.categories!!.isNotEmpty(),
                    startDate = filters.startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate = filters.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    minAmount = filters.minAmount,
                    maxAmount = filters.maxAmount
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    override fun getFilterSummary(filters: TransactionFilters): Flow<TransactionSummary> {
        return dao.getFilterSummary(
            query = filters.query,
            transactionType = filters.type?.name,
            categories = filters.categories,
            hasCategories = filters.categories != null && filters.categories!!.isNotEmpty(),
            startDate = filters.startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate = filters.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            minAmount = filters.minAmount,
            maxAmount = filters.maxAmount
        ).map { summary ->
            val income = summary?.totalIncome ?: 0.0
            val expense = summary?.totalExpense ?: 0.0
            TransactionSummary(
                totalIncome = income,
                totalExpense = expense,
                netAmount = income - expense
            )
        }
    }

    override fun getUsedCategories(type: com.monetra.domain.model.TransactionType?): Flow<List<String>> {
        return dao.getUsedCategories(type?.name)
    }

    override fun getAmountRange(): Flow<Pair<Double, Double>> {
        return dao.getAmountRange().map {
            (it?.minAmount ?: 0.0) to (it?.maxAmount ?: 100000.0)
        }
    }
}
