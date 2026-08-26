package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FrostedGlassBox
import com.example.ui.theme.*
import com.example.ui.viewmodel.RARViewModel

@Composable
fun PomodoroScreen(
    viewModel: RARViewModel,
    modifier: Modifier = Modifier
) {
    val pomodoroState by viewModel.pomodoroState.collectAsState()
    val history by viewModel.pomodoroHistory.collectAsState()
    val context = LocalContext.current

    val minutes = pomodoroState.timeRemainingSeconds / 60
    val seconds = pomodoroState.timeRemainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val progress = if (pomodoroState.totalDurationSeconds > 0) {
        (pomodoroState.totalDurationSeconds - pomodoroState.timeRemainingSeconds).toFloat() / pomodoroState.totalDurationSeconds
    } else 0f

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FOCUS & PRODUCTIVITY",
                    color = IndigoLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Pomodoro Odak Modu",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Bugün: ${pomodoroState.completedSessionsToday} Seans",
                    color = EmeraldSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Circular Timer Display
        FrostedGlassBox(
            modifier = Modifier.size(240.dp),
            shape = CircleShape,
            backgroundColor = GlassSurfaceElevated
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Circular Progress Indicator
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(210.dp),
                    color = TealAccent,
                    strokeWidth = 10.dp,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        color = TextWhite,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pomodoroState.mode,
                        color = IndigoLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            IconButton(
                onClick = { viewModel.resetPomodoro() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Sıfırla", tint = TextWhite)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Play / Pause
            Button(
                onClick = {
                    if (pomodoroState.isRunning) {
                        viewModel.pausePomodoro()
                    } else {
                        if (pomodoroState.timeRemainingSeconds == pomodoroState.totalDurationSeconds || pomodoroState.timeRemainingSeconds == 0) {
                            viewModel.startPomodoro(25, "Çalışma")
                        } else {
                            viewModel.resumePomodoro()
                        }
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .width(160.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Icon(
                    imageVector = if (pomodoroState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (pomodoroState.isRunning) "Duraklat" else "Başlat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Lofi Stream Launcher
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=jfKfPfyJRdk")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(Icons.Default.Headphones, contentDescription = "Lofi Müzik", tint = TealAccent)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preset Duration Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetButton(label = "25 dk Odak", isSelected = pomodoroState.totalDurationSeconds == 25 * 60, onClick = { viewModel.startPomodoro(25, "Çalışma") }, modifier = Modifier.weight(1f))
            PresetButton(label = "50 dk Derin", isSelected = pomodoroState.totalDurationSeconds == 50 * 60, onClick = { viewModel.startPomodoro(50, "Derin Odak") }, modifier = Modifier.weight(1f))
            PresetButton(label = "5 dk Mola", isSelected = pomodoroState.totalDurationSeconds == 5 * 60, onClick = { viewModel.startPomodoro(5, "Kısa Mola") }, modifier = Modifier.weight(1f))
            PresetButton(label = "15 dk Mola", isSelected = pomodoroState.totalDurationSeconds == 15 * 60, onClick = { viewModel.startPomodoro(15, "Uzun Mola") }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lofi Audio Companion Card
        FrostedGlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/lofi%20study")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try { context.startActivity(intent) } catch (e: Exception) {}
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = IndigoLight)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Spotify Odak Listesi", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Ders çalışırken arka planda çal", color = TextDim, fontSize = 11.sp)
                    }
                }
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun PresetButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) IndigoPrimary else GlassSurface)
            .border(1.dp, if (isSelected) IndigoPrimaryLight else GlassBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextWhite,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
