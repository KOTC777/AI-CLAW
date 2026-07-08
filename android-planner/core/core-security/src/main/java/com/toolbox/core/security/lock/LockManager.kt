package com.toolbox.core.security.lock

import com.toolbox.core.security.crypto.Argon2Kdf
import java.security.SecureRandom

/**
 * Manages app-level and feature-level locks.
 */
class LockManager(
    private val argon2Kdf: Argon2Kdf
) {

    private val secureRandom = SecureRandom()

    /**
     * Hash a PIN for storage.
     */
    fun hashPin(pin: String): Pair<String, String> {
        val salt = argon2Kdf.generateSalt()
        val hash = argon2Kdf.deriveKey(pin.toCharArray(), salt, memoryKb = 16384, iterations = 2)
        return Pair(
            java.util.Base64.getEncoder().encodeToString(hash),
            java.util.Base64.getEncoder().encodeToString(salt)
        )
    }

    /**
     * Verify a PIN against stored hash.
     */
    fun verifyPin(pin: String, storedHash: String, storedSalt: String): Boolean {
        val salt = java.util.Base64.getDecoder().decode(storedSalt)
        val hash = argon2Kdf.deriveKey(pin.toCharArray(), salt, memoryKb = 16384, iterations = 2)
        val computedHash = java.util.Base64.getEncoder().encodeToString(hash)
        return computedHash == storedHash
    }

    /**
     * Generate a pattern hash (for pattern lock).
     * Pattern is represented as a list of node indices.
     */
    fun hashPattern(pattern: List<Int>): Pair<String, String> {
        val patternStr = pattern.joinToString("-")
        return hashPin(patternStr)
    }

    /**
     * Verify a pattern against stored hash.
     */
    fun verifyPattern(pattern: List<Int>, storedHash: String, storedSalt: String): Boolean {
        val patternStr = pattern.joinToString("-")
        return verifyPin(patternStr, storedHash, storedSalt)
    }

    sealed class AuthMethod(val value: String) {
        data object Biometric : AuthMethod("biometric")
        data object Pin : AuthMethod("pin")
        data object Pattern : AuthMethod("pattern")

        companion object {
            fun fromString(value: String): AuthMethod = when (value) {
                "biometric" -> Biometric
                "pin" -> Pin
                "pattern" -> Pattern
                else -> throw IllegalArgumentException("Unknown auth method: $value")
            }
        }
    }
}
