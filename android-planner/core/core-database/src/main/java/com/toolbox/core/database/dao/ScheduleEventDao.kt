package com.toolbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.toolbox.core.database.entity.ScheduleEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleEventDao {

    @Query("SELECT * FROM schedule_event WHERE deleted_at IS NULL ORDER BY start_time ASC")
    fun observeAll(): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_event WHERE deleted_at IS NULL AND start_time BETWEEN :start AND :end ORDER BY start_time ASC")
    fun observeByDateRange(start: Long, end: Long): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_event WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<ScheduleEventEntity?>

    @Query("SELECT * FROM schedule_event WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): ScheduleEventEntity?

    @Query("SELECT * FROM schedule_event WHERE deleted_at IS NULL AND linked_task_id = :taskId")
    suspend fun getByLinkedTaskId(taskId: String): ScheduleEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduleEventEntity)

    @Update
    suspend fun update(entity: ScheduleEventEntity)

    @Query("UPDATE schedule_event SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM schedule_event WHERE deleted_at IS NULL AND start_time > :now ORDER BY start_time ASC LIMIT :limit")
    suspend fun getUpcoming(now: Long, limit: Int = 10): List<ScheduleEventEntity>
}
