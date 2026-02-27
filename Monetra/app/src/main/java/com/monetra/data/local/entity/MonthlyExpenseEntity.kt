package com.monetra.data.local.entity

import androidx.room.*
import com.monetra.domain.model.BillInstance
import com.monetra.domain.model.BillStatus
import com.monetra.domain.model.MonthlyExpense
import java.time.YearMonth

/**
 * Entity for the recurring bill rule.
 */
@Entity(tableName = "monthly_expenses")
data class MonthlyExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val amount: Double,
    val category: String,
    val dueDay: Int
)

fun MonthlyExpenseEntity.toDomain() = MonthlyExpense(
    id = id,
    name = name,
    amount = amount,
    category = category,
    dueDay = dueDay
)

fun MonthlyExpense.toEntity() = MonthlyExpenseEntity(
    id = id,
    name = name,
    amount = amount,
    category = category,
    dueDay = dueDay
)

/**
 * Entity for the specific monthly instance of a recurring bill.
 */
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
    val billId: Long,
    val month: YearMonth,
    val amount: Double,
    val paidAmount: Double,
    val status: BillStatus
)

fun BillInstanceEntity.toDomain() = BillInstance(
    id = id,
    billId = billId,
    month = month,
    amount = amount,
    paidAmount = paidAmount,
    status = status
)

fun BillInstance.toEntity() = BillInstanceEntity(
    id = id,
    billId = billId,
    month = month,
    amount = amount,
    paidAmount = paidAmount,
    status = status
)
