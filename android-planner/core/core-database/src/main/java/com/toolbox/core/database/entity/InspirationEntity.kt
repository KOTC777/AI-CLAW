package com.toolbox.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "inspiration_note")
data class InspirationNoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String? = null,
    @ColumnInfo(name = "template_id") val templateId: String? = null,
    val category: String? = null,           // AI-assigned category
    val tags: String? = null,               // JSON array
    val priority: Int = 0,
    @ColumnInfo(name = "ai_processed") val aiProcessed: Boolean = false,
    @ColumnInfo(name = "ai_last_run") val aiLastRun: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null
)

@Entity(
    tableName = "inspiration_attachment",
    foreignKeys = [
        ForeignKey(
            entity = InspirationNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["note_id"])]
)
data class InspirationAttachmentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "note_id") val noteId: String,
    val type: String,                       // drawing|audio|link|web_snapshot|image
    @ColumnInfo(name = "file_path") val filePath: String? = null,
    val url: String? = null,
    val metadata: String? = null,           // JSON
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "inspiration_template")
data class InspirationTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String? = null,
    val structure: String,                  // JSON: template field definitions
    @ColumnInfo(name = "system_prompt") val systemPrompt: String? = null,
    @ColumnInfo(name = "is_builtin") val isBuiltin: Boolean = false,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
