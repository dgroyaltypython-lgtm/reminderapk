package com.rudrafuturetech.voicereminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.createChannel(context)
        val title = intent.getStringExtra("title") ?: "Reminder"
        val voice = intent.getBooleanExtra("voice", true)

        val openIntent = Intent(context, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            context, 9001, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Rudra Voice Reminder")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        context.getSystemService(android.app.NotificationManager::class.java)
            .notify(intent.getLongExtra("id", 1L).toInt(), notification)

        if (voice) {
            val tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts.language = Locale.getDefault()
                    tts.speak("Reminder. $title", TextToSpeech.QUEUE_FLUSH, null, "rudra-reminder")
                }
            }
        }
    }
}
