package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted

enum class RARNavigationTab(val title: String, val icon: ImageVector) {
    CORE("Core", Icons.Default.Home),
    HARDWARE("Sistem", Icons.Default.Assessment),
    POMODORO("Odak", Icons.Default.HourglassBottom),
    TASKS("Görev & Not", Icons.Default.FormatListBulleted),
    AI_STUDIO("AI & Kod", Icons.Default.Code)
}

@Composable
fun FrostedNavigationBar(
    currentTab: RARNavigationTab,
    onTabSelected: (RARNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Frosted Glass container with 32dp rounded corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RARNavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(8.dp, CircleShape, spotColor = IndigoPrimary)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary)
                                } else {
                                    Modifier
                                        .clip(CircleShape)
                                        .clickable { onTabSelected(tab) }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) Color.White else TextDim,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
