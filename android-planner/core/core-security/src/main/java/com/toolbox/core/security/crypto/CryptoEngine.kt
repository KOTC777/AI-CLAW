package com.toolbox.core.security.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM encryption engine for field-level encryption.
 */
class CryptoEngine {

    private val secureRandom = SecureRandom()

    /**
     * Encrypt data using AES-256-GCM.
     * Returns Triple(ciphertext, iv, authTag).
     */
    fun encrypt(data: ByteArray, key: ByteArray): EncryptionResult {
        val iv = ByteArray(IV_LENGTH)
        secureRandom.nextBytes(iv)

        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val ciphertext = cipher.doFinal(data)

        return EncryptionResult(
            ciphertext = ciphertext,
            iv = iv,
            tag = ByteArray(0) // GCM tag is appended to ciphertext by Java
        )
    }

    /**
     * Decrypt data using AES-256-GCM.
     */
    fun decrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        return cipher.doFinal(ciphertext)
    }

    /**
     * Generate a random AES-256 key.
     */
    fun generateKey(): ByteArray {
        val key = ByteArray(KEY_LENGTH)
        secureRandom.nextBytes(key)
        return key
    }

    /**
     * Derive a field encryption key from master key using HKDF-like approach.
     */
    fun deriveFieldKey(masterKey: ByteArray, info: String): ByteArray {
        val hmac = javax.crypto.Mac.getInstance("HmacSHA256")
        val keySpec = javax.crypto.spec.SecretKeySpec(masterKey, "HmacSHA256")
        hmac.init(keySpec)
        return hmac.doFinal(info.toByteArray())
    }

    data class EncryptionResult(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val tag: ByteArray
    ) {
        /**
         * Combine ciphertext and tag for storage (Java GCM appends tag to ciphertext).
         */
        fun toCombined(): ByteArray = ciphertext
    }

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_LENGTH = 32   // 256 bits
        const val IV_LENGTH = 12    // 96 bits (recommended for GCM)
        const val TAG_LENGTH = 128  // 128 bits
    }
}
