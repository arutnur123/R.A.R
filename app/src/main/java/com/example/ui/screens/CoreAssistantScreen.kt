package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.system.SystemStats
import com.example.data.voice.VoiceState
import com.example.ui.components.FrostedGlassBox
import com.example.ui.components.VoiceOrb
import com.example.ui.theme.*
import com.example.ui.viewmodel.InteractionMessage
import com.example.ui.viewmodel.RARViewModel
import kotlinx.coroutines.launch

@Composable
fun CoreAssistantScreen(
    viewModel: RARViewModel,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val stats by viewModel.systemStats.collectAsState()
    val isWakeWordActive by viewModel.isWakeWordActive.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showWhatsAppDialog) {
        com.example.ui.components.WhatsAppQuickReplyDialog(
            viewModel = viewModel,
            onDismiss = { showWhatsAppDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        // Frosted Glass Header
        HeaderSection(
            isWakeWordActive = isWakeWordActive,
            currentLanguage = currentLang,
            onLanguageToggle = {
                val next = if (currentLang == "tr") "en" else "tr"
                viewModel.setLanguage(next)
            },
            onWakeWordToggle = {
                viewModel.toggleWakeWord(!isWakeWordActive)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Center Voice Orb with Glowing Rings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            VoiceOrb(
                voiceState = voiceState,
                audioLevel = audioLevel,
                onClick = {
                    if (voiceState == VoiceState.LISTENING) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dual Telemetry Cards: Processor & Memory
        TelemetryRow(stats = stats)

        Spacer(modifier = Modifier.height(12.dp))

        // 4-Grid Quick Action Modules
        QuickModulesGrid(
            onQuickAction = { prompt ->
                if (prompt == "WHATSAPP_DIALOG") {
                    showWhatsAppDialog = true
                } else {
                    viewModel.processUserCommand(prompt)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Conversation Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    FrostedMessageBubble(
                        message = msg,
                        onSpeak = { text -> viewModel.speakText(text) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Frosted Glass Voice & Text Input Bar
        FrostedInputBar(
            value = inputPrompt,
            onValueChange = { inputPrompt = it },
            voiceState = voiceState,
            onSend = {
                if (inputPrompt.isNotBlank()) {
                    viewModel.processUserCommand(inputPrompt)
                    inputPrompt = ""
                    focusManager.clearFocus()
                }
            },
            onMicClick = {
                if (voiceState == VoiceState.LISTENING) {
                    viewModel.stopListening()
                } else {
                    viewModel.startListening()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HeaderSection(
    isWakeWordActive: Boolean,
    currentLanguage: String,
    onLanguageToggle: () -> Unit,
    onWakeWordToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SYSTEM ACTIVE",
                color = IndigoLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "R.A.R ",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = "Core",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language Pill Button
            FrostedGlassBox(
                modifier = Modifier.size(width = 44.dp, height = 36.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = onLanguageToggle
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = currentLanguage.uppercase(),
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // System Status Indicator with Pulsing Green Dot
            FrostedGlassBox(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                onClick = onWakeWordToggle
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isWakeWordActive) EmeraldSuccess else RoseError)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(stats: SystemStats?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Processor Card
        FrostedGlassBox(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PROCESSOR",
                    color = TextDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${stats?.cpuPercent ?: 14}%",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stable",
                        color = IndigoLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Memory Card
        FrostedGlassBox(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MEMORY",
                    color = TextDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val ramGb = String.format("%.1f", (stats?.ramUsedMb ?: 3400) / 1024.0)
                    Text(
                        text = "${ramGb}GB",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Optimized",
                        color = TealAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickModulesGrid(onQuickAction: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickModuleItem(
            icon = Icons.Default.Audiotrack,
            label = "Müzik",
            onClick = { onQuickAction("Spotify'da odaklanma müziği çal") }
        )
        QuickModuleItem(
            icon = Icons.Default.HourglassBottom,
            label = "Odak",
            onClick = { onQuickAction("Pomodoro çalışma modunu başlat") }
        )
        QuickModuleItem(
            icon = Icons.Default.Whatsapp,
            label = "Mesaj",
            onClick = { onQuickAction("WHATSAPP_DIALOG") }
        )
        QuickModuleItem(
            icon = Icons.Default.Code,
            label = "Kod",
            onClick = { onQuickAction("Kotlin ile veri filtreleme fonksiyonu yaz") }
        )
    }
}

@Composable
private fun QuickModuleItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FrostedGlassBox(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(18.dp),
            onClick = onClick
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = TextWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FrostedMessageBubble(
    message: InteractionMessage,
    onSpeak: (String) -> Unit
) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        FrostedGlassBox(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            backgroundColor = if (isUser) IndigoPrimary.copy(alpha = 0.28f) else GlassSurfaceElevated,
            borderColor = if (isUser) IndigoPrimary.copy(alpha = 0.5f) else GlassBorder
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "R.A.R CORE",
                            color = IndigoLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = { onSpeak(message.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Seslendir",
                                tint = TealAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    color = TextWhite,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun FrostedInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    voiceState: VoiceState,
    onSend: () -> Unit,
    onMicClick: () -> Unit
) {
    FrostedGlassBox(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = GlassSurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mic icon button
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (voiceState == VoiceState.LISTENING) IndigoPrimary else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mikrofon",
                    tint = if (voiceState == VoiceState.LISTENING) Color.White else IndigoLight,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "R.A.R'a komut ver veya sor...",
                        color = TextDim,
                        fontSize = 13.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = IndigoLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                singleLine = true
            )

            // Send icon button
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (value.isNotBlank()) IndigoPrimary else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Gönder",
                    tint = if (value.isNotBlank()) Color.White else TextDim,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
