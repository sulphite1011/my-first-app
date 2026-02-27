package com.calendar.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val time: LocalTime? = null,
    val reminderMinutes: Int = 15,
    val createdAt: Long = System.currentTimeMillis()
)
