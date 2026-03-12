package com.monetra.domain.repository

import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionFilters
import com.monetra.domain.model.TransactionSummary
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface TransactionRepository {
    fun getTransactions(month: YearMonth): Flow<List<Transaction>>
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    fun getTotalIncome(month: YearMonth): Flow<Double>
    fun getTotalExpense(month: YearMonth): Flow<Double>
    fun getTotalIncomeBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Double>
    fun getTotalExpenseBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Double>
    fun getExpenseSumByCategoryBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Map<String, Double>>
    fun getLifetimeIncome(): Flow<Double>
    fun getLifetimeExpense(): Flow<Double>
    fun getExpenseSumByCategory(month: YearMonth): Flow<Map<String, Double>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)

    fun getTransactionsPaged(filters: TransactionFilters): Flow<PagingData<Transaction>>
    fun getFilterSummary(filters: TransactionFilters): Flow<TransactionSummary>
    fun getUsedCategories(type: com.monetra.domain.model.TransactionType?): Flow<List<String>>
    fun getAmountRange(): Flow<Pair<Double, Double>>
}
