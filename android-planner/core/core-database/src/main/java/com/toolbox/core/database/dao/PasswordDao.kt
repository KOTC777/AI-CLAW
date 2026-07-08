package com.toolbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.toolbox.core.database.entity.PasswordEntryEntity
import com.toolbox.core.database.entity.PasswordGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordGroupDao {

    @Query("SELECT * FROM password_group ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<PasswordGroupEntity>>

    @Query("SELECT * FROM password_group WHERE id = :id")
    suspend fun getById(id: String): PasswordGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PasswordGroupEntity)

    @Update
    suspend fun update(entity: PasswordGroupEntity)

    @Query("DELETE FROM password_group WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface PasswordEntryDao {

    @Query("SELECT * FROM password_entry WHERE deleted_at IS NULL ORDER BY title ASC")
    fun observeAll(): Flow<List<PasswordEntryEntity>>

    @Query("SELECT * FROM password_entry WHERE deleted_at IS NULL AND group_id = :groupId ORDER BY title ASC")
    fun observeByGroupId(groupId: String): Flow<List<PasswordEntryEntity>>

    @Query("SELECT * FROM password_entry WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<PasswordEntryEntity?>

    @Query("SELECT * FROM password_entry WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): PasswordEntryEntity?

    @Query("SELECT * FROM password_entry WHERE deleted_at IS NULL AND (title LIKE '%' || :query || '%') ORDER BY title ASC")
    fun search(query: String): Flow<List<PasswordEntryEntity>>

    @Query("SELECT * FROM password_entry WHERE deleted_at IS NULL AND group_id IN (:groupIds) ORDER BY title ASC")
    fun observeByGroupIds(groupIds: List<String>): Flow<List<PasswordEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PasswordEntryEntity)

    @Update
    suspend fun update(entity: PasswordEntryEntity)

    @Query("UPDATE password_entry SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM password_entry WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
