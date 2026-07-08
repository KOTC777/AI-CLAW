package com.toolbox.core.security.vault

import com.toolbox.core.security.crypto.CryptoEngine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Handles export/import of encrypted vault files (.vault).
 */
class VaultExporter(
    private val cryptoEngine: CryptoEngine
) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /**
     * Export data to an encrypted vault file.
     */
    fun export(
        data: VaultData,
        exportKey: ByteArray,
        outputStream: OutputStream
    ) {
        val jsonBytes = json.encodeToString(data).toByteArray(Charsets.UTF_8)
        val encrypted = cryptoEngine.encrypt(jsonBytes, exportKey)

        // Write: [16 bytes salt][12 bytes IV][encrypted data]
        val header = VaultHeader(
            version = VAULT_VERSION,
            iv = encrypted.iv
        )

        outputStream.use { out ->
            out.write(header.version.toByteArray(Charsets.UTF_8).let {
                it.copyOf(HEADER_SIZE).also { padded ->
                    it.copyInto(padded)
                }
            })
            out.write(encrypted.iv)
            out.write(encrypted.ciphertext)
        }
    }

    /**
     * Import data from an encrypted vault file.
     */
    fun import(
        exportKey: ByteArray,
        inputStream: InputStream
    ): VaultData {
        inputStream.use { input ->
            val headerBytes = ByteArray(HEADER_SIZE)
            input.read(headerBytes)

            val iv = ByteArray(CryptoEngine.IV_LENGTH)
            input.read(iv)

            val ciphertext = input.readBytes()

            val decrypted = cryptoEngine.decrypt(ciphertext, exportKey, iv)
            return json.decodeFromString(String(decrypted, Charsets.UTF_8))
        }
    }

    companion object {
        private const val VAULT_VERSION = 1
        private const val HEADER_SIZE = 16
    }
}

@Serializable
data class VaultData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val modules: List<String> = emptyList(),  // which modules are included
    val memos: List<VaultMemo> = emptyList(),
    val passwordGroups: List<VaultPasswordGroup> = emptyList(),
    val passwordEntries: List<VaultPasswordEntry> = emptyList(),
    val scheduleEvents: List<VaultScheduleEvent> = emptyList(),
    val checkinTasks: List<VaultCheckinTask> = emptyList(),
    val inspirationNotes: List<VaultInspirationNote> = emptyList()
)

@Serializable
data class VaultMemo(
    val id: String,
    val title: String,
    val content: String,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class VaultPasswordGroup(
    val id: String,
    val name: String,
    val icon: String?,
    val color: Int?,
    val sortOrder: Int
)

@Serializable
data class VaultPasswordEntry(
    val id: String,
    val title: String,
    val groupId: String?,
    val icon: String?,
    val passwordEncrypted: String,  // Base64
    val passwordIv: String,
    val passwordTag: String,
    val hintsEncrypted: String,
    val hintsIv: String,
    val hintsTag: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class VaultScheduleEvent(
    val id: String,
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val repeatRule: String?,
    val reminderConfig: String?,
    val color: Int?,
    val createdAt: Long
)

@Serializable
data class VaultCheckinTask(
    val id: String,
    val title: String,
    val description: String?,
    val repeatRule: String?,
    val deadlineTime: String?,
    val intensityConfig: String,
    val proofType: String,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCheckins: Int,
    val createdAt: Long
)

@Serializable
data class VaultInspirationNote(
    val id: String,
    val title: String,
    val content: String?,
    val templateId: String?,
    val category: String?,
    val tags: String?,
    val priority: Int,
    val createdAt: Long,
    val updatedAt: Long
)
