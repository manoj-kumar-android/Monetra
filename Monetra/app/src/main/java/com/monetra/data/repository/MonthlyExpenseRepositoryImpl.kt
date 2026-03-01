package com.monetra.data.repository

import com.monetra.data.local.dao.MonthlyExpenseDao
import com.monetra.data.local.entity.toDomain
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.BillInstance
import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.MonthlyExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class MonthlyExpenseRepositoryImpl @Inject constructor(
    private val dao: MonthlyExpenseDao,
    private val cloudBackupRepository: CloudBackupRepository
) : MonthlyExpenseRepository {
    
    // --- Rules ---
    override fun getAllMonthlyExpenses(): Flow<List<MonthlyExpense>> {
        return dao.getAllMonthlyExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMonthlyExpense(expense: MonthlyExpense): Long {
        val id = dao.insertMonthlyExpense(expense.toEntity())
        cloudBackupRepository.scheduleBackup()
        return id
    }

    override suspend fun deleteMonthlyExpense(expense: MonthlyExpense) {
        dao.deleteMonthlyExpense(expense.toEntity())
        cloudBackupRepository.scheduleBackup()
    }

    override suspend fun getMonthlyExpensesByCategory(category: String): List<MonthlyExpense> {
        return dao.getMonthlyExpensesByCategory(category).map { it.toDomain() }
    }

    override suspend fun getMonthlyExpenseById(id: Long): MonthlyExpense? {
        return dao.getMonthlyExpenseById(id)?.toDomain()
    }

    override fun getTotalMonthlyExpenseAmount(): Flow<Double> {
        return dao.getTotalMonthlyExpenseAmount().map { it ?: 0.0 }
    }

    // --- Instances ---

    override fun getInstancesForMonth(month: YearMonth): Flow<List<BillInstance>> {
        return dao.getInstancesForMonth(month).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getInstanceByBillAndMonth(billId: Long, month: YearMonth): BillInstance? {
        return dao.getInstanceByBillAndMonth(billId, month)?.toDomain()
    }

    override suspend fun insertBillInstance(instance: BillInstance) {
        dao.insertBillInstance(instance.toEntity())
        cloudBackupRepository.scheduleBackup()
    }

    override suspend fun getInstanceById(id: Long): BillInstance? {
        return dao.getInstanceById(id)?.toDomain()
    }

    override fun getTotalReservedAmountForMonth(month: YearMonth): Flow<Double> {
        return dao.getTotalReservedAmountForMonth(month).map { it ?: 0.0 }
    }

    override suspend fun hasInstanceForMonth(billId: Long, month: YearMonth): Boolean {
        return dao.hasInstanceForMonth(billId, month)
    }
}
