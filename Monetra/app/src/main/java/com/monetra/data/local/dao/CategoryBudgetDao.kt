package com.monetra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.monetra.data.local.entity.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets")
    fun getAllBudgets(): Flow<List<CategoryBudgetEntity>>

    @Upsert
    suspend fun upsertBudget(budget: CategoryBudgetEntity)

    @Query("DELETE FROM category_budgets WHERE categoryName = :categoryName")
    suspend fun deleteBudget(categoryName: String)
    @Query("SELECT * FROM category_budgets WHERE isSynced = 0")
    suspend fun getUnsyncedBudgets(): List<CategoryBudgetEntity>

    @Query("SELECT * FROM category_budgets WHERE remoteId = :remoteId")
    suspend fun getBudgetByRemoteId(remoteId: String): CategoryBudgetEntity?

    @Query("UPDATE category_budgets SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSynced(remoteIds: List<String>)

    suspend fun upsertSync(entity: CategoryBudgetEntity) {
        val existing = getBudgetByRemoteId(entity.remoteId)
        if (existing == null) {
            upsertBudget(entity.copy(isSynced = true))
        } else if (entity.updatedAt > existing.updatedAt) {
            upsertBudget(entity.copy(isSynced = true))
        }
    }

    @Query("SELECT * FROM category_budgets")
    suspend fun getAllCategoryBudgets(): List<CategoryBudgetEntity>

    @Query("DELETE FROM category_budgets")
    suspend fun deleteAllCategoryBudgets()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategoryBudgets(budgets: List<CategoryBudgetEntity>)
}
