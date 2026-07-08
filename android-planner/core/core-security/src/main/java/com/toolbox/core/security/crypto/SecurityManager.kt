package com.toolbox.core.security.crypto

import com.toolbox.core.datastore.DataStoreManager
import com.toolbox.core.security.keystore.KeystoreManager
import kotlinx.coroutines.flow.first

/**
 * Central security manager that coordinates Keystore, Argon2, and CryptoEngine.
 * Handles master password setup, key derivation, and field-level encryption.
 */
class SecurityManager(
    private val keystoreManager: KeystoreManager,
    private val dataStoreManager: DataStoreManager,
    private val argon2Kdf: Argon2Kdf,
    private val cryptoEngine: CryptoEngine
) {

    private var cachedMasterKey: ByteArray? = null
    private var cachedFieldKey: ByteArray? = null

    /**
     * Check if master password has been set up.
     */
    suspend fun isSetupComplete(): Boolean =
        dataStoreManager.isSetupComplete.first()

    /**
     * Set up master password for the first time.
     * Derives master key, creates DB passphrase, and stores encrypted passphrase.
     */
    suspend fun setupMasterPassword(password: CharArray): ByteArray {
        val salt = argon2Kdf.generateSalt()
        val masterKey = argon2Kdf.deriveKey(password, salt)
        val masterKeyHash = argon2Kdf.deriveKey(password, salt, outputLength = 64)

        // Store hash for verification (not the master key itself)
        dataStoreManager.saveMasterPasswordHash(
            java.util.Base64.getEncoder().encodeToString(masterKeyHash),
            java.util.Base64.getEncoder().encodeToString(salt)
        )

        // Generate and encrypt DB passphrase
        val dbPassphrase = cryptoEngine.generateKey()
        val encryptedPassphrase = cryptoEngine.encrypt(dbPassphrase, masterKey)
        dataStoreManager.saveEncryptedDbPassphrase(
            encryptedPassphrase.ciphertext,
            encryptedPassphrase.iv
        )

        dataStoreManager.setSetupComplete(true)

        // Cache keys
        cachedMasterKey = masterKey
        cachedFieldKey = cryptoEngine.deriveFieldKey(masterKey, "field_encryption")

        return dbPassphrase
    }

    /**
     * Unlock with master password. Returns DB passphrase.
     */
    suspend fun unlock(password: CharArray): ByteArray {
        val stored = dataStoreManager.getMasterPasswordHash()
            ?: throw IllegalStateException("Master password not set up")

        val hash = java.util.Base64.getDecoder().decode(stored.first)
        val salt = java.util.Base64.getDecoder().decode(stored.second)

        val masterKey = argon2Kdf.deriveKey(password, salt)
        val masterKeyHash = argon2Kdf.deriveKey(password, salt, outputLength = 64)

        // Verify password
        if (!masterKeyHash.contentEquals(hash)) {
            throw SecurityException("Invalid master password")
        }

        // Decrypt DB passphrase
        val encryptedData = dataStoreManager.getEncryptedDbPassphrase()
            ?: throw IllegalStateException("DB passphrase not found")

        val dbPassphrase = cryptoEngine.decrypt(encryptedData.first, masterKey, encryptedData.second)

        // Cache keys
        cachedMasterKey = masterKey
        cachedFieldKey = cryptoEngine.deriveFieldKey(masterKey, "field_encryption")

        return dbPassphrase
    }

    /**
     * Get the cached field encryption key (for password entry encryption).
     * Must call unlock() first.
     */
    fun getFieldKey(): ByteArray =
        cachedFieldKey ?: throw IllegalStateException("Not unlocked. Call unlock() first.")

    /**
     * Encrypt a field value.
     */
    fun encryptField(data: String): CryptoEngine.EncryptionResult {
        val key = getFieldKey()
        return cryptoEngine.encrypt(data.toByteArray(Charsets.UTF_8), key)
    }

    /**
     * Decrypt a field value.
     */
    fun decryptField(ciphertext: ByteArray, iv: ByteArray): String {
        val key = getFieldKey()
        val decrypted = cryptoEngine.decrypt(ciphertext, key, iv)
        return String(decrypted, Charsets.UTF_8)
    }

    /**
     * Lock the app (clear cached keys).
     */
    fun lock() {
        cachedMasterKey?.fill(0)
        cachedFieldKey?.fill(0)
        cachedMasterKey = null
        cachedFieldKey = null
    }

    /**
     * Change master password.
     */
    suspend fun changeMasterPassword(
        oldPassword: CharArray,
        newPassword: CharArray,
        currentDbPassphrase: ByteArray
    ): ByteArray {
        // Verify old password
        unlock(oldPassword)

        // Re-derive with new password
        val newSalt = argon2Kdf.generateSalt()
        val newMasterKey = argon2Kdf.deriveKey(newPassword, newSalt)
        val newMasterKeyHash = argon2Kdf.deriveKey(newPassword, newSalt, outputLength = 64)

        // Store new hash
        dataStoreManager.saveMasterPasswordHash(
            java.util.Base64.getEncoder().encodeToString(newMasterKeyHash),
            java.util.Base64.getEncoder().encodeToString(newSalt)
        )

        // Re-encrypt DB passphrase with new master key
        val encryptedPassphrase = cryptoEngine.encrypt(currentDbPassphrase, newMasterKey)
        dataStoreManager.saveEncryptedDbPassphrase(
            encryptedPassphrase.ciphertext,
            encryptedPassphrase.iv
        )

        // Update cache
        cachedMasterKey = newMasterKey
        cachedFieldKey = cryptoEngine.deriveFieldKey(newMasterKey, "field_encryption")

        return currentDbPassphrase
    }
}
