package com.toolbox.feature.schedule.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.alarm.AlarmScheduler
import com.toolbox.core.alarm.ReminderEngine
import com.toolbox.core.database.dao.ScheduleEventDao
import com.toolbox.core.notification.NotificationManager
import com.toolbox.feature.schedule.data.ScheduleRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScheduleModule {

    @Provides
    @Singleton
    fun provideScheduleRepository(dao: ScheduleEventDao): ScheduleRepository {
        return ScheduleRepository(dao)
    }

    @Provides
    @Singleton
    fun provideReminderEngine(
        @ApplicationContext context: Context,
        alarmScheduler: AlarmScheduler,
        notificationManager: NotificationManager
    ): ReminderEngine {
        return ReminderEngine(context, alarmScheduler, notificationManager)
    }
}
