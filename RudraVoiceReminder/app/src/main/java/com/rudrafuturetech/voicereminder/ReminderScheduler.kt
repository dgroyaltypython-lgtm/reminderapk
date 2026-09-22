package com.rudrafuturetech.voicereminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build

object ReminderScheduler {
    const val CHANNEL_ID = "reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Audible reminders from Rudra Voice Reminder"
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", reminder.id)
            putExtra("title", reminder.title)
            putExtra("voice", reminder.voiceEnabled)
        }
        val pending = PendingIntent.getBroadcast(
            context, reminder.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, reminder.triggerAt, pending
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.triggerAt, pending)
        }
    }

    fun cancel(context: Context, id: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
    }
}
