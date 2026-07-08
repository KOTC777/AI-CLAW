package com.toolbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.toolbox.core.database.entity.InspirationAttachmentEntity
import com.toolbox.core.database.entity.InspirationNoteEntity
import com.toolbox.core.database.entity.InspirationTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspirationNoteDao {

    @Query("SELECT * FROM inspiration_note WHERE deleted_at IS NULL ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<InspirationNoteEntity>>

    @Query("SELECT * FROM inspiration_note WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<InspirationNoteEntity?>

    @Query("SELECT * FROM inspiration_note WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): InspirationNoteEntity?

    @Query("SELECT * FROM inspiration_note WHERE deleted_at IS NULL AND category = :category ORDER BY updated_at DESC")
    fun observeByCategory(category: String): Flow<List<InspirationNoteEntity>>

    @Query("SELECT * FROM inspiration_note WHERE deleted_at IS NULL AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY updated_at DESC")
    fun search(query: String): Flow<List<InspirationNoteEntity>>

    @Query("SELECT * FROM inspiration_note WHERE deleted_at IS NULL AND ai_processed = 0")
    suspend fun getUnprocessed(): List<InspirationNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InspirationNoteEntity)

    @Update
    suspend fun update(entity: InspirationNoteEntity)

    @Query("UPDATE inspiration_note SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE inspiration_note SET ai_processed = 1, ai_last_run = :timestamp, category = :category, tags = :tags WHERE id = :id")
    suspend fun updateAiResult(id: String, category: String, tags: String, timestamp: Long)
}

@Dao
interface InspirationAttachmentDao {

    @Query("SELECT * FROM inspiration_attachment WHERE note_id = :noteId ORDER BY created_at ASC")
    fun observeByNoteId(noteId: String): Flow<List<InspirationAttachmentEntity>>

    @Query("SELECT * FROM inspiration_attachment WHERE note_id = :noteId")
    suspend fun getByNoteId(noteId: String): List<InspirationAttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InspirationAttachmentEntity)

    @Query("DELETE FROM inspiration_attachment WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM inspiration_attachment WHERE note_id = :noteId")
    suspend fun deleteByNoteId(noteId: String)
}

@Dao
interface InspirationTemplateDao {

    @Query("SELECT * FROM inspiration_template ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<InspirationTemplateEntity>>

    @Query("SELECT * FROM inspiration_template WHERE id = :id")
    suspend fun getById(id: String): InspirationTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InspirationTemplateEntity)

    @Update
    suspend fun update(entity: InspirationTemplateEntity)

    @Query("DELETE FROM inspiration_template WHERE id = :id AND is_builtin = 0")
    suspend fun delete(id: String)
}
