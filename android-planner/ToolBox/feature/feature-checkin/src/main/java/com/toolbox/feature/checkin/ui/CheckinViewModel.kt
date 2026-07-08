package com.toolbox.feature.checkin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toolbox.core.alarm.EscalatingReminderConfig
import com.toolbox.core.alarm.ReminderEngine
import com.toolbox.core.alarm.ReminderLevel
import com.toolbox.feature.checkin.data.CheckinRecord
import com.toolbox.feature.checkin.data.CheckinRepository
import com.toolbox.feature.checkin.data.CheckinTask
import com.toolbox.feature.checkin.data.IntensityConfig
import com.toolbox.feature.checkin.data.RepeatRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckinViewModel @Inject constructor(
    private val repository: CheckinRepository,
    private val reminderEngine: ReminderEngine
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _event = MutableStateFlow<CheckinEvent?>(null)
    val event: StateFlow<CheckinEvent?> = _event.asStateFlow()

    private val _selectedTask = MutableStateFlow<CheckinTask?>(null)
    val selectedTask: StateFlow<CheckinTask?> = _selectedTask.asStateFlow()

    val tasks: StateFlow<List<CheckinTask>> = repository.observeActiveTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createTask(
        title: String,
        description: String?,
        deadlineTime: String?,
        intensityConfig: IntensityConfig,
        proofType: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val task = repository.createTask(
                    title = title,
                    description = description,
                    groupId = null,
                    repeatRule = RepeatRule(type = "daily"),
                    deadlineTime = deadlineTime,
                    intensityConfig = intensityConfig,
                    proofType = proofType
                )

                // Schedule escalating reminders
                if (deadlineTime != null) {
                    scheduleReminders(task)
                }

                _event.value = CheckinEvent.TaskCreated
            } catch (e: Exception) {
                _event.value = CheckinEvent.Error(e.message ?: "创建失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkin(taskId: String, proofType: String, proofData: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val record = repository.checkin(taskId, proofType, proofData)

                // Cancel reminders for today
                reminderEngine.cancelReminder(taskId.hashCode())

                _event.value = CheckinEvent.CheckedIn(record)
            } catch (e: Exception) {
                _event.value = CheckinEvent.Error(e.message ?: "打卡失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTaskDetail(taskId: String) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            _selectedTask.value = task
        }
    }

    fun archiveTask(taskId: String) {
        viewModelScope.launch {
            reminderEngine.cancelReminder(taskId.hashCode())
            repository.archiveTask(taskId)
        }
    }

    private fun scheduleReminders(task: CheckinTask) {
        val deadlineTime = task.deadlineTime ?: return
        val parts = deadlineTime.split(":")
        if (parts.size != 2) return

        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val now = java.util.Calendar.getInstance()
        val deadline = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }

        if (deadline.before(now)) {
            deadline.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        val levels = task.intensityConfig.levels.map { level ->
            ReminderLevel(
                minutesBefore = level.minutesBefore,
                intensity = level.intensity,
                message = if (level.minutesBefore <= 0) {
                    "🚨 打卡时间已到: ${task.title}"
                } else {
                    "⏰ 还有 ${level.minutesBefore} 分钟: ${task.title}"
                }
            )
        }

        reminderEngine.scheduleEscalatingReminders(
            EscalatingReminderConfig(
                id = task.id,
                title = task.title,
                deadlineTime = deadline.timeInMillis,
                requestCode = task.id.hashCode(),
                levels = levels
            )
        )
    }

    fun clearEvent() {
        _event.value = null
    }
}

sealed class CheckinEvent {
    data object TaskCreated : CheckinEvent()
    data class CheckedIn(val record: CheckinRecord) : CheckinEvent()
    data class Error(val message: String) : CheckinEvent()
}
