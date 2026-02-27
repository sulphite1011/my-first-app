package com.calendar.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.calendar.app.data.Event
import java.time.LocalDateTime
import java.time.ZoneId

class NotificationHelper(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleEventReminder(event: Event) {
        if (event.time == null) return
        
        val reminderDateTime = LocalDateTime.of(event.date, event.time)
            .minusMinutes(event.reminderMinutes.toLong())
        
        // Only schedule if reminder time is in the future
        if (reminderDateTime.isAfter(LocalDateTime.now())) {
            val intent = Intent(context, EventReminderReceiver::class.java).apply {
                putExtra(EventReminderReceiver.EXTRA_EVENT_ID, event.id)
                putExtra(EventReminderReceiver.EXTRA_EVENT_TITLE, event.title)
                putExtra(EventReminderReceiver.EXTRA_EVENT_DESCRIPTION, event.description)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                event.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val triggerTime = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelEventReminder(eventId: Long) {
        val intent = Intent(context, EventReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    
    fun updateEventReminder(event: Event) {
        cancelEventReminder(event.id)
        scheduleEventReminder(event)
    }
}
