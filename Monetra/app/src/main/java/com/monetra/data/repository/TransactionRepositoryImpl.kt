package com.monetra.data.repository

import com.monetra.data.local.dao.TransactionDao
import com.monetra.data.local.entity.toDomainModel
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.Transaction
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
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
        dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: Long) {
        dao.deleteTransactionById(id)
    }
}
