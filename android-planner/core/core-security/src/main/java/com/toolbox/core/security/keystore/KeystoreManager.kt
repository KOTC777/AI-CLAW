package com.toolbox.core.security.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Manages Android Keystore for hardware-backed key storage.
 */
class KeystoreManager {

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    /**
     * Get or create a symmetric key in Android Keystore.
     */
    fun getOrCreateKey(alias: String): SecretKey {
        keyStore.getEntry(alias, null)?.let { entry ->
            return (entry as KeyStore.SecretKeyEntry).secretKey
        }

        val keyGenSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Allow background access
            .build()

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Check if a key exists.
     */
    fun keyExists(alias: String): Boolean =
        keyStore.containsAlias(alias)

    /**
     * Delete a key.
     */
    fun deleteKey(alias: String) {
        keyStore.deleteEntry(alias)
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    }
}
