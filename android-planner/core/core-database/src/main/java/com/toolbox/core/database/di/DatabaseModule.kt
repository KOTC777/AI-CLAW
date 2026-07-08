package com.toolbox.core.database.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.database.AppDatabase
import com.toolbox.core.database.dao.AiProcessingLogDao
import com.toolbox.core.database.dao.AiProviderConfigDao
import com.toolbox.core.database.dao.AiScheduledTaskDao
import com.toolbox.core.database.dao.AppLockConfigDao
import com.toolbox.core.database.dao.CheckinRecordDao
import com.toolbox.core.database.dao.CheckinTaskDao
import com.toolbox.core.database.dao.InspirationAttachmentDao
import com.toolbox.core.database.dao.InspirationNoteDao
import com.toolbox.core.database.dao.InspirationTemplateDao
import com.toolbox.core.database.dao.MemoDao
import com.toolbox.core.database.dao.PasswordEntryDao
import com.toolbox.core.database.dao.PasswordGroupDao
import com.toolbox.core.database.dao.ScheduleEventDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Note: AppDatabase is not provided here directly because it needs a passphrase.
    // It will be provided by a dynamic module or initialized at runtime.

    @Provides
    @Singleton
    fun provideMemoDao(database: AppDatabase): MemoDao = database.memoDao()

    @Provides
    @Singleton
    fun provideScheduleEventDao(database: AppDatabase): ScheduleEventDao = database.scheduleEventDao()

    @Provides
    @Singleton
    fun provideCheckinTaskDao(database: AppDatabase): CheckinTaskDao = database.checkinTaskDao()

    @Provides
    @Singleton
    fun provideCheckinRecordDao(database: AppDatabase): CheckinRecordDao = database.checkinRecordDao()

    @Provides
    @Singleton
    fun providePasswordGroupDao(database: AppDatabase): PasswordGroupDao = database.passwordGroupDao()

    @Provides
    @Singleton
    fun providePasswordEntryDao(database: AppDatabase): PasswordEntryDao = database.passwordEntryDao()

    @Provides
    @Singleton
    fun provideInspirationNoteDao(database: AppDatabase): InspirationNoteDao = database.inspirationNoteDao()

    @Provides
    @Singleton
    fun provideInspirationAttachmentDao(database: AppDatabase): InspirationAttachmentDao = database.inspirationAttachmentDao()

    @Provides
    @Singleton
    fun provideInspirationTemplateDao(database: AppDatabase): InspirationTemplateDao = database.inspirationTemplateDao()

    @Provides
    @Singleton
    fun provideAiProcessingLogDao(database: AppDatabase): AiProcessingLogDao = database.aiProcessingLogDao()

    @Provides
    @Singleton
    fun provideAiProviderConfigDao(database: AppDatabase): AiProviderConfigDao = database.aiProviderConfigDao()

    @Provides
    @Singleton
    fun provideAiScheduledTaskDao(database: AppDatabase): AiScheduledTaskDao = database.aiScheduledTaskDao()

    @Provides
    @Singleton
    fun provideAppLockConfigDao(database: AppDatabase): AppLockConfigDao = database.appLockConfigDao()
}
