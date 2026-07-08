package com.toolbox.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.toolbox.core.database.converter.Converters
import com.toolbox.core.database.dao.AiProcessingLogDao
import com.toolbox.core.database.dao.AiProviderConfigDao
import com.toolbox.core.database.dao.AiScheduledTaskDao
import com.toolbox.core.database.dao.AppLockConfigDao
import com.toolbox.core.database.dao.CheckinRecordDao
import com.toolbox.core.database.dao.CheckinTaskDao
import com.toolbox.core.database.dao.InspirationAttachmentDao
import com.toolbox.core.database.dao.InspirationNoteDao
import com.toolbox.core.database.dao.InspirationTemplateDao
import com.toolbox.core.database.dao.MemoDao
import com.toolbox.core.database.dao.PasswordEntryDao
import com.toolbox.core.database.dao.PasswordGroupDao
import com.toolbox.core.database.dao.ScheduleEventDao
import com.toolbox.core.database.entity.AiProcessingLogEntity
import com.toolbox.core.database.entity.AiProviderConfigEntity
import com.toolbox.core.database.entity.AiScheduledTaskEntity
import com.toolbox.core.database.entity.AppLockConfigEntity
import com.toolbox.core.database.entity.CheckinRecordEntity
import com.toolbox.core.database.entity.CheckinTaskEntity
import com.toolbox.core.database.entity.InspirationAttachmentEntity
import com.toolbox.core.database.entity.InspirationNoteEntity
import com.toolbox.core.database.entity.InspirationTemplateEntity
import com.toolbox.core.database.entity.MemoEntity
import com.toolbox.core.database.entity.PasswordEntryEntity
import com.toolbox.core.database.entity.PasswordGroupEntity
import com.toolbox.core.database.entity.ScheduleEventEntity
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        MemoEntity::class,
        ScheduleEventEntity::class,
        CheckinTaskEntity::class,
        CheckinRecordEntity::class,
        PasswordGroupEntity::class,
        PasswordEntryEntity::class,
        InspirationNoteEntity::class,
        InspirationAttachmentEntity::class,
        InspirationTemplateEntity::class,
        AiProcessingLogEntity::class,
        AiProviderConfigEntity::class,
        AiScheduledTaskEntity::class,
        AppLockConfigEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Memo
    abstract fun memoDao(): MemoDao

    // Schedule
    abstract fun scheduleEventDao(): ScheduleEventDao

    // Checkin
    abstract fun checkinTaskDao(): CheckinTaskDao
    abstract fun checkinRecordDao(): CheckinRecordDao

    // Password
    abstract fun passwordGroupDao(): PasswordGroupDao
    abstract fun passwordEntryDao(): PasswordEntryDao

    // Inspiration
    abstract fun inspirationNoteDao(): InspirationNoteDao
    abstract fun inspirationAttachmentDao(): InspirationAttachmentDao
    abstract fun inspirationTemplateDao(): InspirationTemplateDao

    // AI
    abstract fun aiProcessingLogDao(): AiProcessingLogDao
    abstract fun aiProviderConfigDao(): AiProviderConfigDao
    abstract fun aiScheduledTaskDao(): AiScheduledTaskDao

    // App Lock
    abstract fun appLockConfigDao(): AppLockConfigDao

    companion object {
        private const val DATABASE_NAME = "toolbox.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Create database with SQLCipher encryption.
         * @param passphrase The database encryption key (derived from master password).
         */
        fun getInstance(context: Context, passphrase: ByteArray): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, passphrase).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context, passphrase: ByteArray): AppDatabase {
            val supportFactory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(supportFactory)
                .fallbackToDestructiveMigration() // TODO: Replace with proper migrations in production
                .build()
        }

        /**
         * Clear the cached instance (e.g., on logout/lock).
         */
        fun clearInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
