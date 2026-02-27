package com.calendar.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calendar.app.data.AppDatabase
import com.calendar.app.data.Event
import com.calendar.app.notification.NotificationHelper
import com.calendar.app.utils.CustomCalendar
import com.calendar.app.utils.CustomYearMonth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    
    private val eventDao = AppDatabase.getDatabase(application).eventDao()
    private val notificationHelper = NotificationHelper(application)
    
    private val _currentMonthOffset = MutableStateFlow(0)
    val currentMonthOffset: StateFlow<Int> = _currentMonthOffset.asStateFlow()
    
    val currentYearMonth: StateFlow<CustomYearMonth> = _currentMonthOffset.map { offset ->
        CustomCalendar.getCustomYearMonth(offset)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomCalendar.getCurrentCustomYearMonth())
    
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()
    
    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()
    
    private val _showEventDialog = MutableStateFlow(false)
    val showEventDialog: StateFlow<Boolean> = _showEventDialog.asStateFlow()
    
    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()
    
    fun getEventsForDate(date: LocalDate): Flow<List<Event>> {
        return eventDao.getEventsForDate(date)
    }
    
    fun getEventsForCurrentMonth(): Flow<List<Event>> {
        return currentYearMonth.flatMapLatest { yearMonth ->
            val startDate = CustomCalendar.getDisplayDate(yearMonth, 1)
            val endDate = CustomCalendar.getDisplayDate(yearMonth, CustomCalendar.DAYS_IN_MONTH)
            eventDao.getEventsBetweenDates(startDate, endDate)
        }
    }
    
    fun navigateToPreviousMonth() {
        _currentMonthOffset.value -= 1
    }
    
    fun navigateToNextMonth() {
        _currentMonthOffset.value += 1
    }
    
    fun goToToday() {
        _currentMonthOffset.value = 0
    }
    
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    fun clearSelectedDate() {
        _selectedDate.value = null
    }
    
    fun showAddEventDialog(date: LocalDate) {
        _selectedDate.value = date
        _selectedEvent.value = null
        _showEventDialog.value = true
    }
    
    fun showEditEventDialog(event: Event) {
        _selectedEvent.value = event
        _selectedDate.value = event.date
        _showEventDialog.value = true
    }
    
    fun dismissEventDialog() {
        _showEventDialog.value = false
        _selectedEvent.value = null
    }
    
    fun showDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }
    
    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }
    
    fun saveEvent(
        title: String,
        description: String,
        date: LocalDate,
        time: LocalTime?,
        reminderMinutes: Int
    ) {
        viewModelScope.launch {
            val existingEvent = _selectedEvent.value
            val event = if (existingEvent != null) {
                existingEvent.copy(
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    reminderMinutes = reminderMinutes
                )
            } else {
                Event(
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    reminderMinutes = reminderMinutes
                )
            }
            
            val eventId = eventDao.insertEvent(event)
            val savedEvent = event.copy(id = eventId)
            
            notificationHelper.scheduleEventReminder(savedEvent)
            dismissEventDialog()
        }
    }
    
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            notificationHelper.cancelEventReminder(event.id)
            eventDao.deleteEvent(event)
            dismissDeleteConfirmation()
            dismissEventDialog()
        }
    }
    
    fun deleteEventById(eventId: Long) {
        viewModelScope.launch {
            notificationHelper.cancelEventReminder(eventId)
            eventDao.deleteEventById(eventId)
        }
    }
}
