package com.monetra.intelligence.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.monetra.domain.usecase.intelligence.GetSmartInsightsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class InsightWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getSmartInsights: GetSmartInsightsUseCase,
    private val notificationManager: MonetraNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val insights = getSmartInsights().first()
            
            insights.forEachIndexed { index, insight ->
                notificationManager.showInsightNotification(
                    id = insight.type.ordinal, // Group by type so we don't spam duplicate types
                    title = insight.title,
                    message = insight.message
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
