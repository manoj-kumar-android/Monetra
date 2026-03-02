package com.monetra.data.local.entity

import androidx.room.*
import com.monetra.data.local.util.YearMonthSerializer
import com.monetra.domain.model.BillInstance
import com.monetra.domain.model.BillStatus
import com.monetra.domain.model.MonthlyExpense
import java.time.YearMonth

import kotlinx.serialization.Serializable

/**
 * Entity for the recurring bill rule.
 */
@Serializable
@Entity(tableName = "monthly_expenses")
data class MonthlyExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val category: String,
    val dueDay: Int,
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity

fun MonthlyExpenseEntity.toDomain() = MonthlyExpense(
    id = id,
    remoteId = remoteId,
    name = name,
    amount = amount,
    category = category,
    dueDay = dueDay,
    version = version,
    updatedAt = updatedAt,
    deviceId = deviceId,
    isSynced = isSynced
)

fun MonthlyExpense.toEntity() = MonthlyExpenseEntity(
    id = id,
    remoteId = remoteId,
    name = name,
    amount = amount,
    category = category,
    dueDay = dueDay,
    version = version,
    updatedAt = updatedAt,
    deviceId = deviceId,
    isSynced = isSynced
)

/**
 * Entity for the specific monthly instance of a recurring bill.
 */
@Serializable
@Entity(
    tableName = "bill_instances",
    indices = [
        Index(value = ["billId", "month"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = MonthlyExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BillInstanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val billId: Long,
    @Serializable(with = YearMonthSerializer::class)
    val month: YearMonth,
    val amount: Double,
    val paidAmount: Double,
    val status: BillStatus,
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity

fun BillInstanceEntity.toDomain() = BillInstance(
    id = id,
    remoteId = remoteId,
    billId = billId,
    month = month,
    amount = amount,
    paidAmount = paidAmount,
    status = status,
    version = version,
    updatedAt = updatedAt,
    deviceId = deviceId,
    isSynced = isSynced
)

fun BillInstance.toEntity() = BillInstanceEntity(
    id = id,
    remoteId = remoteId,
    billId = billId,
    month = month,
    amount = amount,
    paidAmount = paidAmount,
    status = status,
    version = version,
    updatedAt = updatedAt,
    deviceId = deviceId,
    isSynced = isSynced
)
