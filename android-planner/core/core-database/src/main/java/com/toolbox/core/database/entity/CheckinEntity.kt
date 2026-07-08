package com.toolbox.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkin_task",
    indices = [Index(value = ["group_id"])]
)
data class CheckinTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "group_id") val groupId: String? = null,
    @ColumnInfo(name = "repeat_rule") val repeatRule: String? = null,       // JSON
    @ColumnInfo(name = "deadline_time") val deadlineTime: String? = null,   // HH:mm
    @ColumnInfo(name = "intensity_config") val intensityConfig: String,     // JSON: multi-level config
    @ColumnInfo(name = "proof_type") val proofType: String,                 // photo|audio|text|screenshot
    @ColumnInfo(name = "proof_config") val proofConfig: String? = null,     // JSON
    @ColumnInfo(name = "current_streak") val currentStreak: Int = 0,
    @ColumnInfo(name = "best_streak") val bestStreak: Int = 0,
    @ColumnInfo(name = "total_checkins") val totalCheckins: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "archived") val archived: Boolean = false
)

@Entity(
    tableName = "checkin_record",
    foreignKeys = [
        ForeignKey(
            entity = CheckinTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["task_id"])]
)
data class CheckinRecordEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "checkin_time") val checkinTime: Long,
    @ColumnInfo(name = "proof_type") val proofType: String,
    @ColumnInfo(name = "proof_data") val proofData: String? = null,     // file path or text
    @ColumnInfo(name = "ai_verified") val aiVerified: Boolean? = null,
    @ColumnInfo(name = "ai_feedback") val aiFeedback: String? = null,
    val status: String                                                  // success|pending|failed
)
