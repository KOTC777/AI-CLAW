package com.toolbox.feature.schedule.data

import com.toolbox.core.common.util.IdGenerator
import com.toolbox.core.common.util.TimeUtil
import com.toolbox.core.database.dao.ScheduleEventDao
import com.toolbox.core.database.entity.ScheduleEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Domain models

data class ScheduleEvent(
    val id: String,
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val repeatRule: RepeatRule?,
    val reminderConfig: ReminderConfig?,
    val linkedTaskId: String?,
    val color: Int?,
    val createdAt: Long
)

@Serializable
data class RepeatRule(
    val type: String,           // none|daily|weekly|monthly|yearly
    val interval: Int = 1,      // every N days/weeks/months/years
    val daysOfWeek: List<Int> = emptyList(), // 1=Mon..7=Sun (for weekly)
    val endDate: Long? = null
)

@Serializable
data class ReminderConfig(
    val enabled: Boolean = true,
    val minutesBefore: List<Int> = listOf(15),  // remind N minutes before
    val intensityLevel: Int = 1                  // 1-5
)

// Repository

class ScheduleRepository @Inject constructor(
    private val scheduleEventDao: ScheduleEventDao
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun observeAll(): Flow<List<ScheduleEvent>> =
        scheduleEventDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeByDateRange(start: Long, end: Long): Flow<List<ScheduleEvent>> =
        scheduleEventDao.observeByDateRange(start, end).map { list -> list.map { it.toDomain() } }

    fun observeById(id: String): Flow<ScheduleEvent?> =
        scheduleEventDao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: String): ScheduleEvent? =
        scheduleEventDao.getById(id)?.toDomain()

    suspend fun getUpcoming(limit: Int = 10): List<ScheduleEvent> =
        scheduleEventDao.getUpcoming(System.currentTimeMillis(), limit).map { it.toDomain() }

    suspend fun create(
        title: String,
        description: String?,
        startTime: Long,
        endTime: Long,
        allDay: Boolean,
        repeatRule: RepeatRule?,
        reminderConfig: ReminderConfig?,
        linkedTaskId: String?,
        color: Int?
    ): ScheduleEvent {
        val now = TimeUtil.nowMillis()
        val entity = ScheduleEventEntity(
            id = IdGenerator.generate(),
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            allDay = allDay,
            repeatRule = repeatRule?.let { json.encodeToString(it) },
            reminderConfig = reminderConfig?.let { json.encodeToString(it) },
            linkedTaskId = linkedTaskId,
            color = color,
            createdAt = now
        )
        scheduleEventDao.insert(entity)
        return entity.toDomain()
    }

    suspend fun update(
        id: String,
        title: String,
        description: String?,
        startTime: Long,
        endTime: Long,
        allDay: Boolean,
        repeatRule: RepeatRule?,
        reminderConfig: ReminderConfig?,
        color: Int?
    ) {
        val existing = scheduleEventDao.getById(id) ?: return
        val updated = existing.copy(
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            allDay = allDay,
            repeatRule = repeatRule?.let { json.encodeToString(it) },
            reminderConfig = reminderConfig?.let { json.encodeToString(it) },
            color = color
        )
        scheduleEventDao.update(updated)
    }

    suspend fun delete(id: String) {
        scheduleEventDao.softDelete(id)
    }

    private fun ScheduleEventEntity.toDomain(): ScheduleEvent {
        val parsedRepeatRule = repeatRule?.let {
            try { json.decodeFromString<RepeatRule>(it) } catch (e: Exception) { null }
        }
        val parsedReminderConfig = reminderConfig?.let {
            try { json.decodeFromString<ReminderConfig>(it) } catch (e: Exception) { null }
        }
        return ScheduleEvent(
            id = id,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            allDay = allDay,
            repeatRule = parsedRepeatRule,
            reminderConfig = parsedReminderConfig,
            linkedTaskId = linkedTaskId,
            color = color,
            createdAt = createdAt
        )
    }
}
