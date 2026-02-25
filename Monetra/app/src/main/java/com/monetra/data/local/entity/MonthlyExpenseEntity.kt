package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.MonthlyExpense

@Entity(tableName = "monthly_expenses")
data class MonthlyExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val amount: Double,
    val category: String
)

fun MonthlyExpenseEntity.toDomain() = MonthlyExpense(id, name, amount, category)
fun MonthlyExpense.toEntity() = MonthlyExpenseEntity(id, name, amount, category)
