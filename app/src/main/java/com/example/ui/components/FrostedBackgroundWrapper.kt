package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FrostedBackground
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TealAccent

@Composable
fun FrostedBackgroundWrapper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FrostedBackground)
    ) {
        // Ambient ambient glow spots
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-Left Indigo Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        IndigoPrimary.copy(alpha = 0.28f),
                        IndigoPrimary.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.1f, h * 0.08f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.1f, h * 0.08f),
                radius = w * 0.75f
            )

            // Bottom-Right Teal Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TealAccent.copy(alpha = 0.20f),
                        TealAccent.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.9f, h * 0.92f),
                    radius = w * 0.70f
                ),
                center = Offset(w * 0.9f, h * 0.92f),
                radius = w * 0.70f
            )

            // Mid-Center Soft Violet Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.5f
                ),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = w * 0.5f
            )
        }

        content()
    }
}
