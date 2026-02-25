package com.monetra.domain.model

data class CategoryBudget(
    val categoryName: String,
    val limit: Double,
    val currentSpent: Double = 0.0
) {
    val progress: Float
        get() = if (limit > 0) (currentSpent / limit).toFloat() else 0f
    
    val isWarning: Boolean
        get() = progress >= 0.8f
        
    val isAlert: Boolean
        get() = progress >= 1.0f
}
