package com.monetra.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.monetra.util.TransactionDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MonetraNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var detector: TransactionDetector

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        val title = extras.getString("android.title", "")
        val text = extras.getString("android.text", "")
        val bigText = extras.getCharSequence("android.bigText", "").toString()

        val combinedText = "$title $text $bigText"

        scope.launch {
            detector.detectAndSave(combinedText, packageName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
