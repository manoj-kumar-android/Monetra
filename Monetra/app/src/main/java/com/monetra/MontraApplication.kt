package com.monetra

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.monetra.intelligence.notification.InsightWorker
import com.monetra.data.worker.EmiReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.presentation.screen.lock.LockActivity
import android.content.Intent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class MontraApplication : Application(), Configuration.Provider, LifecycleEventObserver {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var userPreferenceRepo: com.monetra.domain.repository.UserPreferenceRepository

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        
        // Offload blocking WorkManager initialization to IO thread
        applicationScope.launch(Dispatchers.IO) {
            scheduleFinancialInsights()
            scheduleEmiReminders()
            scheduleRefundableReminders()
        }
        
        // Register for application globally lifecycle events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                // App coming to foreground
                applicationScope.launch {
                    val preferences = userPreferenceRepo.getUserPreferences().first()
                    if (preferences.isOnboardingCompleted) {
                        val intent = Intent(this@MontraApplication, LockActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                    }
                }
            }
            else -> {}
        }
    }

    private fun scheduleRefundableReminders() {
        val workRequest = PeriodicWorkRequestBuilder<com.monetra.data.worker.RefundableReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "refundable_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleFinancialInsights() {
        val workRequest = PeriodicWorkRequestBuilder<InsightWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "financial_insights",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleEmiReminders() {
        val workRequest = PeriodicWorkRequestBuilder<EmiReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "emi_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
