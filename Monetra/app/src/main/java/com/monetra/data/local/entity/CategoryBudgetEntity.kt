package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey val categoryName: String,
    val limit: Double
)
