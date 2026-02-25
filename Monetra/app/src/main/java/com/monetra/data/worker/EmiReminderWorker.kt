package com.monetra.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.monetra.domain.repository.LoanRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class EmiReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val loanRepository: LoanRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val loans = loanRepository.getAllLoans().first()
        
        // Find loans with due dates in the next 3 days
        val approachingLoans = loans.filter { 
            // Simplified logic: checking if today's day of month matches or is near
            // In a real app, LoanEntity should have a dueDateDayOfMonth field
            it.startDate.dayOfMonth == today.dayOfMonth
        }

        if (approachingLoans.isNotEmpty()) {
            approachingLoans.forEach { loan ->
                showNotification(loan.name, "Your EMI of ₹${loan.monthlyEmi} is due today.")
            }
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "emi_reminders"

        val channel = NotificationChannel(channelId, "EMI Reminders", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
