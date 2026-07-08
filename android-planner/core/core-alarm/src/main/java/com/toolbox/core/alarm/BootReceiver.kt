package com.toolbox.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-schedule alarms after device boot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: Re-schedule all active alarms from database
            // This will be implemented in the reminder engine
        }
    }
}
