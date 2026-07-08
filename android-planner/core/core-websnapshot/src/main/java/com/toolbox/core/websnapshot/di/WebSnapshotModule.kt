package com.toolbox.core.websnapshot.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.websnapshot.WebSnapshotEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSnapshotModule {

    @Provides
    @Singleton
    fun provideWebSnapshotEngine(@ApplicationContext context: Context): WebSnapshotEngine {
        return WebSnapshotEngine(context)
    }
}
