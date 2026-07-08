package com.toolbox.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.toolbox.core.notification.NotificationManager
import javax.inject.Inject

@HiltAndroidApp
class ToolBoxApp : Application() {

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager.createChannels()
    }
}
