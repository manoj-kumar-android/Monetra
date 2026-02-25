package com.monetra.data.repository

import com.monetra.data.local.dao.GoalDao
import com.monetra.data.local.entity.toDomain
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.FinancialGoal
import com.monetra.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {
    override fun getGoals(): Flow<List<FinancialGoal>> =
        goalDao.getGoals().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertGoal(goal: FinancialGoal) {
        goalDao.upsertGoal(goal.toEntity())
    }

    override suspend fun deleteGoal(id: Long) {
        goalDao.deleteGoal(id)
    }

    override suspend fun getGoalById(id: Long): FinancialGoal? =
        goalDao.getGoalById(id)?.toDomain()
}
