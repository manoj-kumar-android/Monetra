package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals")
    suspend fun getAllGoals(): List<GoalEntity>

    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<GoalEntity>)
}
