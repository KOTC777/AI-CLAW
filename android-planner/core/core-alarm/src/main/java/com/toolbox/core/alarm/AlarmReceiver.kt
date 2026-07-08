package com.toolbox.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver for alarm events.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra(AlarmScheduler.EXTRA_REQUEST_CODE, -1)
        val data = intent.getStringExtra(AlarmScheduler.EXTRA_DATA) ?: return

        // Forward to reminder handler service
        val serviceIntent = Intent(context, ReminderHandlerService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_REQUEST_CODE, requestCode)
            putExtra(AlarmScheduler.EXTRA_DATA, data)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
