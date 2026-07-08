package com.toolbox.core.ai.provider

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * DeepSeek AI provider (OpenAI-compatible API).
 */
class DeepSeekProvider @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) : AiProvider {

    override val providerType: String = "deepseek"

    override suspend fun chat(
        messages: List<ChatMessage>,
        config: AiProviderConfig
    ): AiResponse {
        val request = ChatRequest(
            model = config.model,
            messages = messages,
            maxTokens = config.maxTokens,
            temperature = config.temperature
        )

        val requestBody = json.encodeToString(ChatRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("${config.baseUrl}/v1/chat/completions")
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(requestBody)
            .build()

        val response = client.newCall(httpRequest).execute()
        val responseBody = response.body?.string()
            ?: throw IllegalStateException("Empty response body")

        if (!response.isSuccessful) {
            throw IllegalStateException("API error ${response.code}: $responseBody")
        }

        val chatResponse = json.decodeFromString(ChatResponse.serializer(), responseBody)
        val choice = chatResponse.choices.firstOrNull()
            ?: throw IllegalStateException("No choices in response")

        return AiResponse(
            content = choice.message.content,
            usage = chatResponse.usage?.let {
                TokenUsage(
                    promptTokens = it.promptTokens,
                    completionTokens = it.completionTokens,
                    totalTokens = it.totalTokens
                )
            },
            finishReason = choice.finishReason
        )
    }
}

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val maxTokens: Int? = null,
    val temperature: Double? = null
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val message: ChatMessage,
    val finishReason: String? = null
)

@Serializable
data class Usage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)
