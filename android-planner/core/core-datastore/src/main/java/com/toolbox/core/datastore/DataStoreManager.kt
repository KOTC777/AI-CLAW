package com.toolbox.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "toolbox_prefs")

/**
 * DataStore manager for app-wide preferences.
 */
class DataStoreManager(private val context: Context) {

    // Keys
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")              // system|light|dark
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val CUSTOM_PRIMARY_COLOR = stringPreferencesKey("custom_primary_color")
        val BACKGROUND_IMAGE_PATH = stringPreferencesKey("background_image_path")
        val MASTER_PASSWORD_HASH = stringPreferencesKey("master_password_hash")
        val MASTER_PASSWORD_SALT = stringPreferencesKey("master_password_salt")
        val DB_PASSPHRASE_ENCRYPTED = stringPreferencesKey("db_passphrase_encrypted")
        val DB_PASSPHRASE_IV = stringPreferencesKey("db_passphrase_iv")
        val IS_SETUP_COMPLETE = booleanPreferencesKey("is_setup_complete")
        val LAST_ACTIVE_TIME = longPreferencesKey("last_active_time")
        val SESSION_TIMEOUT_MINUTES = stringPreferencesKey("session_timeout_minutes")
    }

    // Theme
    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: true }
    val customPrimaryColor: Flow<String?> = context.dataStore.data.map { it[Keys.CUSTOM_PRIMARY_COLOR] }
    val backgroundImage: Flow<String?> = context.dataStore.data.map { it[Keys.BACKGROUND_IMAGE_PATH] }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setCustomPrimaryColor(colorHex: String?) {
        context.dataStore.edit {
            if (colorHex != null) it[Keys.CUSTOM_PRIMARY_COLOR] = colorHex
            else it.remove(Keys.CUSTOM_PRIMARY_COLOR)
        }
    }

    suspend fun setBackgroundImage(path: String?) {
        context.dataStore.edit {
            if (path != null) it[Keys.BACKGROUND_IMAGE_PATH] = path
            else it.remove(Keys.BACKGROUND_IMAGE_PATH)
        }
    }

    // Security
    val isSetupComplete: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_SETUP_COMPLETE] ?: false }
    val lastActiveTime: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_ACTIVE_TIME] ?: 0L }

    suspend fun setSetupComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.IS_SETUP_COMPLETE] = complete }
    }

    suspend fun updateLastActiveTime() {
        context.dataStore.edit { it[Keys.LAST_ACTIVE_TIME] = System.currentTimeMillis() }
    }

    // Encrypted DB Passphrase storage
    suspend fun saveEncryptedDbPassphrase(encrypted: ByteArray, iv: ByteArray) {
        context.dataStore.edit {
            it[Keys.DB_PASSPHRASE_ENCRYPTED] = java.util.Base64.getEncoder().encodeToString(encrypted)
            it[Keys.DB_PASSPHRASE_IV] = java.util.Base64.getEncoder().encodeToString(iv)
        }
    }

    suspend fun getEncryptedDbPassphrase(): Pair<ByteArray, ByteArray>? {
        val data = context.dataStore.data.first()
        val enc = data[Keys.DB_PASSPHRASE_ENCRYPTED] ?: return null
        val iv = data[Keys.DB_PASSPHRASE_IV] ?: return null
        return Pair(
            java.util.Base64.getDecoder().decode(enc),
            java.util.Base64.getDecoder().decode(iv)
        )
    }

    // Master password hash/salt (for verification, not for encryption)
    suspend fun saveMasterPasswordHash(hash: String, salt: String) {
        context.dataStore.edit {
            it[Keys.MASTER_PASSWORD_HASH] = hash
            it[Keys.MASTER_PASSWORD_SALT] = salt
        }
    }

    suspend fun getMasterPasswordHash(): Pair<String, String>? {
        val data = context.dataStore.data.first()
        val hash = data[Keys.MASTER_PASSWORD_HASH] ?: return null
        val salt = data[Keys.MASTER_PASSWORD_SALT] ?: return null
        return Pair(hash, salt)
    }
}
