package com.calendar.app.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Custom calendar system where EVERY month is February with exactly 30 days.
 * This is the core gimmick of the app - all months display as February with 30 days.
 */
object CustomCalendar {
    
    const val DAYS_IN_MONTH = 30
    const val MONTH_NAME = "February"
    
    /**
     * Gets the custom YearMonth for a given offset from today.
     * All months are displayed as February with 30 days.
     */
    fun getCustomYearMonth(offset: Int): CustomYearMonth {
        val today = LocalDate.now()
        val targetMonth = today.plusMonths(offset.toLong())
        return CustomYearMonth(
            year = targetMonth.year,
            monthOffset = offset,
            displayName = MONTH_NAME
        )
    }
    
    /**
     * Gets the custom date for a given day in a custom month.
     * Maps the custom February 30-day calendar to actual dates.
     */
    fun getCustomDate(yearMonth: CustomYearMonth, day: Int): LocalDate {
        val today = LocalDate.now()
        val actualMonth = today.plusMonths(yearMonth.monthOffset.toLong())
        // Create a date in the actual month, clamping to valid days
        val dayOfMonth = day.coerceIn(1, actualMonth.lengthOfMonth())
        return LocalDate.of(actualMonth.year, actualMonth.month, dayOfMonth)
    }
    
    /**
     * Gets the display date for a cell in the calendar grid.
     * This maps the real current date into our fictional February 30-day calendar.
     */
    fun getDisplayDate(yearMonth: CustomYearMonth, day: Int): LocalDate {
        // For the custom calendar, we always show February with 30 days
        // But we need to map to real dates for data storage
        val today = LocalDate.now()
        val actualMonth = today.plusMonths(yearMonth.monthOffset.toLong())
        
        // Clamp day to the actual month's length
        val actualLength = actualMonth.lengthOfMonth()
        val clampedDay = day.coerceIn(1, actualLength)
        
        return LocalDate.of(actualMonth.year, actualMonth.month, clampedDay)
    }
    
    /**
     * Gets the first day of week for a custom month.
     */
    fun getFirstDayOfWeek(yearMonth: CustomYearMonth): DayOfWeek {
        val today = LocalDate.now()
        val actualMonth = today.plusMonths(yearMonth.monthOffset.toLong())
        return LocalDate.of(actualMonth.year, actualMonth.month, 1).dayOfWeek
    }
    
    /**
     * Gets the day of week for a specific date in the custom calendar.
     */
    fun getDayOfWeek(yearMonth: CustomYearMonth, day: Int): DayOfWeek {
        val date = getDisplayDate(yearMonth, day)
        return date.dayOfWeek
    }
    
    /**
     * Checks if a given date is "today" in the custom calendar context.
     */
    fun isToday(yearMonth: CustomYearMonth, day: Int): Boolean {
        val today = LocalDate.now()
        val displayDate = getDisplayDate(yearMonth, day)
        return today == displayDate
    }
    
    /**
     * Gets the current custom year month (for today).
     */
    fun getCurrentCustomYearMonth(): CustomYearMonth {
        return getCustomYearMonth(0)
    }
    
    /**
     * Gets the offset from the current month for a given real date.
     */
    fun getOffsetForDate(date: LocalDate): Int {
        val today = LocalDate.now()
        val todayYearMonth = YearMonth.from(today)
        val dateYearMonth = YearMonth.from(date)
        return todayYearMonth.until(dateYearMonth).toInt()
    }
    
    /**
     * Formats the month/year display.
     */
    fun formatMonthYear(yearMonth: CustomYearMonth): String {
        return "$MONTH_NAME ${yearMonth.year}"
    }
}

/**
 * Represents a custom year/month in the February-30-day calendar system.
 */
data class CustomYearMonth(
    val year: Int,
    val monthOffset: Int,
    val displayName: String
) {
    fun getDaysInMonth(): Int = CustomCalendar.DAYS_IN_MONTH
}

/**
 * Represents a date in the custom calendar system.
 */
data class CustomDate(
    val yearMonth: CustomYearMonth,
    val day: Int
) {
    fun toLocalDate(): LocalDate {
        return CustomCalendar.getDisplayDate(yearMonth, day)
    }
    
    fun isToday(): Boolean {
        return CustomCalendar.isToday(yearMonth, day)
    }
}
