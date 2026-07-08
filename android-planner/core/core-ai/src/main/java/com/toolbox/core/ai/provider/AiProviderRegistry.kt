package com.toolbox.core.ai.provider

import javax.inject.Inject

/**
 * Registry for AI providers. Allows dynamic provider selection.
 */
class AiProviderRegistry @Inject constructor(
    private val providers: Map<String, @JvmSuppressWildcards AiProvider>
) {
    fun getProvider(type: String): AiProvider {
        return providers[type]
            ?: throw IllegalArgumentException("Unknown AI provider type: $type")
    }

    fun getAvailableProviders(): List<String> = providers.keys.toList()
}

/**
 * OpenAI-compatible provider that works with any OpenAI-compatible API.
 */
class OpenAiCompatibleProvider @Inject constructor(
    private val deepSeekProvider: DeepSeekProvider
) : AiProvider {

    override val providerType: String = "openai_compatible"

    override suspend fun chat(
        messages: List<ChatMessage>,
        config: AiProviderConfig
    ): AiResponse {
        // Uses the same implementation as DeepSeek (OpenAI-compatible)
        return deepSeekProvider.chat(messages, config)
    }
}
