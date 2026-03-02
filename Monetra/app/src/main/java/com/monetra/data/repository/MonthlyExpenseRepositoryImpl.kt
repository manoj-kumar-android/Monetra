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
    private val syncManager: com.monetra.data.sync.SyncManager,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : MonthlyExpenseRepository {
    
    // --- Rules ---
    override fun getAllMonthlyExpenses(): Flow<List<MonthlyExpense>> {
        return dao.getAllMonthlyExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMonthlyExpense(expense: MonthlyExpense): Long {
        val deviceId = syncRepository.getDeviceId()
        val syncExpense = expense.copy(
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        val id = dao.insertMonthlyExpense(syncExpense.toEntity())
        syncRepository.setDirty(true)
        syncManager.runSync()
        return id
    }

    override suspend fun deleteMonthlyExpense(expense: MonthlyExpense) {
        dao.deleteMonthlyExpense(expense.toEntity())
        syncRepository.setDirty(true)
        syncManager.runSync()
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
        val deviceId = syncRepository.getDeviceId()
        val syncInstance = instance.copy(
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        dao.insertBillInstance(syncInstance.toEntity())
        syncRepository.setDirty(true)
        syncManager.runSync()
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
