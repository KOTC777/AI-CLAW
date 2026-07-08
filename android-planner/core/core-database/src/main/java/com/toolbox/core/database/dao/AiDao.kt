package com.toolbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.toolbox.core.database.entity.AiProcessingLogEntity
import com.toolbox.core.database.entity.AiProviderConfigEntity
import com.toolbox.core.database.entity.AiScheduledTaskEntity
import com.toolbox.core.database.entity.AppLockConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiProcessingLogDao {

    @Query("SELECT * FROM ai_processing_log ORDER BY created_at DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<AiProcessingLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AiProcessingLogEntity)
}

@Dao
interface AiProviderConfigDao {

    @Query("SELECT * FROM ai_provider_config ORDER BY is_default DESC, name ASC")
    fun observeAll(): Flow<List<AiProviderConfigEntity>>

    @Query("SELECT * FROM ai_provider_config WHERE id = :id")
    suspend fun getById(id: String): AiProviderConfigEntity?

    @Query("SELECT * FROM ai_provider_config WHERE is_default = 1 LIMIT 1")
    suspend fun getDefault(): AiProviderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AiProviderConfigEntity)

    @Update
    suspend fun update(entity: AiProviderConfigEntity)

    @Query("DELETE FROM ai_provider_config WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE ai_provider_config SET is_default = 0")
    suspend fun clearDefaults()

    @Query("UPDATE ai_provider_config SET is_default = 1 WHERE id = :id")
    suspend fun setDefault(id: String)
}

@Dao
interface AiScheduledTaskDao {

    @Query("SELECT * FROM ai_scheduled_task ORDER BY created_at DESC")
    fun observeAll(): Flow<List<AiScheduledTaskEntity>>

    @Query("SELECT * FROM ai_scheduled_task WHERE enabled = 1")
    suspend fun getEnabled(): List<AiScheduledTaskEntity>

    @Query("SELECT * FROM ai_scheduled_task WHERE id = :id")
    suspend fun getById(id: String): AiScheduledTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AiScheduledTaskEntity)

    @Update
    suspend fun update(entity: AiScheduledTaskEntity)

    @Query("DELETE FROM ai_scheduled_task WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface AppLockConfigDao {

    @Query("SELECT * FROM app_lock_config WHERE enabled = 1")
    fun observeAll(): Flow<List<AppLockConfigEntity>>

    @Query("SELECT * FROM app_lock_config WHERE lock_type = 'app' AND enabled = 1 LIMIT 1")
    suspend fun getAppLock(): AppLockConfigEntity?

    @Query("SELECT * FROM app_lock_config WHERE lock_type = 'feature' AND feature_id = :featureId AND enabled = 1 LIMIT 1")
    suspend fun getFeatureLock(featureId: String): AppLockConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AppLockConfigEntity)

    @Update
    suspend fun update(entity: AppLockConfigEntity)

    @Query("DELETE FROM app_lock_config WHERE id = :id")
    suspend fun delete(id: String)
}
