package com.toolbox.feature.checkin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.alarm.ReminderEngine
import com.toolbox.core.database.dao.CheckinRecordDao
import com.toolbox.core.database.dao.CheckinTaskDao
import com.toolbox.feature.checkin.data.CheckinRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CheckinModule {

    @Provides
    @Singleton
    fun provideCheckinRepository(
        taskDao: CheckinTaskDao,
        recordDao: CheckinRecordDao
    ): CheckinRepository {
        return CheckinRepository(taskDao, recordDao)
    }
}
