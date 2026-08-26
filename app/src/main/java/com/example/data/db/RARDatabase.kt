package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        NoteEntity::class,
        PomodoroSessionEntity::class,
        VoiceHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RARDatabase : RoomDatabase() {
    abstract fun rarDao(): RARDao

    companion object {
        @Volatile
        private var INSTANCE: RARDatabase? = null

        fun getDatabase(context: Context): RARDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RARDatabase::class.java,
                    "rar_assistant_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
