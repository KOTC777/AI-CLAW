package com.toolbox.feature.schedule.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toolbox.core.alarm.ReminderEngine
import com.toolbox.core.alarm.ReminderScheduleConfig
import com.toolbox.core.common.util.TimeUtil
import com.toolbox.feature.schedule.data.ReminderConfig
import com.toolbox.feature.schedule.data.RepeatRule
import com.toolbox.feature.schedule.data.ScheduleEvent
import com.toolbox.feature.schedule.data.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val reminderEngine: ReminderEngine
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _event = MutableStateFlow<ScheduleEvent?>(null)
    val event: StateFlow<ScheduleEvent?> = _event.asStateFlow()

    val events: StateFlow<List<ScheduleEvent>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDateSelected(date: Long) {
        _selectedDate.value = date
    }

    fun createEvent(
        title: String,
        description: String?,
        startTime: Long,
        endTime: Long,
        allDay: Boolean,
        repeatRule: RepeatRule?,
        reminderConfig: ReminderConfig?,
        color: Int?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val event = repository.create(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    allDay = allDay,
                    repeatRule = repeatRule,
                    reminderConfig = reminderConfig,
                    linkedTaskId = null,
                    color = color
                )

                // Schedule reminder if configured
                reminderConfig?.let { config ->
                    if (config.enabled) {
                        config.minutesBefore.forEach { minutes ->
                            reminderEngine.scheduleReminder(
                                ReminderScheduleConfig(
                                    id = event.id,
                                    title = event.title,
                                    content = event.description ?: "日程提醒",
                                    targetTime = event.startTime,
                                    minutesBefore = minutes,
                                    intensityLevel = config.intensityLevel,
                                    requestCode = event.id.hashCode() + minutes
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEvent(
        id: String,
        title: String,
        description: String?,
        startTime: Long,
        endTime: Long,
        allDay: Boolean,
        repeatRule: RepeatRule?,
        reminderConfig: ReminderConfig?,
        color: Int?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cancel old reminders
                reminderEngine.cancelReminder(id.hashCode())

                repository.update(
                    id = id,
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    allDay = allDay,
                    repeatRule = repeatRule,
                    reminderConfig = reminderConfig,
                    color = color
                )

                // Schedule new reminders
                reminderConfig?.let { config ->
                    if (config.enabled) {
                        config.minutesBefore.forEach { minutes ->
                            reminderEngine.scheduleReminder(
                                ReminderScheduleConfig(
                                    id = id,
                                    title = title,
                                    content = description ?: "日程提醒",
                                    targetTime = startTime,
                                    minutesBefore = minutes,
                                    intensityLevel = config.intensityLevel,
                                    requestCode = id.hashCode() + minutes
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            reminderEngine.cancelReminder(id.hashCode())
            repository.delete(id)
        }
    }
}

sealed class ScheduleUiEvent {
    data object Created : ScheduleUiEvent()
    data object Updated : ScheduleUiEvent()
    data object Deleted : ScheduleUiEvent()
    data class Error(val message: String) : ScheduleUiEvent()
}
