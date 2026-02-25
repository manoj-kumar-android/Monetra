package com.monetra.domain.repository

import com.monetra.domain.model.FinancialGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(): Flow<List<FinancialGoal>>
    suspend fun upsertGoal(goal: FinancialGoal)
    suspend fun deleteGoal(id: Long)
    suspend fun getGoalById(id: Long): FinancialGoal?
}
