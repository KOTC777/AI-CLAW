package com.toolbox.core.security.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * Sync adapter interface for future cloud synchronization.
 * Currently a placeholder; implement with WebDAV/自建服务器/etc.
 */
interface SyncAdapter {
    /**
     * Check if sync is configured and available.
     */
    suspend fun isAvailable(): Boolean

    /**
     * Get current sync status.
     */
    fun observeStatus(): Flow<SyncStatus>

    /**
     * Push local changes to remote.
     */
    suspend fun push(data: SyncData): SyncResult

    /**
     * Pull remote changes to local.
     */
    suspend fun pull(): SyncResult

    /**
     * Full sync (push + pull + merge).
     */
    suspend fun sync(): SyncResult

    /**
     * Configure sync settings.
     */
    suspend fun configure(config: SyncConfig)
}

@Serializable
data class SyncConfig(
    val provider: String,           // webdav|custom|none
    val endpoint: String? = null,
    val credentials: String? = null, // encrypted
    val autoSync: Boolean = false,
    val syncIntervalMinutes: Int = 30
)

@Serializable
data class SyncData(
    val version: Long,
    val modules: List<String>,
    val payload: String             // encrypted JSON
)

sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object Syncing : SyncStatus()
    data class Success(val timestamp: Long) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
    data object NotConfigured : SyncStatus()
}

sealed class SyncResult {
    data object Success : SyncResult()
    data class Conflict(val conflicts: List<SyncConflict>) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

data class SyncConflict(
    val itemId: String,
    val module: String,
    val localVersion: Long,
    val remoteVersion: Long
)

/**
 * No-op implementation for when sync is not configured.
 */
class NoOpSyncAdapter @Inject constructor() : SyncAdapter {
    override suspend fun isAvailable(): Boolean = false
    override fun observeStatus(): Flow<SyncStatus> = kotlinx.coroutines.flow.flowOf(SyncStatus.NotConfigured)
    override suspend fun push(data: SyncData): SyncResult = SyncResult.Error("同步未配置")
    override suspend fun pull(): SyncResult = SyncResult.Error("同步未配置")
    override suspend fun sync(): SyncResult = SyncResult.Error("同步未配置")
    override suspend fun configure(config: SyncConfig) {}
}
