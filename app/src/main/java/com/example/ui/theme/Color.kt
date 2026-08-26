package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Frosted Glass Core Colors
val FrostedBackground = Color(0xFF050507)
val CanvasBackground = FrostedBackground
val FrostedIndigoAmbient = Color(0x336366F1)
val FrostedTealAmbient = Color(0x2614B8A6)
val GlowIndigo = FrostedIndigoAmbient
val GlowTeal = FrostedTealAmbient

val IndigoPrimary = Color(0xFF6366F1)
val IndigoLight = Color(0xFF818CF8)
val IndigoPrimaryLight = IndigoLight
val IndigoContainer = Color(0xFF1E1B4B)

val TealAccent = Color(0xFF2DD4BF)
val TealContainer = Color(0xFF134E4A)

val EmeraldSuccess = Color(0xFF34D399)
val AmberWarning = Color(0xFFFBBF24)
val AmberAlert = AmberWarning
val RoseError = Color(0xFFFB7185)
val RoseDanger = RoseError

// Glass surfaces and borders
val GlassSurface = Color(0x14FFFFFF) // 8% white
val GlassSurfaceElevated = Color(0x24FFFFFF) // 14% white
val GlassSurfaceCard = Color(0x1AFFFFFF) // 10% white
val GlassBorder = Color(0x2EFFFFFF) // 18% white
val GlassBorderSubtle = Color(0x17FFFFFF) // 9% white
val GlassBorderActive = Color(0x596366F1) // 35% indigo border

// Dark surfaces
val DarkBackground = FrostedBackground
val DarkSurface = Color(0xFF0D0E15)
val DarkSurfaceElevated = Color(0xFF141622)
val DarkSurfaceCard = Color(0xFF182234)
val DarkBorder = GlassBorder

// Text Colors
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0x99FFFFFF) // 60% white
val TextDim = Color(0x66FFFFFF) // 40% white
val TextSubtle = TextDim
val TextPrimary = TextWhite
val TextSecondary = TextMuted
val TextTertiary = TextDim

// Legacy aliases
val CyanPrimary = IndigoLight
val NeonViolet = IndigoPrimary
val ElectricEmerald = TealAccent
val OrbGlowCyan = Color(0x6600E5FF)
val OrbGlowViolet = Color(0x668B5CF6)
