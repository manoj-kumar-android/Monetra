package com.monetra.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.monetra.domain.repository.PendingTransactionRepository
import com.monetra.util.NotificationParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MonetraNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var repository: PendingTransactionRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        val title = extras.getString("android.title", "")
        val text = extras.getString("android.text", "")
        val bigText = extras.getCharSequence("android.bigText", "").toString()

        val combinedText = "$title $text $bigText"

        scope.launch {
            val pending = NotificationParser.parse(combinedText, packageName)
            if (pending != null) {
                if (!repository.isDuplicate(pending.referenceId, pending.rawText)) {
                    repository.insertPending(pending)
                    Log.d("NotificationListener", "Detected transaction: $pending")
                } else {
                    Log.d("NotificationListener", "Duplicate transaction ignored")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // No need to cancel scope if it's tied to application lifecycle or handled correctly
    }
}
