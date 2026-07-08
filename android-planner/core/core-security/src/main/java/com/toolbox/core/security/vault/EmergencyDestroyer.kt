package com.toolbox.core.security.vault

import android.content.Context
import com.toolbox.core.datastore.DataStoreManager
import com.toolbox.core.security.crypto.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Emergency data destruction manager.
 * Can wipe all app data in emergency situations.
 */
class EmergencyDestroyer @Inject constructor(
    private val context: Context,
    private val securityManager: SecurityManager,
    private val dataStoreManager: DataStoreManager
) {

    /**
     * Wipe all application data.
     * This is irreversible.
     */
    suspend fun destroyAll(): DestroyResult = withContext(Dispatchers.IO) {
        try {
            // 1. Lock security manager (clear cached keys)
            securityManager.lock()

            // 2. Delete database
            context.deleteDatabase("toolbox.db")

            // 3. Clear DataStore
            context.filesDir.resolve("datastore").deleteRecursively()

            // 4. Clear SharedPreferences
            context.getSharedPreferences("toolbox_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply()

            // 5. Clear files (backgrounds, web snapshots, etc.)
            context.filesDir.deleteRecursively()
            context.cacheDir.deleteRecursively()

            // 6. Clear external files
            context.getExternalFilesDir(null)?.deleteRecursively()

            DestroyResult.Success
        } catch (e: Exception) {
            DestroyResult.Error(e.message ?: "销毁失败")
        }
    }

    /**
     * Wipe only sensitive data (passwords, encryption keys).
     * Keeps non-sensitive data like memos and settings.
     */
    suspend fun destroySensitive(): DestroyResult = withContext(Dispatchers.IO) {
        try {
            // 1. Lock security manager
            securityManager.lock()

            // 2. Delete database (contains encrypted passwords)
            context.deleteDatabase("toolbox.db")

            // 3. Clear encryption keys from DataStore
            dataStoreManager.setSetupComplete(false)

            DestroyResult.Success
        } catch (e: Exception) {
            DestroyResult.Error(e.message ?: "销毁失败")
        }
    }

    sealed class DestroyResult {
        data object Success : DestroyResult()
        data class Error(val message: String) : DestroyResult()
    }
}
