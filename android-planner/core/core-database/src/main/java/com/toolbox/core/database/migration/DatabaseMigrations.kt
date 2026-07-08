package com.toolbox.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration strategy.
 * Add new migrations here as the schema evolves.
 */
object DatabaseMigrations {

    /**
     * Migration from version 1 to 2.
     * Example: Add new column to inspiration_note.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Example: Add a 'pinned' column to inspiration_note
            // db.execSQL("ALTER TABLE inspiration_note ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * Get all migrations for the database builder.
     */
    fun getAllMigrations(): Array<Migration> = arrayOf(
        MIGRATION_1_2
    )

    /**
     * Fallback strategy for development: destructive migration.
     * In production, use proper migrations above.
     */
    fun getDevStrategy(): androidx.room.RoomDatabase.Callback {
        return object : androidx.room.RoomDatabase.Callback() {
            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                // Log destructive migration for debugging
                android.util.Log.w("DatabaseMigrations", "Destructive migration occurred!")
            }
        }
    }
}
