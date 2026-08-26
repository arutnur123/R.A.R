package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Genel", // Ders, Kod, Günlük, Oyun, Hatırlatıcı
    val priority: String = "Normal", // Düşük, Normal, Yüksek
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "Genel", // Ders Özeti, Kod Taslağı, Fikir, Toplantı
    val aiTags: String = "not", // comma-separated tags
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int = 25,
    val subject: String = "Çalışma",
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "voice_history")
data class VoiceHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userCommand: String,
    val assistantResponse: String,
    val actionType: String = "CHAT", // SPOTIFY, YOUTUBE, SYSTEM, STUDY, CODE, TASK, NOTE, etc.
    val language: String = "tr",
    val timestamp: Long = System.currentTimeMillis()
)
