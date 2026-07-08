package com.toolbox.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Manages alarm scheduling for reminders.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Check if exact alarms can be scheduled (Android 12+).
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedule an exact alarm.
     */
    fun scheduleExact(
        triggerAtMillis: Long,
        requestCode: Int,
        data: String
    ) {
        val intent = createAlarmIntent(requestCode, data)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            // Fallback: inexact alarm
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Schedule a repeating alarm.
     */
    fun scheduleRepeating(
        intervalMillis: Long,
        requestCode: Int,
        data: String
    ) {
        val intent = createAlarmIntent(requestCode, data)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }

    /**
     * Cancel a scheduled alarm.
     */
    fun cancel(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun createAlarmIntent(requestCode: Int, data: String): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_REQUEST_CODE, requestCode)
            putExtra(EXTRA_DATA, data)
        }
    }

    companion object {
        const val EXTRA_REQUEST_CODE = "extra_request_code"
        const val EXTRA_DATA = "extra_data"
    }
}
