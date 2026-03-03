package com.monetra.data.repository

import com.monetra.data.local.dao.CategoryBudgetDao
import com.monetra.data.local.dao.TransactionDao
import com.monetra.data.local.entity.CategoryBudgetEntity
import com.monetra.domain.model.CategoryBudget
import com.monetra.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: CategoryBudgetDao,
    private val transactionDao: TransactionDao,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : BudgetRepository {

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
                    version = budgetEntity.version,
                    updatedAt = budgetEntity.updatedAt,
                    deviceId = budgetEntity.deviceId,
                    isSynced = budgetEntity.isSynced
                )
            }
        }
    }

    override suspend fun saveCategoryBudget(budget: CategoryBudget) {
        val deviceId = syncRepository.getDeviceId()
        val existing = budgetDao.getBudgetByName(budget.categoryName)
        val nextVersion = if (existing == null) 1L else existing.version + 1L
        
        budgetDao.upsertBudget(
            CategoryBudgetEntity(
                remoteId = budget.remoteId,
                categoryName = budget.categoryName,
                limit = budget.limit,
                version = nextVersion,
                updatedAt = System.currentTimeMillis(),
                deviceId = deviceId,
                isSynced = false
            )
        )
        syncRepository.clearTombstone(budget.remoteId)
        syncRepository.setDirty(true)
    }

    override suspend fun deleteCategoryBudget(categoryName: String) {
        budgetDao.getBudgetByName(categoryName)?.let { entity ->
            syncRepository.markDeleted(entity.remoteId, "CATEGORY_BUDGET")
            budgetDao.deleteBudget(categoryName)
        }
    }
}
