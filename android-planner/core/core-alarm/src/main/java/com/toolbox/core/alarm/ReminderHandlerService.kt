package com.toolbox.core.alarm

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Foreground service that handles reminder events.
 * Dispatches to the appropriate handler based on reminder type.
 */
class ReminderHandlerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val requestCode = intent?.getIntExtra(AlarmScheduler.EXTRA_REQUEST_CODE, -1) ?: -1
        val data = intent?.getStringExtra(AlarmScheduler.EXTRA_DATA)

        if (requestCode != -1 && data != null) {
            handleReminder(requestCode, data)
        }

        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun handleReminder(requestCode: Int, data: String) {
        // Parse the reminder data and dispatch to appropriate handler
        // This will be connected to the reminder engine in feature modules
    }
}
