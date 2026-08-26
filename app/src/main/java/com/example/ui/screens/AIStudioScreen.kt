package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FrostedGlassBox
import com.example.ui.theme.*
import com.example.ui.viewmodel.RARViewModel

@Composable
fun AIStudioScreen(
    viewModel: RARViewModel,
    modifier: Modifier = Modifier
) {
    val aiResponse by viewModel.aiStudioResponse.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()
    val context = LocalContext.current

    var selectedToolType by remember { mutableStateOf("CODE") } // CODE, STUDY, SOLVER, PATCH
    var promptInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val toolOptions = listOf(
        Pair("CODE", "💻 Kod Yazarı"),
        Pair("STUDY", "📚 Ders & Sınav"),
        Pair("SOLVER", "🧠 Adım Adım Soru Çözücü"),
        Pair("PATCH", "🎮 Oyun Asistanı")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Column {
            Text(
                text = "DEVELOPER & STUDY LAB",
                color = IndigoLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "R.A.R Yapay Zeka Stüdyosu",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tool Mode Selector Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(toolOptions) { (key, label) ->
                val isSelected = selectedToolType == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) IndigoPrimary else GlassSurface)
                        .border(1.dp, if (isSelected) IndigoPrimaryLight else GlassBorder, RoundedCornerShape(16.dp))
                        .clickable { selectedToolType = key }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Input Prompt Card
        FrostedGlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val placeholder = when (selectedToolType) {
                    "CODE" -> "İstediğin fonksiyonu, algoritmayı veya hata logunu yaz (Örn: 'Jetpack Compose ile animasyonlu kart')..."
                    "STUDY" -> "Çalışmak istediğin konuyu veya ders sorusunu yaz (Örn: 'Türev kuralları ve örnek soru')..."
                    "SOLVER" -> "Çözülmesini istediğin matematik/mantık problemini gir..."
                    else -> "Oyun stratejisi veya sistem optimizasyonu sor (Örn: 'Valorant FPS artırma & sensitivity')..."
                }

                TextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text(placeholder, color = TextDim, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = IndigoLight
                    ),
                    minLines = 3,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            viewModel.queryAIStudio(promptInput, selectedToolType)
                        }
                    },
                    enabled = !isAILoading && promptInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    if (isAILoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yapay Zeka Analiz Ediyor...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Üret ve Açıkla", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Response Output Card
        if (aiResponse.isNotBlank()) {
            FrostedGlassBox(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.9f),
                borderColor = IndigoPrimary.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("R.A.R Çıktısı", color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Copy to clipboard
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("RAR Code", aiResponse))
                                    viewModel.speakText("Kod panoya kopyalandı.")
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = TextWhite, modifier = Modifier.size(16.dp))
                            }

                            // Read aloud
                            IconButton(
                                onClick = { viewModel.speakText(aiResponse.take(200)) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Seslendir", tint = IndigoLight, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = aiResponse,
                        color = TextWhite,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontFamily = if (selectedToolType == "CODE") FontFamily.Monospace else FontFamily.Default
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
