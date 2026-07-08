package com.toolbox.feature.password.data

import com.toolbox.core.common.util.IdGenerator
import com.toolbox.core.common.util.TimeUtil
import com.toolbox.core.database.dao.PasswordEntryDao
import com.toolbox.core.database.dao.PasswordGroupDao
import com.toolbox.core.database.entity.PasswordEntryEntity
import com.toolbox.core.database.entity.PasswordGroupEntity
import com.toolbox.core.security.crypto.CryptoEngine
import com.toolbox.core.security.crypto.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Domain models

data class PasswordGroup(
    val id: String,
    val name: String,
    val icon: String?,
    val color: Int?,
    val sortOrder: Int,
    val entryCount: Int = 0
)

data class PasswordEntry(
    val id: String,
    val title: String,
    val groupId: String?,
    val icon: String?,
    val password: String?,          // Level 0: decrypted (only when explicitly requested)
    val hints: List<String>,        // Level 1: decrypted hints
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class PasswordHint(
    val content: String,
    val label: String = ""
)

// Repository

class PasswordRepository @Inject constructor(
    private val entryDao: PasswordEntryDao,
    private val groupDao: PasswordGroupDao,
    private val securityManager: SecurityManager
) {

    private val json = Json { ignoreUnknownKeys = true }

    // ========== Groups ==========

    fun observeGroups(): Flow<List<PasswordGroup>> =
        groupDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getGroupById(id: String): PasswordGroup? =
        groupDao.getById(id)?.toDomain()

    suspend fun createGroup(name: String, icon: String?, color: Int?): PasswordGroup {
        val entity = PasswordGroupEntity(
            id = IdGenerator.generate(),
            name = name,
            icon = icon,
            color = color,
            sortOrder = 0
        )
        groupDao.insert(entity)
        return entity.toDomain()
    }

    suspend fun updateGroup(id: String, name: String, icon: String?, color: Int?) {
        val existing = groupDao.getById(id) ?: return
        groupDao.update(existing.copy(name = name, icon = icon, color = color))
    }

    suspend fun deleteGroup(id: String) {
        groupDao.delete(id)
    }

    // ========== Entries ==========

    fun observeAllEntries(): Flow<List<PasswordEntry>> =
        entryDao.observeAll().map { list ->
            list.map { it.toDomainWithoutPassword() }
        }

    fun observeEntriesByGroup(groupId: String): Flow<List<PasswordEntry>> =
        entryDao.observeByGroupId(groupId).map { list ->
            list.map { it.toDomainWithoutPassword() }
        }

    fun searchEntries(query: String): Flow<List<PasswordEntry>> =
        entryDao.search(query).map { list ->
            list.map { it.toDomainWithoutPassword() }
        }

    suspend fun getEntryById(id: String): PasswordEntry? =
        entryDao.getById(id)?.toDomainWithDecryptedFields()

    /**
     * Get entry with password revealed (Level 0).
     */
    suspend fun getEntryWithPassword(id: String): PasswordEntry? {
        val entity = entryDao.getById(id) ?: return null
        return entity.toDomainWithDecryptedFields()
    }

    suspend fun createEntry(
        title: String,
        groupId: String?,
        icon: String?,
        password: String,
        hints: List<String>
    ): PasswordEntry {
        val now = TimeUtil.nowMillis()
        val fieldKey = securityManager.getFieldKey()

        // Encrypt Level 0: password
        val passwordEncrypted = securityManager.encryptField(password)

        // Encrypt Level 1: hints
        val hintsJson = json.encodeToString(hints)
        val hintsEncrypted = securityManager.encryptField(hintsJson)

        val entity = PasswordEntryEntity(
            id = IdGenerator.generate(),
            title = title,
            groupId = groupId,
            icon = icon,
            passwordEncrypted = passwordEncrypted.ciphertext,
            passwordIv = passwordEncrypted.iv,
            passwordTag = passwordEncrypted.tag,
            hintsEncrypted = hintsEncrypted.ciphertext,
            hintsIv = hintsEncrypted.iv,
            hintsTag = hintsEncrypted.tag,
            createdAt = now,
            updatedAt = now
        )
        entryDao.insert(entity)

        return PasswordEntry(
            id = entity.id,
            title = title,
            groupId = groupId,
            icon = icon,
            password = password,
            hints = hints,
            createdAt = now,
            updatedAt = now
        )
    }

    suspend fun updateEntry(
        id: String,
        title: String,
        groupId: String?,
        icon: String?,
        password: String?,
        hints: List<String>?
    ) {
        val existing = entryDao.getById(id) ?: return
        val now = TimeUtil.nowMillis()

        // Re-encrypt fields that changed
        val passwordEnc = password?.let {
            securityManager.encryptField(it)
        }
        val hintsEnc = hints?.let {
            securityManager.encryptField(json.encodeToString(it))
        }

        val updated = existing.copy(
            title = title,
            groupId = groupId,
            icon = icon,
            passwordEncrypted = passwordEnc?.ciphertext ?: existing.passwordEncrypted,
            passwordIv = passwordEnc?.iv ?: existing.passwordIv,
            passwordTag = passwordEnc?.tag ?: existing.passwordTag,
            hintsEncrypted = hintsEnc?.ciphertext ?: existing.hintsEncrypted,
            hintsIv = hintsEnc?.iv ?: existing.hintsIv,
            hintsTag = hintsEnc?.tag ?: existing.hintsTag,
            updatedAt = now
        )
        entryDao.update(updated)
    }

    suspend fun deleteEntry(id: String) {
        entryDao.softDelete(id)
    }

    suspend fun getEntryCount(): Int = entryDao.count()

    // ========== Mapping ==========

    private fun PasswordGroupEntity.toDomain() = PasswordGroup(
        id = id,
        name = name,
        icon = icon,
        color = color,
        sortOrder = sortOrder
    )

    /**
     * Map entity to domain WITHOUT decrypting sensitive fields.
     * Used for list views where password is not shown.
     */
    private fun PasswordEntryEntity.toDomainWithoutPassword() = PasswordEntry(
        id = id,
        title = title,
        groupId = groupId,
        icon = icon,
        password = null,  // Not decrypted for list view
        hints = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    /**
     * Map entity to domain WITH all fields decrypted.
     * Used for detail view.
     */
    private fun PasswordEntryEntity.toDomainWithDecryptedFields(): PasswordEntry {
        val decryptedPassword = try {
            securityManager.decryptField(passwordEncrypted, passwordIv)
        } catch (e: Exception) {
            "[解密失败]"
        }

        val decryptedHints = try {
            val hintsJson = securityManager.decryptField(hintsEncrypted, hintsIv)
            json.decodeFromString<List<String>>(hintsJson)
        } catch (e: Exception) {
            listOf("[解密失败]")
        }

        return PasswordEntry(
            id = id,
            title = title,
            groupId = groupId,
            icon = icon,
            password = decryptedPassword,
            hints = decryptedHints,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
