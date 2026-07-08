package com.toolbox.feature.inspiration.data

import com.toolbox.core.common.util.IdGenerator
import com.toolbox.core.common.util.TimeUtil
import com.toolbox.core.database.dao.InspirationAttachmentDao
import com.toolbox.core.database.dao.InspirationNoteDao
import com.toolbox.core.database.dao.InspirationTemplateDao
import com.toolbox.core.database.entity.InspirationAttachmentEntity
import com.toolbox.core.database.entity.InspirationNoteEntity
import com.toolbox.core.database.entity.InspirationTemplateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Domain models

data class InspirationNote(
    val id: String,
    val title: String,
    val content: String?,
    val templateId: String?,
    val category: String?,
    val tags: List<String>,
    val priority: Int,
    val aiProcessed: Boolean,
    val attachments: List<InspirationAttachment>,
    val createdAt: Long,
    val updatedAt: Long
)

data class InspirationAttachment(
    val id: String,
    val noteId: String,
    val type: String,           // drawing|audio|link|web_snapshot|image
    val filePath: String?,
    val url: String?,
    val metadata: Map<String, String>?,
    val createdAt: Long
)

data class InspirationTemplate(
    val id: String,
    val name: String,
    val icon: String?,
    val structure: TemplateStructure,
    val systemPrompt: String?,
    val isBuiltin: Boolean
)

@Serializable
data class TemplateStructure(
    val fields: List<TemplateField>
)

@Serializable
data class TemplateField(
    val name: String,
    val type: String,       // text|number|date|select
    val label: String,
    val placeholder: String? = null,
    val options: List<String>? = null
)

// Repository

