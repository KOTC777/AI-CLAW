package com.toolbox.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.toolbox.core.database.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    @Query("SELECT * FROM memo WHERE deleted_at IS NULL ORDER BY pinned DESC, updated_at DESC")
    fun observeAll(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memo WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<MemoEntity?>

    @Query("SELECT * FROM memo WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): MemoEntity?

    @Query("SELECT * FROM memo WHERE deleted_at IS NULL AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY updated_at DESC")
    fun search(query: String): Flow<List<MemoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MemoEntity)

    @Update
    suspend fun update(entity: MemoEntity)

    @Query("UPDATE memo SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE memo SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)
}
