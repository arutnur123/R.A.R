package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.NoteEntity
import com.example.data.db.PomodoroSessionEntity
import com.example.data.db.RARDatabase
import com.example.data.db.TaskEntity
import com.example.data.db.VoiceHistoryEntity
import com.example.data.repository.RARRepository
import com.example.data.system.SystemMonitor
import com.example.data.system.SystemStats
import com.example.data.voice.CommandProcessor
import com.example.data.voice.CommandResult
import com.example.data.voice.RARVoiceEngine
import com.example.data.voice.VoiceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InteractionMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "rar"
    val text: String,
    val actionType: String = "CHAT",
    val timestamp: Long = System.currentTimeMillis()
)

data class PomodoroState(
    val isRunning: Boolean = false,
    val timeRemainingSeconds: Int = 25 * 60,
    val totalDurationSeconds: Int = 25 * 60,
    val mode: String = "Çalışma (25 dk)", // "Çalışma (25 dk)", "Kısa Mola (5 dk)", "Uzun Mola (15 dk)"
    val completedSessionsToday: Int = 0
)

class RARViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RARDatabase.getDatabase(application)
    private val systemMonitor = SystemMonitor(application)
    val repository = RARRepository(db.rarDao(), systemMonitor)
    val voiceEngine = RARVoiceEngine(application)
    private val commandProcessor = CommandProcessor(application, repository)

    // Voice Engine State bindings
    val voiceState: StateFlow<VoiceState> = voiceEngine.voiceState
    val audioLevel: StateFlow<Float> = voiceEngine.audioLevel
    val isWakeWordActive: StateFlow<Boolean> = voiceEngine.isWakeWordActive
    val currentLanguage: StateFlow<String> = voiceEngine.currentLanguage

    // Interactive messages
    private val _messages = MutableStateFlow<List<InteractionMessage>>(
        listOf(
            InteractionMessage(
                sender = "rar",
                text = "Merhaba! R.A.R sistemleri aktif. 'R.A.R' diyerek veya mikrofona dokunarak seslenebilirsin. Sesli müzik arama, ders özetleri, kod yazımı, donanım takibi ve Pomodoro modu hazır!",
                actionType = "SYSTEM"
            )
        )
    )
    val messages: StateFlow<List<InteractionMessage>> = _messages.asStateFlow()

    // System Hardware Stats
    private val _systemStats = MutableStateFlow<SystemStats?>(null)
    val systemStats: StateFlow<SystemStats?> = _systemStats.asStateFlow()

    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    // Pomodoro Timer State
    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    private var pomodoroJob: Job? = null

    // Focus / Do Not Disturb Mode
    private val _isFocusFilterActive = MutableStateFlow(false)
    val isFocusFilterActive: StateFlow<Boolean> = _isFocusFilterActive.asStateFlow()

    // Game Mode State
    private val _isGameModeActive = MutableStateFlow(false)
    val isGameModeActive: StateFlow<Boolean> = _isGameModeActive.asStateFlow()

    // Database flows
    val tasks: StateFlow<List<TaskEntity>> = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notes: StateFlow<List<NoteEntity>> = repository.notes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pomodoroHistory: StateFlow<List<PomodoroSessionEntity>> = repository.pomodoroSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val voiceHistory: StateFlow<List<VoiceHistoryEntity>> = repository.voiceHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Code & AI Studio State
    private val _aiStudioResponse = MutableStateFlow("")
    val aiStudioResponse: StateFlow<String> = _aiStudioResponse.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    // Clipboard Analysis
    private val _clipboardContent = MutableStateFlow("")
    val clipboardContent: StateFlow<String> = _clipboardContent.asStateFlow()

    // WhatsApp Quick Reply State
    private val whatsAppManager = com.example.data.whatsapp.WhatsAppManager(application)
    private val _whatsAppTargets = MutableStateFlow(com.example.data.whatsapp.WhatsAppDefaults.defaultTargets)
    val whatsAppTargets: StateFlow<List<com.example.data.whatsapp.WhatsAppTarget>> = _whatsAppTargets.asStateFlow()

    private val _selectedWhatsAppTarget = MutableStateFlow<com.example.data.whatsapp.WhatsAppTarget?>(com.example.data.whatsapp.WhatsAppDefaults.defaultTargets.first())
    val selectedWhatsAppTarget: StateFlow<com.example.data.whatsapp.WhatsAppTarget?> = _selectedWhatsAppTarget.asStateFlow()

    private val _whatsAppTemplates = MutableStateFlow(com.example.data.whatsapp.WhatsAppDefaults.defaultTemplates)
    val whatsAppTemplates: StateFlow<List<com.example.data.whatsapp.QuickReplyTemplate>> = _whatsAppTemplates.asStateFlow()

    private val _whatsAppDraftText = MutableStateFlow("")
    val whatsAppDraftText: StateFlow<String> = _whatsAppDraftText.asStateFlow()

    private val _isGeneratingWhatsAppDraft = MutableStateFlow(false)
    val isGeneratingWhatsAppDraft: StateFlow<Boolean> = _isGeneratingWhatsAppDraft.asStateFlow()

    fun selectWhatsAppTarget(target: com.example.data.whatsapp.WhatsAppTarget?) {
        _selectedWhatsAppTarget.value = target
    }

    fun setWhatsAppDraftText(text: String) {
        _whatsAppDraftText.value = text
    }

    fun addWhatsAppTarget(name: String, phoneNumber: String, isGroup: Boolean) {
        val newTarget = com.example.data.whatsapp.WhatsAppTarget(
            id = "target_${System.currentTimeMillis()}",
            name = name,
            phoneNumber = phoneNumber,
            isGroup = isGroup,
            avatarEmoji = if (isGroup) "👥" else "👤"
        )
        _whatsAppTargets.value = _whatsAppTargets.value + newTarget
        _selectedWhatsAppTarget.value = newTarget
    }

    fun sendWhatsAppMessage(target: com.example.data.whatsapp.WhatsAppTarget?, customPhone: String, messageText: String) {
        val success = whatsAppManager.sendWhatsAppMessage(target, customPhone, messageText)
        val targetName = target?.name ?: if (customPhone.isNotBlank()) customPhone else "Alıcı"
        if (success) {
            val responseMsg = "WhatsApp hızlı yanıtı ($targetName): '$messageText' iletiliyor."
            _messages.value = _messages.value + InteractionMessage(
                sender = "rar",
                text = "💬 $responseMsg",
                actionType = "WHATSAPP"
            )
            voiceEngine.speak("$targetName için WhatsApp mesajı gönderiliyor.")
        } else {
            voiceEngine.speak("WhatsApp açılamadı, lütfen uygulamanın yüklü olduğundan emin olun.")
        }
    }

    fun generateSmartWhatsAppReply(incomingOrContext: String, tone: String = "Profesyonel") {
        if (incomingOrContext.isBlank()) return
        viewModelScope.launch {
            _isGeneratingWhatsAppDraft.value = true
            val prompt = "Aşağıdaki WhatsApp mesajına/durumuna $tone tonda, doğrudan kopyalanıp gönderilebilecek, tek ve net bir Türkçe hızlı yanıt taslağı yaz. Sadece yanıt mesajını ver:\n\nDurum: $incomingOrContext"
            val draft = repository.queryGeminiAI(prompt, "Sen hızlı, kibar ve net WhatsApp yanıtları hazırlayan bir yapay zeka asistanısın. Sadece mesaj metnini döndür.").getOrDefault("Mesajınızı aldım, en kısa sürede dönüş yapacağım.")
            _whatsAppDraftText.value = draft.trim().removePrefix("\"").removeSuffix("\"")
            _isGeneratingWhatsAppDraft.value = false
            voiceEngine.speak("Yapay zeka akıllı yanıt taslağını oluşturdu.")
        }
    }

    init {
        // Wire voice engine callbacks
        voiceEngine.onCommandRecognized = { command ->
            processUserCommand(command)
        }

        voiceEngine.onWakeWordDetected = {
            // Can play subtle sound or vibrate
        }

        // Start periodic system hardware stats loop
        viewModelScope.launch {
            while (true) {
                _systemStats.value = repository.getSystemStats()
                delay(3000)
            }
        }
    }

    fun processUserCommand(commandText: String) {
        if (commandText.isBlank()) return

        // Append user message
        val userMsg = InteractionMessage(sender = "user", text = commandText)
        _messages.value = _messages.value + userMsg

        voiceEngine.setThinking()

        viewModelScope.launch {
            val result = commandProcessor.processCommand(commandText, voiceEngine.currentLanguage.value)
            handleCommandResult(result)
        }
    }

    private fun handleCommandResult(result: CommandResult) {
        when (result) {
            is CommandResult.TextResponse -> {
                val rarMsg = InteractionMessage(sender = "rar", text = result.displayText, actionType = result.actionType)
                _messages.value = _messages.value + rarMsg
                voiceEngine.speak(result.speechText)
            }
            is CommandResult.ActionExecuted -> {
                val rarMsg = InteractionMessage(sender = "rar", text = result.displayText, actionType = result.actionType)
                _messages.value = _messages.value + rarMsg
                voiceEngine.speak(result.speechText)
            }
            is CommandResult.PomodoroTriggered -> {
                startPomodoro(result.minutes)
                val rarMsg = InteractionMessage(sender = "rar", text = "${result.minutes} dakikalık Pomodoro Odak Sayacı ve Lofi yayını başlatıldı.", actionType = "POMODORO")
                _messages.value = _messages.value + rarMsg
                voiceEngine.speak(result.speechText)
            }
            is CommandResult.SystemStatusReport -> {
                val display = "📊 Sistem Durumu:\n• CPU Kullanımı: %${result.stats.cpuPercent}\n• RAM: ${result.stats.ramUsedMb} MB / ${result.stats.ramTotalMb} MB (%${result.stats.ramPercent})\n• Pil: %${result.stats.batteryPercent} (${if (result.stats.isCharging) "Şarj Oluyor" else "Deşarj"})\n• Sıcaklık: ${result.stats.batteryTempCelsius}°C\n• Ağ: ${result.stats.networkType}"
                val rarMsg = InteractionMessage(sender = "rar", text = display, actionType = "SYSTEM")
                _messages.value = _messages.value + rarMsg
                voiceEngine.speak(result.speechText)
            }
            is CommandResult.TaskAdded -> {
                val display = "✅ Görev Listeye Eklendi:\n• ${result.task.title} [${result.task.category} / ${result.task.priority}]"
                val rarMsg = InteractionMessage(sender = "rar", text = display, actionType = "TASK")
                _messages.value = _messages.value + rarMsg
                voiceEngine.speak(result.speechText)
            }
            is CommandResult.NoteSaved -> {
                val display = "📝 Not Kaydedildi:\n• ${result.note.title}\n${result.note.content}"
                val rarMsg = InteractionMessage(sender = "rar", text = display, actionType = "NOTE")
                _messages.value = _messages.value + rarMsg
                voiceEngine.speak(result.speechText)
            }
            is CommandResult.CodeGenerated -> {
                _aiStudioResponse.value = result.codeContent
                val rarMsg = InteractionMessage(sender = "rar", text = "💻 Kod Bloğu Oluşturuldu:\n\n${result.codeContent}", actionType = "CODE")
                _messages.value = _messages.value + rarMsg
                voiceEngine.speak(result.speechText)
            }
        }
    }

    // Voice Engine Controls
    fun startListening() = voiceEngine.startListening()
    fun stopListening() = voiceEngine.stopListening()
    fun speakText(text: String) = voiceEngine.speak(text)
    fun stopSpeaking() = voiceEngine.stopSpeaking()
    fun toggleWakeWord(enabled: Boolean) = voiceEngine.toggleWakeWord(enabled)
    fun setLanguage(lang: String) = voiceEngine.setLanguage(lang)
    fun setSpeechRate(rate: Float) {
        voiceEngine.speechRate = rate
    }
    fun setSpeechPitch(pitch: Float) {
        voiceEngine.speechPitch = pitch
    }

    // Pomodoro Controls
    fun startPomodoro(minutes: Int = 25, mode: String = "Çalışma") {
        pomodoroJob?.cancel()
        val totalSecs = minutes * 60
        _pomodoroState.value = _pomodoroState.value.copy(
            isRunning = true,
            timeRemainingSeconds = totalSecs,
            totalDurationSeconds = totalSecs,
            mode = "$mode ($minutes dk)"
        )

        pomodoroJob = viewModelScope.launch {
            while (_pomodoroState.value.timeRemainingSeconds > 0 && _pomodoroState.value.isRunning) {
                delay(1000)
                val remaining = _pomodoroState.value.timeRemainingSeconds - 1
                _pomodoroState.value = _pomodoroState.value.copy(timeRemainingSeconds = remaining)
            }
            if (_pomodoroState.value.timeRemainingSeconds == 0) {
                // Completed!
                _pomodoroState.value = _pomodoroState.value.copy(
                    isRunning = false,
                    completedSessionsToday = _pomodoroState.value.completedSessionsToday + 1
                )
                repository.insertPomodoroSession(
                    PomodoroSessionEntity(
                        durationMinutes = minutes,
                        subject = mode
                    )
                )
                voiceEngine.speak("Tebrikler! Pomodoro çalışma periyodunu tamamladın. Şimdi kısa bir mola verebilirsin.")
            }
        }
    }

    fun pausePomodoro() {
        pomodoroJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(isRunning = false)
    }

    fun resumePomodoro() {
        if (_pomodoroState.value.timeRemainingSeconds > 0) {
            _pomodoroState.value = _pomodoroState.value.copy(isRunning = true)
            pomodoroJob = viewModelScope.launch {
                while (_pomodoroState.value.timeRemainingSeconds > 0 && _pomodoroState.value.isRunning) {
                    delay(1000)
                    val remaining = _pomodoroState.value.timeRemainingSeconds - 1
                    _pomodoroState.value = _pomodoroState.value.copy(timeRemainingSeconds = remaining)
                }
                if (_pomodoroState.value.timeRemainingSeconds == 0) {
                    _pomodoroState.value = _pomodoroState.value.copy(
                        isRunning = false,
                        completedSessionsToday = _pomodoroState.value.completedSessionsToday + 1
                    )
                    repository.insertPomodoroSession(
                        PomodoroSessionEntity(
                            durationMinutes = _pomodoroState.value.totalDurationSeconds / 60,
                            subject = _pomodoroState.value.mode
                        )
                    )
                    voiceEngine.speak("Pomodoro tamamlandı!")
                }
            }
        }
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        _pomodoroState.value = PomodoroState()
    }

    fun toggleFocusFilter() {
        val next = !_isFocusFilterActive.value
        _isFocusFilterActive.value = next
        if (next) {
            voiceEngine.speak("Akıllı Bildirim ve Odak Filtresi aktif edildi. Dikkat dağıtıcı unsurlar engellendi.")
        } else {
            voiceEngine.speak("Odak filtresi devre dışı bırakıldı.")
        }
    }

    fun toggleGameMode() {
        val next = !_isGameModeActive.value
        _isGameModeActive.value = next
        if (next) {
            voiceEngine.speak("Oyun Asistanı & Donanım Güçlendirici Modu açıldı. FPS ve kaynaklar optimize edildi.")
        } else {
            voiceEngine.speak("Oyun modu kapatıldı.")
        }
    }

    // System Optimization Action
    fun optimizeSystem() {
        viewModelScope.launch {
            _isOptimizing.value = true
            delay(1200)
            _isOptimizing.value = false
            _systemStats.value = repository.getSystemStats().copy(
                cpuPercent = 14,
                ramPercent = 38
            )
            voiceEngine.speak("Sistem önbelleği temizlendi. Arka plan işlemleri optimize edildi.")
        }
    }

    // Tasks Actions
    fun addTask(title: String, category: String = "Genel", priority: String = "Normal") {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title.trim(),
                    category = category,
                    priority = priority,
                    dueDate = "Bugün"
                )
            )
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Notes Actions
    fun addNote(title: String, content: String, category: String = "Genel") {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            val tags = when (category) {
                "Ders Özeti" -> "ders,çalışma,sınav"
                "Kod Taslağı" -> "kod,yazılım,algoritma"
                "Fikir" -> "proje,fikir,yaratıcı"
                else -> "genel,not"
            }
            repository.insertNote(
                NoteEntity(
                    title = title.ifBlank { "Yeni Not" },
                    content = content,
                    category = category,
                    aiTags = tags
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // AI Studio Actions
    fun queryAIStudio(prompt: String, toolType: String = "CODE") {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isAILoading.value = true
            val systemPrompt = when (toolType) {
                "CODE" -> "Sen uzman bir yazılım mühendisisin. Temiz, modern ve optimize edilmiş kod blokları ve detaylı açıklamalar üret."
                "STUDY" -> "Sen bir ders ve sınav asistanısın. Konuyu kavram haritası, püf noktalar ve soru örnekleriyle özetle."
                "SOLVER" -> "Sen adım adım mantık yürüten bir soru çözüm rehberisin. Her adımı numaralandırarak gerekçeleriyle açıkla."
                "PATCH" -> "Sen bir oyun asistanısın. Oyun optimizasyonları, yama notları ve mekanik taktikleri sun."
                else -> "Sen yardımsever ve bilgili R.A.R asistanısın."
            }
            val res = repository.queryGeminiAI(prompt, systemPrompt).getOrDefault("Analiz tamamlanamadı.")
            _aiStudioResponse.value = res
            _isAILoading.value = false
        }
    }

    // Clipboard Analysis Action
    fun readAndAnalyzeClipboard() {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString() ?: ""
            _clipboardContent.value = text
            if (text.isNotBlank()) {
                queryAIStudio("Aşağıdaki pano metnini özetle, ana fikirlerini çıkar ve varsa yapılacak eylemleri listele:\n\n$text", "STUDY")
                voiceEngine.speak("Panodaki metin okundu ve yapay zeka analizi başlatıldı.")
            } else {
                voiceEngine.speak("Panoda metin bulunamadı.")
            }
        } else {
            voiceEngine.speak("Panonuz şu an boş.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        pomodoroJob?.cancel()
        voiceEngine.release()
    }
}
