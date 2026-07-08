package com.toolbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.toolbox.core.database.entity.CheckinRecordEntity
import com.toolbox.core.database.entity.CheckinTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckinTaskDao {

    @Query("SELECT * FROM checkin_task WHERE archived = 0 ORDER BY created_at DESC")
    fun observeActive(): Flow<List<CheckinTaskEntity>>

    @Query("SELECT * FROM checkin_task WHERE id = :id")
    fun observeById(id: String): Flow<CheckinTaskEntity?>

    @Query("SELECT * FROM checkin_task WHERE id = :id")
    suspend fun getById(id: String): CheckinTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CheckinTaskEntity)

    @Update
    suspend fun update(entity: CheckinTaskEntity)

    @Query("UPDATE checkin_task SET archived = 1 WHERE id = :id")
    suspend fun archive(id: String)

    @Query("UPDATE checkin_task SET current_streak = :streak, best_streak = MAX(best_streak, :streak), total_checkins = total_checkins + 1 WHERE id = :id")
    suspend fun updateStats(id: String, streak: Int)
}

@Dao
interface CheckinRecordDao {

    @Query("SELECT * FROM checkin_record WHERE task_id = :taskId ORDER BY checkin_time DESC")
    fun observeByTaskId(taskId: String): Flow<List<CheckinRecordEntity>>

    @Query("SELECT * FROM checkin_record WHERE task_id = :taskId AND checkin_time BETWEEN :start AND :end ORDER BY checkin_time DESC")
    suspend fun getByTaskAndDateRange(taskId: String, start: Long, end: Long): List<CheckinRecordEntity>

    @Query("SELECT * FROM checkin_record WHERE task_id = :taskId ORDER BY checkin_time DESC LIMIT 1")
    suspend fun getLatestByTaskId(taskId: String): CheckinRecordEntity?

    @Query("SELECT * FROM checkin_record WHERE status = 'pending' AND checkin_time < :before")
    suspend fun getPendingBefore(before: Long): List<CheckinRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CheckinRecordEntity)

    @Update
    suspend fun update(entity: CheckinRecordEntity)
}
