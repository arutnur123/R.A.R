package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FrostedGlassBox
import com.example.ui.theme.*
import com.example.ui.viewmodel.RARViewModel

@Composable
fun HardwareScreen(
    viewModel: RARViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.systemStats.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val isFocusFilterActive by viewModel.isFocusFilterActive.collectAsState()
    val isGameModeActive by viewModel.isGameModeActive.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .verticalScroll(scrollState)
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
                    text = "HARDWARE & SYSTEM MONITOR",
                    color = IndigoLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Sistem ve Donanım Takibi",
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
                    text = "Canlı Veri",
                    color = TealAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large CPU & RAM Frosted Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CPU Metric
            FrostedGlassBox(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("İŞLEMCİ (CPU)", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Speed, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "%${stats?.cpuPercent ?: 14}",
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ((stats?.cpuPercent ?: 14) / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = IndigoPrimary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }

            // RAM Metric
            FrostedGlassBox(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("BELLEK (RAM)", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Memory, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val ramGb = String.format("%.1f", (stats?.ramUsedMb ?: 3400) / 1024.0)
                    Text(
                        text = "${ramGb} GB",
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ((stats?.ramPercent ?: 48) / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TealAccent,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Battery & Temperature Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Battery
            FrostedGlassBox(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccess.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (stats?.isCharging == true) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Pil Seviyesi", color = TextDim, fontSize = 11.sp)
                        Text(
                            text = "%${stats?.batteryPercent ?: 85} ${if (stats?.isCharging == true) "⚡" else ""}",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Temp & Network
            FrostedGlassBox(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeviceThermostat,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Sıcaklık / Ağ", color = TextDim, fontSize = 11.sp)
                        Text(
                            text = "${stats?.batteryTempCelsius ?: 31}°C (${stats?.networkType ?: "Wi-Fi"})",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Optimization Button
        Button(
            onClick = { viewModel.optimizeSystem() },
            enabled = !isOptimizing,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
        ) {
            if (isOptimizing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sistem Optimize Ediliyor...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sistem Önbelleğini Temizle ve Hızlandır", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart Modes (Focus DND & Game Mode)
        Text(
            text = "AKILLI ASİSTAN MODLARI",
            color = TextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Focus Mode Card
        FrostedGlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = IndigoLight)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Akıllı Bildirim & Odak Filtresi", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Önemsiz bildirimleri sessize alır", color = TextDim, fontSize = 12.sp)
                    }
                }

                Switch(
                    checked = isFocusFilterActive,
                    onCheckedChange = { viewModel.toggleFocusFilter() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IndigoPrimary)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Game Mode Card
        FrostedGlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TealAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = TealAccent)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Oyun Asistanı & FPS Boost", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Maksimum performans & gecikme optimizasyonu", color = TextDim, fontSize = 12.sp)
                    }
                }

                Switch(
                    checked = isGameModeActive,
                    onCheckedChange = { viewModel.toggleGameMode() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealAccent)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
