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
    @Query("SELECT * FROM category_budgets")
    suspend fun getAllCategoryBudgets(): List<CategoryBudgetEntity>

    @Query("DELETE FROM category_budgets")
    suspend fun deleteAllCategoryBudgets()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategoryBudgets(budgets: List<CategoryBudgetEntity>)
}
