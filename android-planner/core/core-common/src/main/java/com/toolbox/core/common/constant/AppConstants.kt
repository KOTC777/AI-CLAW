package com.toolbox.core.common.constant

/**
 * Application-wide constants.
 */
object AppConstants {
    const val APP_NAME = "ToolBox"
    const val DATABASE_NAME = "toolbox.db"
    const val DATASTORE_NAME = "toolbox_prefs"

    // Notification Channels
    const val CHANNEL_REMINDER = "channel_reminder"
    const val CHANNEL_CHECKIN = "channel_checkin"
    const val CHANNEL_AI = "channel_ai"
    const val CHANNEL_FOREGROUND = "channel_foreground"

    // Request Codes
    const val REQUEST_CODE_ALARM = 1001
    const val REQUEST_CODE_CHECKIN = 1002

    // Time
    const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
}

/**
 * Security-related constants.
 */
object SecurityConstants {
    const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    const val MASTER_KEY_ALIAS = "toolbox_master_key"
    const val FIELD_KEY_ALIAS = "toolbox_field_key"

    // Argon2id parameters
    const val ARGON2_MEMORY_KB = 65536  // 64 MB
    const val ARGON2_ITERATIONS = 3
    const val ARGON2_PARALLELISM = 1
    const val ARGON2_SALT_LENGTH = 16
    const val ARGON2_KEY_LENGTH = 32

    // AES-GCM
    const val AES_KEY_LENGTH = 256
    const val GCM_IV_LENGTH = 12
    const val GCM_TAG_LENGTH = 128

    // Password
    const val MIN_MASTER_PASSWORD_LENGTH = 6
}

/**
 * Database constants.
 */
object DatabaseConstants {
    const val CURRENT_VERSION = 1
}
