package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RARDao {
    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean)

    // Notes
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // Pomodoro Sessions
    @Query("SELECT * FROM pomodoro_sessions ORDER BY completedAt DESC")
    fun getAllPomodoroSessions(): Flow<List<PomodoroSessionEntity>>

    @Insert
    suspend fun insertPomodoroSession(session: PomodoroSessionEntity): Long

    // Voice History
    @Query("SELECT * FROM voice_history ORDER BY timestamp DESC LIMIT 100")
    fun getRecentVoiceHistory(): Flow<List<VoiceHistoryEntity>>

    @Insert
    suspend fun insertVoiceHistory(history: VoiceHistoryEntity): Long

    @Query("DELETE FROM voice_history")
    suspend fun clearVoiceHistory()
}