class InspirationRepository @Inject constructor(
    private val noteDao: InspirationNoteDao,
    private val attachmentDao: InspirationAttachmentDao,
    private val templateDao: InspirationTemplateDao
) {

    private val json = Json { ignoreUnknownKeys = true }

    // Notes

    fun observeAll(): Flow<List<InspirationNote>> =
        noteDao.observeAll().map { list -> list.map { it.toDomain(emptyList()) } }

    fun observeById(id: String): Flow<InspirationNote?> {
        return noteDao.observeById(id).map { entity ->
            entity?.let {
                val attachments = attachmentDao.getByNoteId(it.id)
                it.toDomain(attachments.map { a -> a.toDomain() })
            }
        }
    }

    fun observeByCategory(category: String): Flow<List<InspirationNote>> =
        noteDao.observeByCategory(category).map { list -> list.map { it.toDomain(emptyList()) } }

    fun search(query: String): Flow<List<InspirationNote>> =
        noteDao.search(query).map { list -> list.map { it.toDomain(emptyList()) } }

    suspend fun getUnprocessed(): List<InspirationNote> =
        noteDao.getUnprocessed().map { it.toDomain(emptyList()) }

    suspend fun create(
        title: String,
        content: String?,
        templateId: String?,
        priority: Int = 0
    ): InspirationNote {
        val now = TimeUtil.nowMillis()
        val entity = InspirationNoteEntity(
            id = IdGenerator.generate(),
            title = title,
            content = content,
            templateId = templateId,
            createdAt = now,
            updatedAt = now
        )
        noteDao.insert(entity)
        return entity.toDomain(emptyList())
    }

    suspend fun update(
        id: String,
        title: String,
        content: String?,
        templateId: String?,
        priority: Int
    ) {
        val existing = noteDao.getById(id) ?: return
        noteDao.update(existing.copy(
            title = title,
            content = content,
            templateId = templateId,
            priority = priority,
            updatedAt = TimeUtil.nowMillis()
        ))
    }

    suspend fun delete(id: String) {
        noteDao.softDelete(id)
    }

    suspend fun updateAiResult(id: String, category: String, tags: List<String>) {
        noteDao.updateAiResult(
            id = id,
            category = category,
            tags = json.encodeToString(tags),
            timestamp = System.currentTimeMillis()
        )
    }

    // Attachments

    fun observeAttachments(noteId: String): Flow<List<InspirationAttachment>> =
        attachmentDao.observeByNoteId(noteId).map { list -> list.map { it.toDomain() } }

    suspend fun addAttachment(
        noteId: String,
        type: String,
        filePath: String?,
        url: String?,
        metadata: Map<String, String>? = null
    ): InspirationAttachment {
        val entity = InspirationAttachmentEntity(
            id = IdGenerator.generate(),
            noteId = noteId,
            type = type,
            filePath = filePath,
            url = url,
            metadata = metadata?.let { json.encodeToString(it) },
            createdAt = TimeUtil.nowMillis()
        )
        attachmentDao.insert(entity)
        return entity.toDomain()
    }

    suspend fun deleteAttachment(id: String) {
        attachmentDao.delete(id)
    }

    // Templates

    fun observeTemplates(): Flow<List<InspirationTemplate>> =
        templateDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getTemplateById(id: String): InspirationTemplate? =
        templateDao.getById(id)?.toDomain()

    suspend fun createTemplate(
        name: String,
        icon: String?,
        structure: TemplateStructure,
        systemPrompt: String?
    ): InspirationTemplate {
        val entity = InspirationTemplateEntity(
            id = IdGenerator.generate(),
            name = name,
            icon = icon,
            structure = json.encodeToString(structure),
            systemPrompt = systemPrompt,
            isBuiltin = false
        )
        templateDao.insert(entity)
        return entity.toDomain()
    }

    suspend fun initBuiltinTemplates() {
        val existing = templateDao.getAll()
        if (existing.isNotEmpty()) return

        val builtins = listOf(
            InspirationTemplateEntity(
                id = "tpl_quick",
                name = "快速记录",
                icon = "⚡",
                structure = json.encodeToString(TemplateStructure(listOf(
                    TemplateField("content", "text", "内容", "记下你的想法...")
                ))),
                isBuiltin = true,
                sortOrder = 0
            ),
            InspirationTemplateEntity(
                id = "tpl_idea",
                name = "灵感火花",
                icon = "💡",
                structure = json.encodeToString(TemplateStructure(listOf(
                    TemplateField("idea", "text", "灵感", "你的灵感是..."),
                    TemplateField("context", "text", "背景", "这个灵感的背景..."),
                    TemplateField("action", "text", "下一步", "可以做什么...")
                ))),
                systemPrompt = "你是一个灵感整理助手。请分析这个灵感，给出分类建议和相关标签。返回JSON格式：{\"category\":\"分类\",\"tags\":[\"标签1\",\"标签2\"]}",
                isBuiltin = true,
                sortOrder = 1
            ),
            InspirationTemplateEntity(
                id = "tpl_note",
                name = "读书笔记",
                icon = "📚",
                structure = json.encodeToString(TemplateStructure(listOf(
                    TemplateField("book", "text", "书名", "书名..."),
                    TemplateField("author", "text", "作者", "作者..."),
                    TemplateField("quote", "text", "摘录", "精彩段落..."),
                    TemplateField("thought", "text", "感想", "你的想法...")
                ))),
                systemPrompt = "你是一个读书笔记助手。请分析这段读书笔记，提取关键主题和标签。返回JSON：{\"category\":\"分类\",\"tags\":[\"标签\"]}",
                isBuiltin = true,
                sortOrder = 2
            ),
            InspirationTemplateEntity(
                id = "tpl_link",
                name = "链接收藏",
                icon = "🔗",
                structure = json.encodeToString(TemplateStructure(listOf(
                    TemplateField("url", "text", "链接", "https://..."),
                    TemplateField("title", "text", "标题", "页面标题..."),
                    TemplateField("summary", "text", "摘要", "页面主要内容...")
                ))),
                systemPrompt = "你是一个链接分类助手。请分析这个链接的内容，给出分类和标签。返回JSON：{\"category\":\"分类\",\"tags\":[\"标签\"]}",
                isBuiltin = true,
                sortOrder = 3
            )
        )
        builtins.forEach { templateDao.insert(it) }
    }

    // Mapping

    private fun InspirationNoteEntity.toDomain(attachments: List<InspirationAttachment>) = InspirationNote(
        id = id,
        title = title,
        content = content,
        templateId = templateId,
        category = category,
        tags = tags?.let { try { json.decodeFromString(it) } catch (e: Exception) { emptyList() } } ?: emptyList(),
        priority = priority,
        aiProcessed = aiProcessed,
        attachments = attachments,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun InspirationAttachmentEntity.toDomain() = InspirationAttachment(
        id = id,
        noteId = noteId,
        type = type,
        filePath = filePath,
        url = url,
        metadata = metadata?.let { try { json.decodeFromString(it) } catch (e: Exception) { null } },
        createdAt = createdAt
    )

    private fun InspirationTemplateEntity.toDomain() = InspirationTemplate(
        id = id,
        name = name,
        icon = icon,
        structure = try { json.decodeFromString(structure) } catch (e: Exception) { TemplateStructure(emptyList()) },
        systemPrompt = systemPrompt,
        isBuiltin = isBuiltin
    )

    private suspend fun InspirationTemplateDao.getAll(): List<InspirationTemplateEntity> {
        var result: List<InspirationTemplateEntity> = emptyList()
        observeAll().collect { result = it }
        return result
    }
}
