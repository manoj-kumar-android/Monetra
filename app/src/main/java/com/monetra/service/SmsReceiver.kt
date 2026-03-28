package com.monetra.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.monetra.util.TransactionDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Listens for incoming SMS messages and detects transactions from bank SMS.
 * Because we use the shared TransactionDetector, it avoids duplicate entries
 * if a notification for the same SMS is also received.
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var detector: TransactionDetector

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val sender = sms.displayOriginatingAddress ?: "SMS"
            val body = sms.displayMessageBody ?: continue

            scope.launch {
                detector.detectAndSave(body, "SMS: $sender")
            }
        }
    }
}
