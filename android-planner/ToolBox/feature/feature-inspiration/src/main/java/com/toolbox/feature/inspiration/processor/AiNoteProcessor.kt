package com.toolbox.feature.inspiration.processor

import com.toolbox.core.ai.command.AiCommandBatch
import com.toolbox.core.ai.command.CommandExecutor
import com.toolbox.core.ai.command.CommandHandler
import com.toolbox.core.ai.command.CommandResult
import com.toolbox.core.ai.provider.AiProviderConfig
import com.toolbox.core.ai.provider.AiProviderRegistry
import com.toolbox.core.ai.provider.ChatMessage
import com.toolbox.core.database.dao.AiProcessingLogDao
import com.toolbox.core.database.dao.AiProviderConfigDao
import com.toolbox.core.database.entity.AiProcessingLogEntity
import com.toolbox.core.common.util.IdGenerator
import com.toolbox.core.common.util.TimeUtil
import com.toolbox.feature.inspiration.data.InspirationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Processes notes using AI for categorization and tagging.
 */
class AiNoteProcessor @Inject constructor(
    private val providerRegistry: AiProviderRegistry,
    private val providerConfigDao: AiProviderConfigDao,
    private val processingLogDao: AiProcessingLogDao,
    private val commandExecutor: CommandExecutor,
    private val repository: InspirationRepository
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Process a single note with AI.
     */
    suspend fun processNote(
        noteId: String,
        providerType: String? = null,
        customSystemPrompt: String? = null
    ): AiProcessResult = withContext(Dispatchers.IO) {
        val note = repository.observeById(noteId).let { flow ->
            var result: com.toolbox.feature.inspiration.data.InspirationNote? = null
            flow.collect { result = it; return@collect }
            result ?: return@withContext AiProcessResult.Error("笔记不存在")
        }

        val config = providerConfigDao.getDefault()
            ?: return@withContext AiProcessResult.Error("未配置 AI Provider")

        val provider = providerRegistry.getProvider(providerType ?: config.providerType)

        val systemPrompt = customSystemPrompt ?: config.systemPrompt ?: DEFAULT_SYSTEM_PROMPT

        val userContent = buildString {
            appendLine("标题: ${note.title}")
            note.content?.let { appendLine("内容: $it") }
            if (note.tags.isNotEmpty()) appendLine("现有标签: ${note.tags.joinToString()}")
            note.category?.let { appendLine("现有分类: $it") }
        }

        val messages = listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(role = "user", content = userContent)
        )

        val providerConfig = AiProviderConfig(
            baseUrl = config.baseUrl,
            apiKey = "", // Key is encrypted, handled by provider
            model = config.model,
            maxTokens = config.maxTokens,
            temperature = config.temperature
        )

        try {
            val response = provider.chat(messages, providerConfig)

            // Parse commands from response
            val batch = commandExecutor.parseCommands(response.content)

            if (batch.commands.isEmpty()) {
                // Try parsing as direct JSON response
                val aiResult = try {
                    json.decodeFromString<AiClassificationResult>(response.content)
                } catch (e: Exception) {
                    null
                }

                if (aiResult != null) {
                    repository.updateAiResult(noteId, aiResult.category, aiResult.tags)
                    AiProcessResult.Success(
                        category = aiResult.category,
                        tags = aiResult.tags,
                        commands = emptyList()
                    )
                } else {
                    AiProcessResult.Error("AI 返回格式无法解析")
                }
            } else {
                // Execute commands
                val handler = NoteCommandHandler(repository)
                val results = commandExecutor.execute(batch, handler)

                val failed = results.filterIsInstance<CommandResult.Failure>()
                if (failed.isEmpty()) {
                    AiProcessResult.Success(
                        category = null,
                        tags = emptyList(),
                        commands = results.filterIsInstance<CommandResult.Success>().map { it.details ?: "" }
                    )
                } else {
                    AiProcessResult.PartialSuccess(
                        succeeded = results.filterIsInstance<CommandResult.Success>().size,
                        failed = failed.size,
                        errors = failed.map { it.error }
                    )
                }
            }.also {
                // Log processing
                processingLogDao.insert(AiProcessingLogEntity(
                    id = IdGenerator.generate(),
                    provider = provider.providerType,
                    model = config.model,
                    inputSummary = note.title,
                    responseRaw = response.content,
                    commandsExecuted = json.encodeToString(batch.commands.map { c -> c.type }),
                    status = "success",
                    tokensUsed = response.usage?.totalTokens,
                    createdAt = TimeUtil.nowMillis()
                ))
            }
        } catch (e: Exception) {
            processingLogDao.insert(AiProcessingLogEntity(
                id = IdGenerator.generate(),
                provider = providerType ?: config.providerType,
                model = config.model,
                inputSummary = note.title,
                responseRaw = null,
                commandsExecuted = null,
                status = "failed",
                errorMessage = e.message,
                createdAt = TimeUtil.nowMillis()
            ))
            AiProcessResult.Error(e.message ?: "AI 处理失败")
        }
    }

    /**
     * Process all unprocessed notes.
     */
    suspend fun processAllUnprocessed(
        providerType: String? = null
    ): List<AiProcessResult> {
        val unprocessed = repository.getUnprocessed()
        return unprocessed.map { note ->
            processNote(note.id, providerType)
        }
    }

    companion object {
        private const val DEFAULT_SYSTEM_PROMPT = """你是一个笔记分类助手。请分析用户提供的笔记内容，返回JSON格式的分类结果。

返回格式（严格JSON，不要其他文字）：
{"category":"分类名称","tags":["标签1","标签2","标签3"]}

分类建议：技术、生活、工作、学习、创意、读书、旅行、健康、财务、其他
标签：提取3-5个关键词作为标签"""
    }
}

@Serializable
data class AiClassificationResult(
    val category: String,
    val tags: List<String>
)

sealed class AiProcessResult {
    data class Success(
        val category: String?,
        val tags: List<String>,
        val commands: List<String>
    ) : AiProcessResult()

    data class PartialSuccess(
        val succeeded: Int,
        val failed: Int,
        val errors: List<String>
    ) : AiProcessResult()

    data class Error(val message: String) : AiProcessResult()
}

/**
 * Command handler for note operations.
 */
private class NoteCommandHandler(
    private val repository: InspirationRepository
) : CommandHandler {
    override suspend fun classify(noteId: String, category: String) {
        repository.updateAiResult(noteId, category, emptyList())
    }

    override suspend fun addTags(noteId: String, tags: List<String>) {
        val note = repository.observeById(noteId).let { flow ->
            var result: com.toolbox.feature.inspiration.data.InspirationNote? = null
            flow.collect { result = it; return@collect }
            result ?: return
        }
        val mergedTags = (note.tags + tags).distinct()
        repository.updateAiResult(noteId, note.category ?: "", mergedTags)
    }

    override suspend fun summarize(noteId: String): String {
        return "摘要功能需配合 AI prompt 实现"
    }

    override suspend fun moveToGroup(noteId: String, targetGroup: String) {
        repository.updateAiResult(noteId, targetGroup, emptyList())
    }

    override suspend fun setPriority(noteId: String, priority: Int) {
        val note = repository.observeById(noteId).let { flow ->
            var result: com.toolbox.feature.inspiration.data.InspirationNote? = null
            flow.collect { result = it; return@collect }
            result ?: return
        }
        repository.update(noteId, note.title, note.content, note.templateId, priority)
    }

    override suspend fun linkNotes(noteId: String, targetId: String) {
        // TODO: Implement note linking
    }

    override suspend fun archive(noteId: String) {
        repository.delete(noteId)
    }
}
