package com.toolbox.core.alarm

import android.content.Context
import com.toolbox.core.notification.NotificationManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Multi-level reminder engine.
 * Supports 5 intensity levels with time-based auto-escalation.
 */
class ReminderEngine @Inject constructor(
    private val context: Context,
    private val alarmScheduler: AlarmScheduler,
    private val notificationManager: NotificationManager
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Schedule a reminder with intensity config.
     */
    fun scheduleReminder(config: ReminderScheduleConfig) {
        val triggerTime = config.targetTime - (config.minutesBefore * 60 * 1000L)

        if (triggerTime <= System.currentTimeMillis()) {
            // Already past, trigger immediately
            triggerReminder(config)
            return
        }

        // Schedule exact alarm
        alarmScheduler.scheduleExact(
            triggerAtMillis = triggerTime,
            requestCode = config.requestCode,
            data = json.encodeToString(ReminderData.serializer(), ReminderData(
                id = config.id,
                title = config.title,
                content = config.content,
                intensityLevel = config.intensityLevel,
                type = config.type
            ))
        )
    }

    /**
     * Schedule escalating reminders for checkin tasks.
     * Creates multiple alarms at increasing intensity levels.
     */
    fun scheduleEscalatingReminders(config: EscalatingReminderConfig) {
        val now = System.currentTimeMillis()

        config.levels.forEachIndexed { index, level ->
            val triggerTime = config.deadlineTime - (level.minutesBefore * 60 * 1000L)

            if (triggerTime > now) {
                alarmScheduler.scheduleExact(
                    triggerAtMillis = triggerTime,
                    requestCode = config.requestCode + index,
                    data = json.encodeToString(ReminderData.serializer(), ReminderData(
                        id = config.id,
                        title = config.title,
                        content = level.message,
                        intensityLevel = level.intensity,
                        type = "checkin"
                    ))
                )
            }
        }
    }

    /**
     * Cancel all reminders for a specific item.
     */
    fun cancelReminder(requestCode: Int, levelCount: Int = 5) {
        repeat(levelCount) { index ->
            alarmScheduler.cancel(requestCode + index)
        }
    }

    /**
     * Trigger a reminder based on intensity level.
     */
    fun triggerReminder(config: ReminderScheduleConfig) {
        when (config.intensityLevel) {
            1 -> triggerLevel1(config)
            2 -> triggerLevel2(config)
            3 -> triggerLevel3(config)
            4 -> triggerLevel4(config)
            5 -> triggerLevel5(config)
        }
    }

    /**
     * Level 1: Simple notification.
     */
    private fun triggerLevel1(config: ReminderScheduleConfig) {
        val notification = notificationManager.buildNotification(
            channelId = NotificationManager.CHANNEL_REMINDER,
            title = config.title,
            content = config.content
        )
        notificationManager.show(config.requestCode, notification)
    }

    /**
     * Level 2: Persistent notification with vibration.
     */
    private fun triggerLevel2(config: ReminderScheduleConfig) {
        val notification = notificationManager.buildNotification(
            channelId = NotificationManager.CHANNEL_CHECKIN,
            title = "⏰ ${config.title}",
            content = config.content,
            priority = android.app.Notification.PRIORITY_HIGH
        ).setOngoing(true) // Cannot be swiped away
        notificationManager.show(config.requestCode, notification)
    }

    /**
     * Level 3: High priority notification (full screen intent).
     */
    private fun triggerLevel3(config: ReminderScheduleConfig) {
        val notification = notificationManager.buildNotification(
            channelId = NotificationManager.CHANNEL_CHECKIN,
            title = "⚠️ ${config.title}",
            content = config.content,
            priority = android.app.Notification.PRIORITY_MAX
        ).setOngoing(true)
        notificationManager.show(config.requestCode, notification)
    }

    /**
     * Level 4: Lock screen notification with sound + vibration.
     */
    private fun triggerLevel4(config: ReminderScheduleConfig) {
        val notification = notificationManager.buildNotification(
            channelId = NotificationManager.CHANNEL_CHECKIN,
            title = "🚨 ${config.title}",
            content = config.content,
            priority = android.app.Notification.PRIORITY_MAX
        ).apply {
            setOngoing(true)
            setDefaults(android.app.Notification.DEFAULT_ALL)
        }
        notificationManager.show(config.requestCode, notification)
    }

    /**
     * Level 5: Foreground service with continuous alerts.
     */
    private fun triggerLevel5(config: ReminderScheduleConfig) {
        // Show persistent notification
        triggerLevel4(config)
        // TODO: Start foreground service with periodic vibration/sound
    }
}

@Serializable
data class ReminderData(
    val id: String,
    val title: String,
    val content: String,
    val intensityLevel: Int,
    val type: String    // schedule|checkin
)

data class ReminderScheduleConfig(
    val id: String,
    val title: String,
    val content: String,
    val targetTime: Long,
    val minutesBefore: Int,
    val intensityLevel: Int,
    val requestCode: Int,
    val type: String = "schedule"
)

data class EscalatingReminderConfig(
    val id: String,
    val title: String,
    val deadlineTime: Long,
    val requestCode: Int,
    val levels: List<ReminderLevel>
)

data class ReminderLevel(
    val minutesBefore: Int,
    val intensity: Int,
    val message: String
)
