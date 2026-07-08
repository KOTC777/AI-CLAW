package com.toolbox.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Manages notification channels and builds notifications.
 */
class NotificationManager(private val context: Context) {

    private val systemManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Create all notification channels.
     */
    fun createChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_REMINDER,
                "日程提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "日程事件提醒通知"
                enableVibration(true)
            },
            NotificationChannel(
                CHANNEL_CHECKIN,
                "打卡提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "任务打卡强制提醒"
                enableVibration(true)
                enableLights(true)
            },
            NotificationChannel(
                CHANNEL_AI,
                "AI 处理",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "AI 分类和处理结果通知"
            },
            NotificationChannel(
                CHANNEL_FOREGROUND,
                "前台服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台服务运行状态"
            }
        )
        systemManager.createNotificationChannels(channels)
    }

    /**
     * Build a basic notification.
     */
    fun buildNotification(
        channelId: String,
        title: String,
        content: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(priority)
            .setAutoCancel(true)
    }

    /**
     * Show a notification.
     */
    fun show(id: Int, notification: NotificationCompat.Builder) {
        systemManager.notify(id, notification.build())
    }

    /**
     * Cancel a notification.
     */
    fun cancel(id: Int) {
        systemManager.cancel(id)
    }

    companion object {
        const val CHANNEL_REMINDER = "channel_reminder"
        const val CHANNEL_CHECKIN = "channel_checkin"
        const val CHANNEL_AI = "channel_ai"
        const val CHANNEL_FOREGROUND = "channel_foreground"

        const val NOTIFICATION_ID_REMINDER = 2001
        const val NOTIFICATION_ID_CHECKIN = 2002
        const val NOTIFICATION_ID_AI = 2003
        const val NOTIFICATION_ID_FOREGROUND = 2004
    }
}
