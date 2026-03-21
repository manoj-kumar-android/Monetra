package com.monetra.data.repository

import com.monetra.domain.model.SubscriptionPlan
import com.monetra.domain.model.UserSubscription
import com.monetra.domain.repository.SubscriptionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.drivebackup.api.DriveBackupManager
import com.monetra.drivebackup.api.VipConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val driveBackupManager: DriveBackupManager
) : SubscriptionRepository {

    override fun getSubscriptionStatus(): Flow<UserSubscription> {
        return combine(
            userPreferenceRepository.getUserPreferences(),
            driveBackupManager.accountName
        ) { prefs, email ->
            val isVip = VipConfig.isVip(email)
            val isPremium = prefs.isPremiumUnlocked || isVip

            UserSubscription(
                plan = if (isPremium) SubscriptionPlan.PREMIUM_ONETIME else SubscriptionPlan.FREE,
                purchaseTimestamp = if (isPremium) System.currentTimeMillis() else null
            )
        }
    }

    override suspend fun updateSubscription(isPremium: Boolean) {
        val currentPrefs = userPreferenceRepository.getUserPreferences().first()
        userPreferenceRepository.saveUserPreferences(
            currentPrefs.copy(isPremiumUnlocked = isPremium)
        )
    }
}
