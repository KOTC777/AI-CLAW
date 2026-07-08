package com.toolbox.core.common.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Flow extension: map errors to Result type.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.success(it) }
    .catch { emit(Result.failure(it)) }

/**
 * String extensions.
 */
fun String.isNotNullOrBlank(): Boolean = !isNullOrBlank()

/**
 * Long extensions for time formatting.
 */
fun Long.toFormattedDate(pattern: String = "yyyy-MM-dd"): String {
    val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}

fun Long.toFormattedDateTime(): String = toFormattedDate("yyyy-MM-dd HH:mm")

fun Long.toFormattedTime(): String = toFormattedDate("HH:mm")

/**
 * ByteArray extensions.
 */
fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

fun String.hexToBytes(): ByteArray =
    chunked(2).map { it.toInt(16).toByte() }.toByteArray()
