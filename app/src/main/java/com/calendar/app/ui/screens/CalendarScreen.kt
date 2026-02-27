package com.calendar.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.app.data.Event
import com.calendar.app.ui.CalendarViewModel
import com.calendar.app.utils.CustomCalendar
import com.calendar.app.utils.CustomYearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val currentYearMonth by viewModel.currentYearMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val showEventDialog by viewModel.showEventDialog.collectAsStateWithLifecycle()
    val events by viewModel.getEventsForCurrentMonth().collectAsStateWithLifecycle(initialValue = emptyList())
    
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = CustomCalendar.formatMonthYear(currentYearMonth),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateToPreviousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, "Previous month")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateToNextMonth() }) {
                        Icon(Icons.Default.ChevronRight, "Next month")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val today = LocalDate.now()
                    viewModel.showAddEventDialog(today)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add event")
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CalendarMonth, "Month") },
                        label = { Text("Month") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Today, "Today") },
                        label = { Text("Today") },
                        selected = false,
                        onClick = { viewModel.goToToday() }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragOffset > 100f -> viewModel.navigateToPreviousMonth()
                                dragOffset < -100f -> viewModel.navigateToNextMonth()
                            }
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    )
                }
        ) {
            // Weekday headers
            WeekdayHeader()
            
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
            
            // Calendar grid
            CalendarGrid(
                yearMonth = currentYearMonth,
                events = events,
                onDateClick = { date ->
                    viewModel.selectDate(date)
                    viewModel.showAddEventDialog(date)
                }
            )
        }
    }
    
    // Event Dialog
    if (showEventDialog) {
        selectedDate?.let { date ->
            EventDialog(
                date = date,
                event = null,
                onDismiss = { viewModel.dismissEventDialog() },
                onSave = { title, description, eventDate, time, reminderMinutes ->
                    viewModel.saveEvent(title, description, eventDate, time, reminderMinutes)
                },
                onDelete = { }
            )
        }
    }
}

@Composable
fun WeekdayHeader() {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: CustomYearMonth,
    events: List<Event>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfWeek = CustomCalendar.getFirstDayOfWeek(yearMonth)
    val daysInMonth = CustomCalendar.DAYS_IN_MONTH
    
    // Calculate empty cells before the first day
    val emptyCells = firstDayOfWeek.value % 7
    
    // Create list of all cells (empty + days)
    val totalCells = emptyCells + daysInMonth
    val cells = (0 until totalCells).map { index ->
        if (index < emptyCells) {
            null
        } else {
            index - emptyCells + 1
        }
    }
    
    // Group events by date for quick lookup
    val eventsByDate = events.groupBy { it.date }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false
    ) {
        items(cells) { day ->
            DayCell(
                day = day,
                yearMonth = yearMonth,
                events = day?.let { eventsByDate[CustomCalendar.getDisplayDate(yearMonth, it)] } ?: emptyList(),
                onClick = { day?.let { onDateClick(CustomCalendar.getDisplayDate(yearMonth, it)) } }
            )
        }
    }
}

@Composable
fun DayCell(
    day: Int?,
    yearMonth: CustomYearMonth,
    events: List<Event>,
    onClick: () -> Unit
) {
    if (day == null) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(4.dp)
        )
        return
    }
    
    val date = CustomCalendar.getDisplayDate(yearMonth, day)
    val isToday = CustomCalendar.isToday(yearMonth, day)
    val hasEvents = events.isNotEmpty()
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(
                if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day number
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Event indicators
            if (hasEvents) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    events.take(3).forEach { _ ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

// ViewModel Factory
class CalendarViewModelFactory(
    private val application: android.app.Application
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
