package com.toolbox.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_event")
data class ScheduleEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    @ColumnInfo(name = "all_day") val allDay: Boolean = false,
    @ColumnInfo(name = "repeat_rule") val repeatRule: String? = null,    // JSON
    @ColumnInfo(name = "reminder_config") val reminderConfig: String? = null, // JSON
    @ColumnInfo(name = "linked_task_id") val linkedTaskId: String? = null,
    val color: Int? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null
)
