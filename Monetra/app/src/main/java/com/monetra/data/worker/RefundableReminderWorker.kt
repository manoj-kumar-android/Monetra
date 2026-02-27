package com.monetra.data.worker

import android.app.NotificationChannel
import android.content.ContentValues
import android.app.NotificationManager
import android.content.Context
import android.telephony.SmsManager
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

        // Only handle specifically scheduled reminders.
        // The daily-batch path was removed — it caused reminders to repeat every day.
        // Reminders are fire-and-forget: scheduled once, fired once, then flags cleared.
        if (specificId == -1L) return Result.success()

        val item = repository.getRefundableById(specificId)

        // Guard: skip if item is gone, already paid, or flags already cleared
        if (item == null || item.isPaid) return Result.success()
        if (!item.remindMe && !item.sendSmsReminder) return Result.success()

        var modified = false

        if (item.remindMe) {
            // Notification message depends on who owes whom
            val (title, body) = when (item.entryType) {
                RefundableType.LENT ->
                    "💰 Payment Due" to "${item.personName} owes you ₹${item.amount}. Time to collect!"
                RefundableType.BORROWED ->
                    "⚠️ Payment Reminder" to "You owe ₹${item.amount} to ${item.personName}. Don't forget to pay!"
            }
            showNotification(title, body, item.id)
            modified = true
        }

        if (item.sendSmsReminder) {
            // SMS message is sent to the OTHER person's number
            val smsBody = when (item.entryType) {
                RefundableType.LENT ->
                    // I lent money → SMS to borrower: remind them to pay me back
                    "Hi ${item.personName}, this is a friendly reminder that you have ₹${item.amount} due. Kindly settle it at your earliest. - Sent via Monetra"
                RefundableType.BORROWED ->
                    // I borrowed money → SMS to lender: inform them I'll pay
                    "Hi ${item.personName}, I wanted to inform you that I'm aware of the ₹${item.amount} I owe you and will be settling it soon. Thank you for your patience. - Sent via Monetra"
            }
            sendSms(item.phoneNumber, smsBody)
            modified = true
        }

        // Fire-and-forget: clear flags so this reminder is NEVER sent again
        if (modified) {
            repository.upsertRefundable(item.copy(remindMe = false, sendSmsReminder = false))
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String, id: Long) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "refundable_reminders"

        val channel = NotificationChannel(channelId, "Refundable Reminders", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val deepLinkIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            "monetra://refundable?id=$id".toUri(),
            applicationContext,
            com.monetra.MainActivity::class.java
        ).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext,
            id.toInt(),
            deepLinkIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id.toInt(), notification)
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                applicationContext.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val normalizedNumber = when {
                phoneNumber.startsWith("+") -> phoneNumber
                phoneNumber.startsWith("91") && phoneNumber.length == 12 -> "+$phoneNumber"
                phoneNumber.length == 10 -> "+91$phoneNumber"
                else -> phoneNumber
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(normalizedNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(normalizedNumber, null, message, null, null)
            }
            saveSmsToSentBox(normalizedNumber, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveSmsToSentBox(phoneNumber: String, message: String) {
        try {
            val values = ContentValues().apply {
                put("address", phoneNumber)
                put("body", message)
                put("date", System.currentTimeMillis())
                put("read", 1)
                put("type", 2)
            }
            applicationContext.contentResolver.insert("content://sms/sent".toUri(), values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
