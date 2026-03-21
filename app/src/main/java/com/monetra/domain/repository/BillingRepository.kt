package com.monetra.domain.repository

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val isPremium: Flow<Boolean>
    suspend fun startPurchase(activity: Activity): Result<Unit>
    suspend fun queryPurchase()
}
