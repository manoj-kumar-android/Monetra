package com.monetra.domain.model

data class CategoryBudget(
    override  val remoteId: String = java.util.UUID.randomUUID().toString(),
    val categoryName: String,
    val limit: Double,
    val currentSpent: Double = 0.0,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable {
    val progress: Float
        get() = if (limit > 0) (currentSpent / limit).toFloat() else 0f
    
    val isWarning: Boolean
        get() = progress >= 0.8f
        
    val isAlert: Boolean
        get() = progress >= 1.0f
}
