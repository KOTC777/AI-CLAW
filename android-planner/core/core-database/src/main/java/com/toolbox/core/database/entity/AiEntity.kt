package com.toolbox.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_processing_log")
data class AiProcessingLogEntity(
    @PrimaryKey val id: String,
    val provider: String,
    val model: String? = null,
    @ColumnInfo(name = "input_summary") val inputSummary: String? = null,
    @ColumnInfo(name = "response_raw") val responseRaw: String? = null,
    @ColumnInfo(name = "commands_executed") val commandsExecuted: String? = null, // JSON
    val status: String,                     // success|failed|partial
    @ColumnInfo(name = "error_message") val errorMessage: String? = null,
    @ColumnInfo(name = "tokens_used") val tokensUsed: Int? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "ai_provider_config")
data class AiProviderConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "provider_type") val providerType: String,   // deepseek|openai|claude|custom
    @ColumnInfo(name = "base_url") val baseUrl: String,
    @ColumnInfo(name = "api_key_encrypted") val apiKeyEncrypted: ByteArray,
    @ColumnInfo(name = "api_key_iv") val apiKeyIv: ByteArray,
    @ColumnInfo(name = "api_key_tag") val apiKeyTag: ByteArray,
    val model: String,
    @ColumnInfo(name = "system_prompt") val systemPrompt: String? = null,
    @ColumnInfo(name = "max_tokens") val maxTokens: Int = 4096,
    val temperature: Double = 0.7,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AiProviderConfigEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

@Entity(tableName = "ai_scheduled_task")
data class AiScheduledTaskEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "cron_expression") val cronExpression: String,
    @ColumnInfo(name = "task_type") val taskType: String,           // classify|summarize|organize
    @ColumnInfo(name = "target_scope") val targetScope: String,     // JSON
    @ColumnInfo(name = "provider_config_id") val providerConfigId: String,
    val enabled: Boolean = true,
    @ColumnInfo(name = "last_run") val lastRun: Long? = null,
    @ColumnInfo(name = "next_run") val nextRun: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "app_lock_config")
data class AppLockConfigEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "lock_type") val lockType: String,           // app|feature
    @ColumnInfo(name = "feature_id") val featureId: String? = null, // NULL = app lock
    @ColumnInfo(name = "auth_method") val authMethod: String,       // biometric|pin|pattern
    @ColumnInfo(name = "pin_hash") val pinHash: String? = null,
    @ColumnInfo(name = "pin_salt") val pinSalt: String? = null,
    val enabled: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
