package com.toolbox.core.ai.command

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Commands that AI can request the app to execute.
 * AI returns structured JSON with commands; CommandExecutor validates and executes them.
 */
@Serializable
data class AiCommand(
    val type: String,       // classify|tag|summarize|move|set_priority|link|archive
    val noteId: String,
    val params: Map<String, String> = emptyMap()
)

@Serializable
data class AiCommandBatch(
    val commands: List<AiCommand>
)

/**
 * Result of executing a command.
 */
sealed class CommandResult {
    data class Success(val command: AiCommand, val details: String? = null) : CommandResult()
    data class Failure(val command: AiCommand, val error: String) : CommandResult()
}

/**
 * Validates and executes AI-generated commands.
 */
class CommandExecutor {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parse AI response into a batch of commands.
     */
    fun parseCommands(responseContent: String): AiCommandBatch {
        return try {
            json.decodeFromString(AiCommandBatch.serializer(), responseContent.trim())
        } catch (e: Exception) {
            // Try parsing single command
            try {
                val single = json.decodeFromString(AiCommand.serializer(), responseContent.trim())
                AiCommandBatch(commands = listOf(single))
            } catch (e2: Exception) {
                AiCommandBatch(commands = emptyList())
            }
        }
    }

    /**
     * Validate a command before execution.
     */
    fun validate(command: AiCommand): String? {
        val validTypes = setOf("classify", "tag", "summarize", "move", "set_priority", "link", "archive")
        if (command.type !in validTypes) {
            return "Unknown command type: ${command.type}"
        }
        if (command.noteId.isBlank()) {
            return "noteId is required"
        }
        return null // Valid
    }

    /**
     * Execute a batch of commands.
     * @param handler The handler that performs actual data operations.
     */
    suspend fun execute(
        batch: AiCommandBatch,
        handler: CommandHandler
    ): List<CommandResult> {
        return batch.commands.map { command ->
            val error = validate(command)
            if (error != null) {
                return@map CommandResult.Failure(command, error)
            }

            try {
                when (command.type) {
                    "classify" -> {
                        val category = command.params["category"]
                            ?: return@map CommandResult.Failure(command, "Missing 'category' param")
                        handler.classify(command.noteId, category)
                        CommandResult.Success(command, "Classified as '$category'")
                    }
                    "tag" -> {
                        val tags = command.params["tags"]?.split(",")?.map { it.trim() }
                            ?: return@map CommandResult.Failure(command, "Missing 'tags' param")
                        handler.addTags(command.noteId, tags)
                        CommandResult.Success(command, "Tagged with ${tags.joinToString()}")
                    }
                    "summarize" -> {
                        val summary = handler.summarize(command.noteId)
                        CommandResult.Success(command, summary)
                    }
                    "move" -> {
                        val target = command.params["target"]
                            ?: return@map CommandResult.Failure(command, "Missing 'target' param")
                        handler.moveToGroup(command.noteId, target)
                        CommandResult.Success(command, "Moved to '$target'")
                    }
                    "set_priority" -> {
                        val priority = command.params["value"]?.toIntOrNull()
                            ?: return@map CommandResult.Failure(command, "Invalid 'value' param")
                        handler.setPriority(command.noteId, priority)
                        CommandResult.Success(command, "Priority set to $priority")
                    }
                    "link" -> {
                        val targetId = command.params["targetId"]
                            ?: return@map CommandResult.Failure(command, "Missing 'targetId' param")
                        handler.linkNotes(command.noteId, targetId)
                        CommandResult.Success(command, "Linked to $targetId")
                    }
                    "archive" -> {
                        handler.archive(command.noteId)
                        CommandResult.Success(command, "Archived")
                    }
                    else -> CommandResult.Failure(command, "Unhandled command type: ${command.type}")
                }
            } catch (e: Exception) {
                CommandResult.Failure(command, e.message ?: "Unknown error")
            }
        }
    }
}

/**
 * Interface for handling command execution.
 * Implemented by feature modules.
 */
interface CommandHandler {
    suspend fun classify(noteId: String, category: String)
    suspend fun addTags(noteId: String, tags: List<String>)
    suspend fun summarize(noteId: String): String
    suspend fun moveToGroup(noteId: String, targetGroup: String)
    suspend fun setPriority(noteId: String, priority: Int)
    suspend fun linkNotes(noteId: String, targetId: String)
    suspend fun archive(noteId: String)
}
