package com.toolbox.core.security.crypto

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom

/**
 * Argon2id key derivation function using BouncyCastle.
 */
class Argon2Kdf {

    private val secureRandom = SecureRandom()

    /**
     * Generate a random salt.
     */
    fun generateSalt(length: Int = SALT_LENGTH): ByteArray {
        val salt = ByteArray(length)
        secureRandom.nextBytes(salt)
        return salt
    }

    /**
     * Derive a key from password using Argon2id.
     */
    fun deriveKey(
        password: CharArray,
        salt: ByteArray,
        memoryKb: Int = MEMORY_KB,
        iterations: Int = ITERATIONS,
        parallelism: Int = PARALLELISM,
        outputLength: Int = OUTPUT_LENGTH
    ): ByteArray {
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withMemoryAsKB(memoryKb)
            .withIterations(iterations)
            .withParallelism(parallelism)
            .withSalt(salt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(params)

        val passwordBytes = CharArray(password.size).also { password.copyInto(it) }
        val output = ByteArray(outputLength)
        generator.generateBytes(passwordBytes, output)

        // Clear sensitive data
        passwordBytes.fill('\u0000')

        return output
    }

    companion object {
        const val SALT_LENGTH = 16
        const val MEMORY_KB = 65536      // 64 MB
        const val ITERATIONS = 3
        const val PARALLELISM = 1
        const val OUTPUT_LENGTH = 32      // 256 bits
    }
}
