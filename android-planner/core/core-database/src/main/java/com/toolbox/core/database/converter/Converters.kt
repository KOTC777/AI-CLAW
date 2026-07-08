package com.toolbox.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters for common types.
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromByteArray(value: ByteArray?): String? =
        value?.let { java.util.Base64.getEncoder().encodeToString(it) }

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? =
        value?.let { java.util.Base64.getDecoder().decode(it) }
}
