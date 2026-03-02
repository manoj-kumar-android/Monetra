package com.monetra.data.repository

import com.monetra.data.local.dao.CategoryBudgetDao
import com.monetra.data.local.dao.TransactionDao
import com.monetra.data.local.entity.CategoryBudgetEntity
import com.monetra.domain.model.CategoryBudget
import com.monetra.domain.repository.BudgetRepository
import com.monetra.domain.repository.CloudBackupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: CategoryBudgetDao,
    private val transactionDao: TransactionDao,
    private val syncManager: com.monetra.data.sync.SyncManager,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : BudgetRepository {

    private suspend fun triggerSync() {
        syncRepository.setDirty(true)
        syncManager.runSync()
    }

    override fun getCategoryBudgets(month: YearMonth): Flow<List<CategoryBudget>> {
        val yearMonthStr = String.format("%04d-%02d", month.year, month.monthValue)
        
        return combine(
            budgetDao.getAllBudgets(),
            transactionDao.getExpenseSumByCategory(yearMonthStr)
        ) { budgets, sums ->
            val sumsMap = sums.associate { it.category to it.total }
            
            budgets.map { budgetEntity ->
                CategoryBudget(
                    remoteId = budgetEntity.remoteId,
                    categoryName = budgetEntity.categoryName,
                    limit = budgetEntity.limit,
                    currentSpent = sumsMap[budgetEntity.categoryName] ?: 0.0,
                    updatedAt = budgetEntity.updatedAt,
                    deviceId = budgetEntity.deviceId,
                    isSynced = budgetEntity.isSynced
                )
            }
        }
    }

    override suspend fun saveCategoryBudget(budget: CategoryBudget) {
        val deviceId = syncRepository.getDeviceId()
        budgetDao.upsertBudget(
            CategoryBudgetEntity(
                remoteId = budget.remoteId,
                categoryName = budget.categoryName,
                limit = budget.limit,
                updatedAt = System.currentTimeMillis(),
                deviceId = deviceId,
                isSynced = false
            )
        )
        triggerSync()
    }

    override suspend fun deleteCategoryBudget(categoryName: String) {
        budgetDao.deleteBudget(categoryName)
        triggerSync()
    }
}
