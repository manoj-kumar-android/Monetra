package com.monetra.data.repository

import com.monetra.data.local.dao.MonthlyExpenseDao
import com.monetra.data.local.entity.toDomain
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.BillInstance
import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.repository.MonthlyExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class MonthlyExpenseRepositoryImpl @Inject constructor(
    private val dao: MonthlyExpenseDao,
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
        
        val existing = if (expense.id != 0L) {
            dao.getMonthlyExpenseById(expense.id)
        } else {
            dao.getExpenseByRemoteId(expense.remoteId)
        }

        val syncExpense = expense.copy(
            id = existing?.id ?: expense.id,
            remoteId = existing?.remoteId ?: expense.remoteId,
            version = if (existing == null) 1L else existing.version + 1L,
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        val id = dao.insertMonthlyExpense(syncExpense.toEntity())
        syncRepository.clearTombstone(syncExpense.remoteId)
        syncRepository.setDirty(true)
        return id
    }

    override suspend fun deleteMonthlyExpense(expense: MonthlyExpense) {
        // Find and mark all associated child instances as deleted to prevent zombie orphans on Drive
        val instances = dao.getAllInstancesForBillList(expense.id)
        instances.forEach { instance ->
            syncRepository.markDeleted(instance.remoteId, "BILL_INSTANCE")
        }
        syncRepository.markDeleted(expense.remoteId, "MONTHLY_EXPENSE")
        dao.deleteMonthlyExpense(expense.toEntity())
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
        
        val existing = if (instance.id != 0L) {
            dao.getInstanceById(instance.id)
        } else {
            dao.getInstanceByRemoteId(instance.remoteId)
        }

        val syncInstance = instance.copy(
            id = existing?.id ?: instance.id,
            remoteId = existing?.remoteId ?: instance.remoteId,
            version = if (existing == null) 1L else existing.version + 1L,
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        dao.insertBillInstance(syncInstance.toEntity())
        syncRepository.clearTombstone(syncInstance.remoteId)
        syncRepository.setDirty(true)
    }

    override suspend fun getInstanceById(id: Long?): BillInstance? {
        return dao.getInstanceById(id)?.toDomain()
    }

    override fun getTotalReservedAmountForMonth(month: YearMonth): Flow<Double> {
        return dao.getTotalReservedAmountForMonth(month).map { it ?: 0.0 }
    }

    override suspend fun hasInstanceForMonth(billId: Long, month: YearMonth): Boolean {
        return dao.hasInstanceForMonth(billId, month)
    }
}
