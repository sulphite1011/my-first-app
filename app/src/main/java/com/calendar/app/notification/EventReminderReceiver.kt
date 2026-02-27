package com.calendar.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.calendar.app.MainActivity
import com.calendar.app.R

class EventReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1)
        val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Event"
        val eventDescription = intent.getStringExtra(EXTRA_EVENT_DESCRIPTION) ?: ""
        
        if (eventId != -1L) {
            showNotification(context, eventId, eventTitle, eventDescription)
        }
    }
    
    private fun showNotification(
        context: Context,
        eventId: Long,
        title: String,
        description: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open the app when notification is tapped
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_calendar)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title\n$description"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(eventId.toInt(), notification)
    }
    
    companion object {
        const val CHANNEL_ID = "event_reminders"
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_DESCRIPTION = "event_description"
    }
}
