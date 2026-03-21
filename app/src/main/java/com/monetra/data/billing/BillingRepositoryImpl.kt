package com.monetra.data.billing

import android.app.Activity
import com.monetra.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager
) : BillingRepository {
    override val isPremium: Flow<Boolean> = billingManager.isPremiumUnlocked

    override suspend fun startPurchase(activity: Activity): Result<Unit> {
        return billingManager.startPurchase(activity)
    }

    override suspend fun queryPurchase() {
        billingManager.queryPurchases()
    }
}
