package com.monetra.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.monetra.domain.model.RefundableType
import com.monetra.domain.repository.RefundableRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.core.net.toUri

@HiltWorker
class RefundableReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: RefundableRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val specificId = inputData.getLong("refundable_id", -1L)

        if (specificId == -1L) return Result.success()

        val item = repository.getRefundableById(specificId)

        if (item == null || item.isPaid) return Result.success()
        if (!item.remindMe) return Result.success()

        val (title, body) = when (item.entryType) {
            RefundableType.LENT ->
                "💰 Payment Due" to "${item.personName} owes you ₹${item.amount}. Time to collect!"
            RefundableType.BORROWED ->
                "⚠️ Payment Reminder" to "You owe ₹${item.amount} to ${item.personName}. Don't forget to pay!"
        }

        val smsBody = when (item.entryType) {
            RefundableType.LENT ->
                "Hi ${item.personName}, this is a friendly reminder that you have ₹${item.amount} due. Kindly settle it at your earliest. - Sent via Monetra"
            else -> null // No SMS button for BORROWED or others
        }

        showNotification(title, body, item.id, item.phoneNumber, smsBody, item.entryType)

        return Result.success()
    }

    private fun showNotification(
        title: String, 
        message: String, 
        id: Long, 
        phoneNumber: String, 
        smsBody: String?, 
        entryType: RefundableType
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "refundable_reminders"

        val channel = NotificationChannel(channelId, "Refundable Reminders", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "monetra://refundable?id=$id".toUri(),
            applicationContext,
            com.monetra.MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id.toInt(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // OK Action
        val okIntent = Intent(applicationContext, NotificationReceiver::class.java).apply {
            action = "ACTION_OK"
            putExtra("notification_id", id.toInt())
        }
        val okPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            id.toInt() + 1000,
            okIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Cannot be dismissed by swipe
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false) // Only dismissed by button action
            .addAction(0, "OK", okPendingIntent)

        // Send Reminder Action - Only for LENT
        if (entryType == RefundableType.LENT && smsBody != null) {
            val sendIntent = Intent(applicationContext, NotificationActionActivity::class.java).apply {
                action = "ACTION_SEND_REMINDER"
                putExtra("notification_id", id.toInt())
                putExtra("phone_number", phoneNumber)
                putExtra("sms_body", smsBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            val sendPendingIntent = PendingIntent.getActivity(
                applicationContext,
                id.toInt() + 2000,
                sendIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Send Reminder", sendPendingIntent)
        }

        notificationManager.notify(id.toInt(), builder.build())
    }
}
