package com.monetra.domain.repository

import com.monetra.domain.model.UserSubscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getSubscriptionStatus(): Flow<UserSubscription>
    suspend fun updateSubscription(isPremium: Boolean)
}
