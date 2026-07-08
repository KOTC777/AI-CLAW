package com.toolbox.core.common.util

import java.util.UUID

/**
 * UUID generator for entity IDs.
 */
object IdGenerator {
    fun generate(): String = UUID.randomUUID().toString()
}

/**
 * Time utilities.
 */
object TimeUtil {
    fun nowMillis(): Long = System.currentTimeMillis()

    fun startOfDay(millis: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfDay(millis: Long): Long = startOfDay(millis) + 24 * 60 * 60 * 1000 - 1
}

/**
 * Byte array utilities for encryption.
 */
object ByteUtil {
    fun toHexString(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    fun fromHexString(hex: String): ByteArray =
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    fun concat(vararg arrays: ByteArray): ByteArray {
        val result = ByteArray(arrays.sumOf { it.size })
        var offset = 0
        for (array in arrays) {
            array.copyInto(result, offset)
            offset += array.size
        }
        return result
    }
}
