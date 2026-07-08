package com.toolbox.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "password_group")
data class PasswordGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String? = null,
    val color: Int? = null,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)

@Entity(
    tableName = "password_entry",
    foreignKeys = [
        ForeignKey(
            entity = PasswordGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["group_id"])]
)
data class PasswordEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "group_id") val groupId: String? = null,
    val icon: String? = null,

    // Level 0: Password (field-level AES-256-GCM encrypted)
    @ColumnInfo(name = "password_encrypted") val passwordEncrypted: ByteArray,
    @ColumnInfo(name = "password_iv") val passwordIv: ByteArray,
    @ColumnInfo(name = "password_tag") val passwordTag: ByteArray,

    // Level 1: Hints (field-level AES-256-GCM encrypted, JSON array)
    @ColumnInfo(name = "hints_encrypted") val hintsEncrypted: ByteArray,
    @ColumnInfo(name = "hints_iv") val hintsIv: ByteArray,
    @ColumnInfo(name = "hints_tag") val hintsTag: ByteArray,

    // Level 2: Extension (field-level AES-256-GCM encrypted, reserved)
    @ColumnInfo(name = "ext_encrypted") val extEncrypted: ByteArray? = null,
    @ColumnInfo(name = "ext_iv") val extIv: ByteArray? = null,
    @ColumnInfo(name = "ext_tag") val extTag: ByteArray? = null,

    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null
) {
    // Override equals/hashCode for ByteArray fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PasswordEntryEntity) return false
        return id == other.id && title == other.title
    }

    override fun hashCode(): Int = id.hashCode() * 31 + title.hashCode()
}
