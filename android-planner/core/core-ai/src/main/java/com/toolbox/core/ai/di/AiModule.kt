package com.toolbox.core.ai.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.ai.command.CommandExecutor
import com.toolbox.core.ai.provider.AiProvider
import com.toolbox.core.ai.provider.AiProviderRegistry
import com.toolbox.core.ai.provider.DeepSeekProvider
import com.toolbox.core.ai.provider.OpenAiCompatibleProvider
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideDeepSeekProvider(
        client: OkHttpClient,
        json: Json
    ): DeepSeekProvider = DeepSeekProvider(client, json)

    @Provides
    @Singleton
    fun provideOpenAiCompatibleProvider(
        deepSeekProvider: DeepSeekProvider
    ): OpenAiCompatibleProvider = OpenAiCompatibleProvider(deepSeekProvider)

    @Provides
    @Singleton
    fun provideAiProviderRegistry(
        deepSeekProvider: DeepSeekProvider,
        openAiCompatibleProvider: OpenAiCompatibleProvider
    ): AiProviderRegistry {
        val providers = mapOf(
            "deepseek" to deepSeekProvider,
            "openai_compatible" to openAiCompatibleProvider
        )
        return AiProviderRegistry(providers)
    }

    @Provides
    @Singleton
    fun provideCommandExecutor(): CommandExecutor = CommandExecutor()
}
