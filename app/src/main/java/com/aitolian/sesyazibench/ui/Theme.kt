package com.aitolian.sesyazibench.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** V1 · Neon Mor */
object SY {
    val Bg = Color(0xFF0E0B1A)
    val BgTop = Color(0xFF2A1450)
    val Sheet = Color(0xFF17132A)
    val Card = Color(0xFF221C3D)
    val Chip = Color(0xFF2A1F4A)
    val ChipText = Color(0xFFD6C8FF)
    val Text = Color(0xFFF1EEFF)
    val Muted = Color(0xFFA39DC4)
    val A1 = Color(0xFF7C5CFF)
    val A2 = Color(0xFFFF4FD8)
    val Accent = Color(0xFFB69CFF)
    val OnAccent = Color(0xFF12091F)
    val Error = Color(0xFFFF8A9A)
    val Ok = Color(0xFF7EE0B5)
    val AdBg = Color(0xFF0A0814)

    val background = Brush.verticalGradient(0f to BgTop, 0.42f to Bg)
}

@Composable
fun SesYaziTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = SY.Accent, onPrimary = SY.OnAccent, background = SY.Bg, surface = SY.Sheet,
            onSurface = SY.Text, onBackground = SY.Text, surfaceVariant = SY.Card, error = SY.Error,
        ),
        content = content,
    )
}
