package com.toolbox.feature.memo.data

import com.toolbox.core.common.util.IdGenerator
import com.toolbox.core.common.util.TimeUtil
import com.toolbox.core.database.dao.MemoDao
import com.toolbox.core.database.entity.MemoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Domain model for Memo.
 */
data class Memo(
    val id: String,
    val title: String,
    val content: String,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Repository for Memo operations.
 */
class MemoRepository @Inject constructor(
    private val memoDao: MemoDao
) {

    fun observeAll(): Flow<List<Memo>> =
        memoDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeById(id: String): Flow<Memo?> =
        memoDao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: String): Memo? =
        memoDao.getById(id)?.toDomain()

    fun search(query: String): Flow<List<Memo>> =
        memoDao.search(query).map { list -> list.map { it.toDomain() } }

    suspend fun create(title: String, content: String): Memo {
        val now = TimeUtil.nowMillis()
        val entity = MemoEntity(
            id = IdGenerator.generate(),
            title = title,
            content = content,
            pinned = false,
            createdAt = now,
            updatedAt = now
        )
        memoDao.insert(entity)
        return entity.toDomain()
    }

    suspend fun update(id: String, title: String, content: String) {
        val existing = memoDao.getById(id) ?: return
        val updated = existing.copy(
            title = title,
            content = content,
            updatedAt = TimeUtil.nowMillis()
        )
        memoDao.update(updated)
    }

    suspend fun delete(id: String) {
        memoDao.softDelete(id)
    }

    suspend fun togglePin(id: String) {
        val existing = memoDao.getById(id) ?: return
        memoDao.setPinned(id, !existing.pinned)
    }
}

private fun MemoEntity.toDomain() = Memo(
    id = id,
    title = title,
    content = content,
    pinned = pinned,
    createdAt = createdAt,
    updatedAt = updatedAt
)
