package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.FinancialGoal
import com.monetra.domain.model.GoalCategory
import java.time.LocalDate

import kotlinx.serialization.Serializable
import com.monetra.data.local.util.LocalDateSerializer

@Serializable
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    @Serializable(with = LocalDateSerializer::class)
    val deadline: LocalDate?,
    val category: GoalCategory,
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity

fun GoalEntity.toDomain(): FinancialGoal = FinancialGoal(
    id = id,
    remoteId = remoteId,
    title = title,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    deadline = deadline,
    category = category,
    version = version,
    updatedAt = updatedAt,
    deviceId = deviceId,
    isSynced = isSynced
)

fun FinancialGoal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    remoteId = remoteId,
    title = title,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    deadline = deadline,
    category = category,
    version = version,
    updatedAt = updatedAt,
    deviceId = deviceId,
    isSynced = isSynced
)
