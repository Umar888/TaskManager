package com.example.taskmanager.data.local

import com.example.taskmanager.data.model.Task

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 6)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        /** 2.1: reminders, repeating tasks and checklists. Additive only, so existing tasks are kept. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN reminderMinutes INTEGER")
                db.execSQL("ALTER TABLE tasks ADD COLUMN `repeat` TEXT NOT NULL DEFAULT 'None'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN checklist TEXT")
            }
        }

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
