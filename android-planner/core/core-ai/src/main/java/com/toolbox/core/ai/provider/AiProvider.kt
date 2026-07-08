package com.toolbox.core.ai.provider

import kotlinx.serialization.Serializable

/**
 * Abstract interface for AI providers.
 */
interface AiProvider {
    val providerType: String

    suspend fun chat(
        messages: List<ChatMessage>,
        config: AiProviderConfig
    ): AiResponse
}

@Serializable
data class ChatMessage(
    val role: String,       // system|user|assistant
    val content: String
)

@Serializable
data class AiProviderConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val systemPrompt: String? = null,
    val maxTokens: Int = 4096,
    val temperature: Double = 0.7
)

@Serializable
data class AiResponse(
    val content: String,
    val usage: TokenUsage? = null,
    val finishReason: String? = null
)

@Serializable
data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)
