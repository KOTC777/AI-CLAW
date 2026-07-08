package com.toolbox.feature.checkin.data

import com.toolbox.core.common.util.IdGenerator
import com.toolbox.core.common.util.TimeUtil
import com.toolbox.core.database.dao.CheckinRecordDao
import com.toolbox.core.database.dao.CheckinTaskDao
import com.toolbox.core.database.entity.CheckinRecordEntity
import com.toolbox.core.database.entity.CheckinTaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Domain models

data class CheckinTask(
    val id: String,
    val title: String,
    val description: String?,
    val groupId: String?,
    val repeatRule: RepeatRule?,
    val deadlineTime: String?,          // HH:mm
    val intensityConfig: IntensityConfig,
    val proofType: String,              // photo|audio|text|screenshot
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCheckins: Int,
    val createdAt: Long,
    val archived: Boolean
)

data class CheckinRecord(
    val id: String,
    val taskId: String,
    val checkinTime: Long,
    val proofType: String,
    val proofData: String?,
    val aiVerified: Boolean?,
    val aiFeedback: String?,
    val status: String
)

@Serializable
data class RepeatRule(
    val type: String,                   // none|daily|weekly|weekdays
    val daysOfWeek: List<Int> = emptyList()
)

@Serializable
data class IntensityConfig(
    val levels: List<IntensityLevel> = listOf(
        IntensityLevel(120, 1),   // 2h before: L1
        IntensityLevel(30, 2),    // 30m before: L2
        IntensityLevel(10, 3),    // 10m before: L3
        IntensityLevel(0, 4),     // At deadline: L4
        IntensityLevel(-30, 5)    // 30m after: L5
    )
)

@Serializable
data class IntensityLevel(
    val minutesBefore: Int,         // negative = after deadline
    val intensity: Int              // 1-5
)

// Repository

class CheckinRepository @Inject constructor(
    private val taskDao: CheckinTaskDao,
    private val recordDao: CheckinRecordDao
) {

    private val json = Json { ignoreUnknownKeys = true }

    // Tasks

    fun observeActiveTasks(): Flow<List<CheckinTask>> =
        taskDao.observeActive().map { list -> list.map { it.toDomain() } }

    fun observeTaskById(id: String): Flow<CheckinTask?> =
        taskDao.observeById(id).map { it?.toDomain() }

    suspend fun getTaskById(id: String): CheckinTask? =
        taskDao.getById(id)?.toDomain()

    suspend fun createTask(
        title: String,
        description: String?,
        groupId: String?,
        repeatRule: RepeatRule?,
        deadlineTime: String?,
        intensityConfig: IntensityConfig,
        proofType: String
    ): CheckinTask {
        val now = TimeUtil.nowMillis()
        val entity = CheckinTaskEntity(
            id = IdGenerator.generate(),
            title = title,
            description = description,
            groupId = groupId,
            repeatRule = repeatRule?.let { json.encodeToString(it) },
            deadlineTime = deadlineTime,
            intensityConfig = json.encodeToString(intensityConfig),
            proofType = proofType,
            createdAt = now
        )
        taskDao.insert(entity)
        return entity.toDomain()
    }

    suspend fun updateTask(task: CheckinTask) {
        val entity = task.toEntity()
        taskDao.update(entity)
    }

    suspend fun archiveTask(id: String) {
        taskDao.archive(id)
    }

    // Records

    fun observeRecordsByTaskId(taskId: String): Flow<List<CheckinRecord>> =
        recordDao.observeByTaskId(taskId).map { list -> list.map { it.toDomain() } }

    suspend fun checkin(
        taskId: String,
        proofType: String,
        proofData: String?
    ): CheckinRecord {
        val now = TimeUtil.nowMillis()
        val task = taskDao.getById(taskId)

        val entity = CheckinRecordEntity(
            id = IdGenerator.generate(),
            taskId = taskId,
            checkinTime = now,
            proofType = proofType,
            proofData = proofData,
            aiVerified = null,
            aiFeedback = null,
            status = if (proofType == "text") "success" else "pending"
        )
        recordDao.insert(entity)

        // Update streak
        task?.let {
            val newStreak = it.currentStreak + 1
            taskDao.updateStats(taskId, newStreak)
        }

        return entity.toDomain()
    }

    suspend fun updateRecord(record: CheckinRecord) {
        val entity = CheckinRecordEntity(
            id = record.id,
            taskId = record.taskId,
            checkinTime = record.checkinTime,
            proofType = record.proofType,
            proofData = record.proofData,
            aiVerified = record.aiVerified,
            aiFeedback = record.aiFeedback,
            status = record.status
        )
        recordDao.update(entity)
    }

    suspend fun hasCheckedInToday(taskId: String): Boolean {
        val todayStart = TimeUtil.startOfDay(System.currentTimeMillis())
        val todayEnd = TimeUtil.endOfDay(System.currentTimeMillis())
        val records = recordDao.getByTaskAndDateRange(taskId, todayStart, todayEnd)
        return records.any { it.status == "success" }
    }

    suspend fun getStreak(taskId: String): Int {
        return taskDao.getById(taskId)?.currentStreak ?: 0
    }

    // Mapping

    private fun CheckinTaskEntity.toDomain() = CheckinTask(
        id = id,
        title = title,
        description = description,
        groupId = groupId,
        repeatRule = repeatRule?.let { try { json.decodeFromString(it) } catch (e: Exception) { null } },
        deadlineTime = deadlineTime,
        intensityConfig = try { json.decodeFromString(intensityConfig) } catch (e: Exception) { IntensityConfig() },
        proofType = proofType,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        totalCheckins = totalCheckins,
        createdAt = createdAt,
        archived = archived
    )

    private fun CheckinTask.toEntity() = CheckinTaskEntity(
        id = id,
        title = title,
        description = description,
        groupId = groupId,
        repeatRule = repeatRule?.let { json.encodeToString(it) },
        deadlineTime = deadlineTime,
        intensityConfig = json.encodeToString(intensityConfig),
        proofType = proofType,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        totalCheckins = totalCheckins,
        createdAt = createdAt,
        archived = archived
    )

    private fun CheckinRecordEntity.toDomain() = CheckinRecord(
        id = id,
        taskId = taskId,
        checkinTime = checkinTime,
        proofType = proofType,
        proofData = proofData,
        aiVerified = aiVerified,
        aiFeedback = aiFeedback,
        status = status
    )
}
