package com.toolbox.feature.inspiration.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.ai.command.CommandExecutor
import com.toolbox.core.ai.provider.AiProviderRegistry
import com.toolbox.core.database.dao.AiProcessingLogDao
import com.toolbox.core.database.dao.AiProviderConfigDao
import com.toolbox.core.database.dao.InspirationAttachmentDao
import com.toolbox.core.database.dao.InspirationNoteDao
import com.toolbox.core.database.dao.InspirationTemplateDao
import com.toolbox.core.websnapshot.WebSnapshotEngine
import com.toolbox.feature.inspiration.data.InspirationRepository
import com.toolbox.feature.inspiration.processor.AiNoteProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InspirationModule {

    @Provides
    @Singleton
    fun provideInspirationRepository(
        noteDao: InspirationNoteDao,
        attachmentDao: InspirationAttachmentDao,
        templateDao: InspirationTemplateDao
    ): InspirationRepository {
        return InspirationRepository(noteDao, attachmentDao, templateDao)
    }

    @Provides
    @Singleton
    fun provideAiNoteProcessor(
        providerRegistry: AiProviderRegistry,
        providerConfigDao: AiProviderConfigDao,
        processingLogDao: AiProcessingLogDao,
        commandExecutor: CommandExecutor,
        repository: InspirationRepository
    ): AiNoteProcessor {
        return AiNoteProcessor(providerRegistry, providerConfigDao, processingLogDao, commandExecutor, repository)
    }
}
