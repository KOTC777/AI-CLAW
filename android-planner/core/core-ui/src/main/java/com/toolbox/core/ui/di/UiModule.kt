package com.toolbox.core.ui.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.datastore.DataStoreManager
import com.toolbox.core.ui.background.BackgroundManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UiModule {

    @Provides
    @Singleton
    fun provideBackgroundManager(
        @ApplicationContext context: Context,
        dataStoreManager: DataStoreManager
    ): BackgroundManager = BackgroundManager(context, dataStoreManager)
}
