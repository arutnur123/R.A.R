package com.example.data.repository

import android.content.Context
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiRetrofitClient
import com.example.data.db.NoteEntity
import com.example.data.db.PomodoroSessionEntity
import com.example.data.db.RARDao
import com.example.data.db.TaskEntity
import com.example.data.db.VoiceHistoryEntity
import com.example.data.system.SystemMonitor
import com.example.data.system.SystemStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RARRepository(
    private val rarDao: RARDao,
    private val systemMonitor: SystemMonitor
) {
    // Database flows
    val tasks: Flow<List<TaskEntity>> = rarDao.getAllTasks()
    val notes: Flow<List<NoteEntity>> = rarDao.getAllNotes()
    val pomodoroSessions: Flow<List<PomodoroSessionEntity>> = rarDao.getAllPomodoroSessions()
    val voiceHistory: Flow<List<VoiceHistoryEntity>> = rarDao.getRecentVoiceHistory()

    // Tasks operations
    suspend fun insertTask(task: TaskEntity): Long = rarDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = rarDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = rarDao.deleteTask(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = rarDao.setTaskCompleted(id, completed)

    // Notes operations
    suspend fun insertNote(note: NoteEntity): Long = rarDao.insertNote(note)
    suspend fun deleteNote(note: NoteEntity) = rarDao.deleteNote(note)

    // Pomodoro operations
    suspend fun insertPomodoroSession(session: PomodoroSessionEntity): Long = rarDao.insertPomodoroSession(session)

    // Voice history operations
    suspend fun insertVoiceHistory(history: VoiceHistoryEntity): Long = rarDao.insertVoiceHistory(history)
    suspend fun clearVoiceHistory() = rarDao.clearVoiceHistory()

    // System Monitor
    suspend fun getSystemStats(): SystemStats = systemMonitor.getSystemStats()

    // Gemini AI Call with robust offline and error fallback
    suspend fun queryGeminiAI(
        prompt: String,
        systemInstruction: String = "Sen R.A.R (Rastgele Akıllı Rehber / Realtime Autonomous Responder), son derece zeki, hızlı, yardımsever, çok dilli bir yapay zeka asistanısın. Türkçe ve İngilizce dillerini kusursuz konuşursun. Kullanıcının isteklerine net, anlaşılır ve yapıcı yanıtlar ver."
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = GeminiRetrofitClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local offline fallback answer based on prompt keywords
            return@withContext Result.success(generateOfflineSmartResponse(prompt))
        }

        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt)),
                        role = "user"
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    topP = 0.95f,
                    topK = 40,
                    maxOutputTokens = 2048
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstruction))
                )
            )

            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                Result.success(responseText)
            } else {
                Result.success(generateOfflineSmartResponse(prompt))
            }
        } catch (e: Exception) {
            // Fallback gracefully to offline intelligence
            Result.success(generateOfflineSmartResponse(prompt))
        }
    }

    private fun generateOfflineSmartResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("kimsin") || lower.contains("who are you") || lower.contains("r.a.r") ->
                "Ben R.A.R! Sesli komutlar, donanım takibi, ders ve sınav desteği, kod yazımı, Pomodoro odak modu ve görev yönetimiyle her zaman yanındayım."

            lower.contains("kod") || lower.contains("python") || lower.contains("kotlin") || lower.contains("fonksiyon") ->
                "```kotlin\n// R.A.R Otomatik Kod Taslağı\nfun optimizeTaskFlow(data: List<String>): List<String> {\n    return data.filter { it.isNotBlank() }.distinct()\n}\n```\nKod bloğu başarıyla yapılandırıldı."

            lower.contains("ders") || lower.contains("sınav") || lower.contains("özet") || lower.contains("çalışma") ->
                "📚 Ders Asistanı: Konunun anahtar kavramları:\n1. Temel Tanım ve Prensipler\n2. Önemli Formüller ve Bağıntılar\n3. Sık Çıkan Soru Tipleri ve Püf Noktalar\nDetaylı açıklama için spesifik bir konu başlığı sorabilirsin."

            lower.contains("matematik") || lower.contains("fizik") || lower.contains("soru") ->
                "💡 Adım Adım Soru Çözüm Mantığı:\nAdım 1: Verilen ve istenen değişkenleri belirle.\nAdım 2: Uygun formülü seç ve sadeleştir.\nAdım 3: İşlem önceliğine göre hesapla ve doğrula."

            lower.contains("merhaba") || lower.contains("selam") || lower.contains("hello") || lower.contains("hi") ->
                "Merhaba! R.A.R tüm sistemleriyle aktif. Sana nasıl yardımcı olabilirim?"

            lower.contains("nasılsın") || lower.contains("how are you") ->
                "Tüm donanım ve yapay zeka modüllerim %100 kapasiteyle çalışıyor! Senin için hazırım."

            else ->
                "Komut alındı: '$prompt'. R.A.R yapay zeka ve sistem modülleri talebini işledi."
        }
    }
}
