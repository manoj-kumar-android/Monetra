package com.monetra.data.worker

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri

class NotificationActionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val notificationId = intent.getIntExtra("notification_id", -1)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (notificationId != -1) {
            notificationManager.cancel(notificationId)
        }
        
        if (intent.action == "ACTION_SEND_REMINDER") {
            val phoneNumber = intent.getStringExtra("phone_number")
            val smsBody = intent.getStringExtra("sms_body")
            
            if (phoneNumber != null && smsBody != null) {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "smsto:$phoneNumber".toUri()
                    putExtra("sms_body", smsBody)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(smsIntent)
            }
        }
        
        finish()
    }
}
