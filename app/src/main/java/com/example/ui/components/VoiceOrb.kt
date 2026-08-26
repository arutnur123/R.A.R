package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.voice.VoiceState
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TextWhite

@Composable
fun VoiceOrb(
    voiceState: VoiceState,
    audioLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "frosted_orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val audioMultiplier = if (voiceState == VoiceState.LISTENING) (1f + audioLevel * 0.45f) else 1f
    val currentScale = pulseScale * audioMultiplier

    val statusText = when (voiceState) {
        VoiceState.LISTENING -> "LISTENING"
        VoiceState.THINKING -> "THINKING"
        VoiceState.SPEAKING -> "SPEAKING"
        VoiceState.IDLE -> "R.A.R ACTIVE"
    }

    val statusColor = when (voiceState) {
        VoiceState.LISTENING -> IndigoPrimary
        VoiceState.THINKING -> Color(0xFF8B5CF6)
        VoiceState.SPEAKING -> EmeraldSuccess
        VoiceState.IDLE -> IndigoPrimary
    }

    Box(
        modifier = modifier
            .size(190.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Ambient Neon Glow Behind Orb
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(currentScale)
                .blur(28.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            IndigoPrimary.copy(alpha = 0.45f),
                            TealAccent.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Outer Frosted Glass Orb (160dp)
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(16.dp, CircleShape, spotColor = IndigoPrimary.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(GlassSurface)
                .border(1.dp, GlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Dashed orbiting tech ring
            Canvas(modifier = Modifier.size(120.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2

                drawCircle(
                    color = IndigoLight.copy(alpha = 0.4f),
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), rotationAngle)
                    )
                )
            }

            // Core Glowing Orb (80dp)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(currentScale)
                    .shadow(20.dp, CircleShape, spotColor = IndigoPrimary)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                IndigoPrimary,
                                Color(0xFF4F46E5),
                                TealAccent
                            )
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (voiceState) {
                    VoiceState.LISTENING -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Dinliyor",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    VoiceState.THINKING -> {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Düşünüyor",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    VoiceState.SPEAKING -> {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Konuşuyor",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    VoiceState.IDLE -> {
                        // White central core dot with glowing ring
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }
        }

        // Frosted Status Pill Badge at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(statusColor)
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = statusText,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
