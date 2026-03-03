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
    private val goalDao: GoalDao,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : GoalRepository {
    override fun getGoals(): Flow<List<FinancialGoal>> =
        goalDao.getGoals().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertGoal(goal: FinancialGoal) {
        val deviceId = syncRepository.getDeviceId()
        
        val existing = if (goal.id != 0L) {
            goalDao.getGoalById(goal.id)
        } else {
            goalDao.getGoalByRemoteId(goal.remoteId)
        }
        
        val syncGoal = goal.copy(
            id = existing?.id ?: goal.id,
            remoteId = existing?.remoteId ?: goal.remoteId,
            version = if (existing == null) 1L else existing.version + 1L,
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        goalDao.upsertGoal(syncGoal.toEntity())
        syncRepository.clearTombstone(syncGoal.remoteId)
        syncRepository.setDirty(true)
    }

    override suspend fun deleteGoal(id: Long) {
        goalDao.getGoalById(id)?.let { entity ->
            syncRepository.markDeleted(entity.remoteId, "GOAL")
            goalDao.deleteGoal(id)
        }
    }

    override suspend fun getGoalById(id: Long): FinancialGoal? =
        goalDao.getGoalById(id)?.toDomain()
}
