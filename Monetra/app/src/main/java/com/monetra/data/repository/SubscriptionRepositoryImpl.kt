package com.monetra.data.repository

import com.monetra.domain.model.SubscriptionPlan
import com.monetra.domain.model.UserSubscription
import com.monetra.domain.repository.SubscriptionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : SubscriptionRepository {

    // All users are treated as PREMIUM since the app is fully free
    override fun getSubscriptionStatus(): Flow<UserSubscription> {
        return userPreferenceRepository.getUserPreferences().map {
            UserSubscription(plan = SubscriptionPlan.PREMIUM)
        }
    }

    override suspend fun updateSubscription(isPremium: Boolean) {
        // No-op: subscription management removed; all features are free
    }
}
