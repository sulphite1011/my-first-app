package com.calendar.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calendar.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all event reminders after boot
            val notificationHelper = NotificationHelper(context)
            val eventDao = AppDatabase.getDatabase(context).eventDao()
            
            CoroutineScope(Dispatchers.IO).launch {
                val events = eventDao.getAllEvents().first()
                events.forEach { event ->
                    notificationHelper.scheduleEventReminder(event)
                }
            }
        }
    }
}
