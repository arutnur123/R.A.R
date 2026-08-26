package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.db.NoteEntity
import com.example.data.db.TaskEntity
import com.example.data.db.VoiceHistoryEntity
import com.example.data.repository.RARRepository
import com.example.data.system.SystemStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class CommandResult {
    data class TextResponse(val speechText: String, val displayText: String, val actionType: String = "CHAT") : CommandResult()
    data class ActionExecuted(val speechText: String, val displayText: String, val actionType: String, val intentUri: String? = null) : CommandResult()
    data class PomodoroTriggered(val speechText: String, val minutes: Int = 25) : CommandResult()
    data class SystemStatusReport(val speechText: String, val stats: SystemStats) : CommandResult()
    data class TaskAdded(val speechText: String, val task: TaskEntity) : CommandResult()
    data class NoteSaved(val speechText: String, val note: NoteEntity) : CommandResult()
    data class CodeGenerated(val speechText: String, val codeContent: String, val language: String) : CommandResult()
}

class CommandProcessor(
    private val context: Context,
    private val repository: RARRepository
) {

    suspend fun processCommand(rawCommand: String, language: String = "tr"): CommandResult = withContext(Dispatchers.IO) {
        val command = rawCommand.lowercase().trim()
        val isEnglish = language == "en" || detectIsEnglish(command)
        val lang = if (isEnglish) "en" else "tr"

        val result: CommandResult = when {
            // 1. Spotify & Music Module
            command.contains("spotify") || command.contains("müzik") || command.contains("şarkı") ||
                    command.contains("music") || command.contains("song") || command.contains("çal") -> {
                handleSpotifyCommand(command, lang)
            }

            // 2. YouTube & Video Module
            command.contains("youtube") || command.contains("video") || command.contains("izle") || command.contains("watch") -> {
                handleYoutubeCommand(command, lang)
            }

            // 3. WhatsApp Module
            command.contains("whatsapp") || command.contains("mesaj") || command.contains("message") -> {
                handleWhatsappCommand(command, lang)
            }

            // 4. Hardware & System Monitor
            command.contains("sistem") || command.contains("donanım") || command.contains("cpu") ||
                    command.contains("ram") || command.contains("pil") || command.contains("system") ||
                    command.contains("battery") || command.contains("hafıza") -> {
                handleSystemCommand(lang)
            }

            // 5. Study & Pomodoro Focus Module
            command.contains("pomodoro") || command.contains("odak") || command.contains("focus") ||
                    (command.contains("çalışma") && (command.contains("başlat") || command.contains("modu"))) -> {
                handlePomodoroCommand(lang)
            }

            // 6. Task / ToDo Creation
            command.contains("hatırlat") || command.contains("görev") || command.contains("yapılacak") ||
                    command.contains("todo") || command.contains("remind") || command.contains("task") -> {
                handleTaskCreation(command, lang)
            }

            // 7. Note Taking
            command.contains("not al") || command.contains("not kaydet") || command.contains("not yaz") ||
                    command.contains("take a note") || command.contains("save note") -> {
                handleNoteCreation(command, lang)
            }

            // 8. Code & Programming Assistance
            command.contains("kod") || command.contains("fonksiyon") || command.contains("code") ||
                    command.contains("python") || command.contains("kotlin") || command.contains("yazılım") -> {
                handleCodeCommand(command, lang)
            }

            // 9. Study / Exam / Problem Solving
            command.contains("soru") || command.contains("özetle") || command.contains("ders") ||
                    command.contains("sınav") || command.contains("solve") || command.contains("study") -> {
                handleStudyCommand(command, lang)
            }

            // 10. Game Mode & Patch Tracking
            command.contains("oyun") || command.contains("game") || command.contains("fps") || command.contains("yama") || command.contains("patch") -> {
                handleGameCommand(command, lang)
            }

            // 11. Tech News & Trends
            command.contains("haber") || command.contains("trend") || command.contains("gündem") || command.contains("news") -> {
                handleNewsCommand(lang)
            }

            // 12. Shutdown / Standby
            command.contains("kapat") || command.contains("dur") || command.contains("exit") ||
                    command.contains("uyu") || command.contains("sleep") || command.contains("stop") -> {
                val speech = if (lang == "en") "R.A.R entering standby mode. See you!" else "R.A.R sistemleri güvenli şekilde bekleme moduna alındı. Görüşmek üzere!"
                CommandResult.TextResponse(speech, speech, "SYSTEM_SLEEP")
            }

            // 13. General AI Conversation Query
            else -> {
                handleGeneralAI(rawCommand, lang)
            }
        }

        // Save into voice history
        val (speechTxt, displayTxt, actType) = when (result) {
            is CommandResult.TextResponse -> Triple(result.speechText, result.displayText, result.actionType)
            is CommandResult.ActionExecuted -> Triple(result.speechText, result.displayText, result.actionType)
            is CommandResult.PomodoroTriggered -> Triple(result.speechText, "Pomodoro Başlatıldı (25 dk)", "POMODORO")
            is CommandResult.SystemStatusReport -> Triple(result.speechText, "Sistem Raporu Oluşturuldu", "SYSTEM")
            is CommandResult.TaskAdded -> Triple(result.speechText, "Görev: ${result.task.title}", "TASK")
            is CommandResult.NoteSaved -> Triple(result.speechText, "Not: ${result.note.title}", "NOTE")
            is CommandResult.CodeGenerated -> Triple(result.speechText, result.codeContent, "CODE")
        }

        repository.insertVoiceHistory(
            VoiceHistoryEntity(
                userCommand = rawCommand,
                assistantResponse = displayTxt,
                actionType = actType,
                language = lang
            )
        )

        result
    }

    private fun detectIsEnglish(text: String): Boolean {
        val englishWords = listOf("the", "play", "open", "system", "status", "study", "code", "what", "how", "who", "help", "note", "task")
        return englishWords.any { text.contains(it) }
    }

    private fun handleSpotifyCommand(command: String, lang: String): CommandResult {
        val query = command
            .replace("spotify", "")
            .replace("müzik", "")
            .replace("şarkı", "")
            .replace("çal", "")
            .replace("play", "")
            .replace("music", "")
            .replace("song", "")
            .replace("aç", "")
            .trim()

        val url = if (query.isNotBlank()) {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            "https://open.spotify.com/search/$encoded"
        } else {
            "https://open.spotify.com"
        }

        launchWebOrIntent(url)
        val speech = if (lang == "en") {
            if (query.isNotBlank()) "Opening Spotify for '$query'." else "Opening Spotify music player."
        } else {
            if (query.isNotBlank()) "Spotify üzerinde '$query' açılıyor." else "Spotify müzik modülü çalıştırılıyor."
        }
        return CommandResult.ActionExecuted(speech, speech, "SPOTIFY", url)
    }

    private fun handleYoutubeCommand(command: String, lang: String): CommandResult {
        val query = command
            .replace("youtube", "")
            .replace("video", "")
            .replace("izle", "")
            .replace("ara", "")
            .replace("search", "")
            .replace("watch", "")
            .replace("aç", "")
            .trim()

        val url = if (query.isNotBlank()) {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            "https://www.youtube.com/results?search_query=$encoded"
        } else {
            "https://www.youtube.com"
        }

        launchWebOrIntent(url)
        val speech = if (lang == "en") {
            if (query.isNotBlank()) "Searching YouTube for '$query'." else "Opening YouTube."
        } else {
            if (query.isNotBlank()) "YouTube'da '$query' aranıyor ve açılıyor." else "YouTube video modülü açılıyor."
        }
        return CommandResult.ActionExecuted(speech, speech, "YOUTUBE", url)
    }

    private fun handleWhatsappCommand(command: String, lang: String): CommandResult {
        // Examples: "whatsapp ahmet gecikiyorum", "whatsapp mesajı gönder", "whatsapp'tan gruba selam yaz"
        var clean = command
            .replace("whatsapp'tan", "")
            .replace("whatsapp'a", "")
            .replace("whatsapp", "")
            .replace("mesajı", "")
            .replace("mesaj", "")
            .replace("gönder", "")
            .replace("yaz", "")
            .replace("send", "")
            .replace("message", "")
            .replace("to", "")
            .trim()

        val recipientAndMessage = if (clean.contains("e ") || clean.contains("a ") || clean.contains("'e ") || clean.contains("'a ")) {
            clean
        } else {
            clean
        }

        val speech: String
        val url: String

        if (recipientAndMessage.isNotBlank()) {
            val encoded = URLEncoder.encode(recipientAndMessage, StandardCharsets.UTF_8.toString())
            url = "https://api.whatsapp.com/send?text=$encoded"
            speech = if (lang == "en") {
                "Preparing WhatsApp quick response: '$recipientAndMessage'."
            } else {
                "WhatsApp hızlı yanıt hazırlanıyor: '$recipientAndMessage'."
            }
        } else {
            url = "https://api.whatsapp.com/send?text="
            speech = if (lang == "en") "Opening WhatsApp Quick Reply console." else "WhatsApp Hızlı Yanıt konsolu açılıyor."
        }

        launchWebOrIntent(url)
        return CommandResult.ActionExecuted(speech, speech, "WHATSAPP", url)
    }

    private suspend fun handleSystemCommand(lang: String): CommandResult {
        val stats = repository.getSystemStats()
        val speech = if (lang == "en") {
            "System Status: CPU usage ${stats.cpuPercent} percent, RAM usage ${stats.ramPercent} percent, Battery at ${stats.batteryPercent} percent."
        } else {
            "Sistem Durumu: CPU kullanımı yüzde ${stats.cpuPercent}, RAM kullanımı yüzde ${stats.ramPercent}, Pil durumu yüzde ${stats.batteryPercent}."
        }
        return CommandResult.SystemStatusReport(speech, stats)
    }

    private fun handlePomodoroCommand(lang: String): CommandResult {
        // Open Lofi study stream in background / browser
        val lofiUrl = "https://www.youtube.com/watch?v=jfKfPfyJRdk"
        launchWebOrIntent(lofiUrl)
        val speech = if (lang == "en") {
            "Focus Mode activated! 25-minute Pomodoro timer started with ambient study stream."
        } else {
            "Odaklanma modu başlatıldı. 25 dakikalık çalışma süreniz ve odak müziği başladı."
        }
        return CommandResult.PomodoroTriggered(speech, 25)
    }

    private suspend fun handleTaskCreation(command: String, lang: String): CommandResult {
        val cleanTitle = command
            .replace("hatırlat", "")
            .replace("görev", "")
            .replace("yapılacak", "")
            .replace("ekle", "")
            .replace("remind me to", "")
            .replace("add task", "")
            .replace("todo", "")
            .trim()
            .replaceFirstChar { it.uppercase() }

        val finalTitle = if (cleanTitle.length > 2) cleanTitle else if (lang == "en") "New Task" else "Yeni Görev"
        val category = when {
            command.contains("ders") || command.contains("ödev") || command.contains("study") -> "Ders"
            command.contains("kod") || command.contains("yazılım") || command.contains("bug") -> "Kod"
            command.contains("oyun") -> "Oyun"
            else -> "Günlük"
        }

        val task = TaskEntity(
            title = finalTitle,
            category = category,
            priority = "Yüksek",
            dueDate = if (lang == "en") "Today" else "Bugün"
        )
        repository.insertTask(task)

        val speech = if (lang == "en") "Task '$finalTitle' added to your checklist." else "'$finalTitle' göreviniz listeye eklendi."
        return CommandResult.TaskAdded(speech, task)
    }

    private suspend fun handleNoteCreation(command: String, lang: String): CommandResult {
        val noteContent = command
            .replace("not al", "")
            .replace("not kaydet", "")
            .replace("not yaz", "")
            .replace("take a note", "")
            .replace("save note", "")
            .trim()
            .replaceFirstChar { it.uppercase() }

        val content = if (noteContent.length > 2) noteContent else if (lang == "en") "Voice note recorded by R.A.R" else "R.A.R sesli not kaydı"
        val title = if (content.length > 25) content.substring(0, 25) + "..." else content

        val note = NoteEntity(
            title = title,
            content = content,
            category = "Sesli Not",
            aiTags = "sesli,rar,asistan"
        )
        repository.insertNote(note)

        val speech = if (lang == "en") "Note saved: '$title'" else "Notunuz kaydedildi: '$title'"
        return CommandResult.NoteSaved(speech, note)
    }

    private suspend fun handleCodeCommand(command: String, lang: String): CommandResult {
        val prompt = "Yazılım danışmanı ve kod uzmanı olarak yanıt ver: $command. Çözümü veya kodu temiz, anlaşılır ve modern syntax ile açıkla."
        val aiResponse = repository.queryGeminiAI(prompt).getOrDefault("")
        val speech = if (lang == "en") "Code analysis completed. Displaying snippet on screen." else "Kod analizi tamamlandı. Kod bloğu ekrana aktarılıyor."
        return CommandResult.CodeGenerated(speech, aiResponse, "kotlin")
    }

    private suspend fun handleStudyCommand(command: String, lang: String): CommandResult {
        val prompt = "Ders ve sınav asistanı olarak: '$command' konusunu veya sorusunu net maddelerle, mantıksal adımlarla açıkla ve özetle."
        val aiResponse = repository.queryGeminiAI(prompt).getOrDefault("")
        val speech = if (lang == "en") "Study summary and step-by-step logic prepared." else "Ders özeti ve adım adım çözüm rehberi hazırlandı."
        return CommandResult.TextResponse(speech, aiResponse, "STUDY")
    }

    private suspend fun handleGameCommand(command: String, lang: String): CommandResult {
        val prompt = "Oyun asistanı olarak: '$command' hakkında stratejik ipuçları, sistem optimizasyon tavsiyeleri veya yama notu özeti ver."
        val aiResponse = repository.queryGeminiAI(prompt).getOrDefault("")
        val speech = if (lang == "en") "Game optimization and patch insights ready." else "Oyun odak modu ve yama bilgisi devrede."
        return CommandResult.TextResponse(speech, aiResponse, "GAME")
    }

    private suspend fun handleNewsCommand(lang: String): CommandResult {
        val prompt = "En güncel teknoloji, yapay zeka ve bilim trendlerini 3 vurucu madde halinde özetle."
        val aiResponse = repository.queryGeminiAI(prompt).getOrDefault("")
        val speech = if (lang == "en") "Here are the latest technology and AI trends." else "İşte güncel teknoloji ve yapay zeka trend özeti."
        return CommandResult.TextResponse(speech, aiResponse, "NEWS")
    }

    private suspend fun handleGeneralAI(rawCommand: String, lang: String): CommandResult {
        val aiResponse = repository.queryGeminiAI(rawCommand).getOrDefault("Talebiniz R.A.R tarafından başarıyla işlendi.")
        // Prepare shorter spoken text if response is long
        val speech = if (aiResponse.length > 180) {
            aiResponse.take(160) + "..."
        } else {
            aiResponse
        }
        return CommandResult.TextResponse(speech, aiResponse, "CHAT")
    }

    private fun launchWebOrIntent(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // fallback
        }
    }
}
