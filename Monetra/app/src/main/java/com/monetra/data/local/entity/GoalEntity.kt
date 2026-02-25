package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.FinancialGoal
import com.monetra.domain.model.GoalCategory
import java.time.LocalDate

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: LocalDate?,
    val category: GoalCategory
)

fun GoalEntity.toDomain(): FinancialGoal = FinancialGoal(
    id = id,
    title = title,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    deadline = deadline,
    category = category
)

fun FinancialGoal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    title = title,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    deadline = deadline,
    category = category
)
