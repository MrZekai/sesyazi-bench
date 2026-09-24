package com.aitolian.sesyazibench.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Tema tercihi: 0 = sistem, 1 = açık, 2 = koyu. */
const val THEME_SYSTEM = 0
const val THEME_LIGHT = 1
const val THEME_DARK = 2

private class Palette(
    val bg: Color, val bgTop: Color, val sheet: Color, val card: Color, val chip: Color, val chipText: Color,
    val text: Color, val muted: Color, val accent: Color, val onAccent: Color, val error: Color, val ok: Color,
    val adBg: Color, val outline: Color, val track: Color, val highlight: Color,
)

/** Koyu: V1 · Neon Mor (ana metin #F1EEFF / #17132A ≈ 15,8:1). */
private val Dark = Palette(
    bg = Color(0xFF0E0B1A), bgTop = Color(0xFF2A1450), sheet = Color(0xFF17132A), card = Color(0xFF221C3D),
    chip = Color(0xFF2A1F4A), chipText = Color(0xFFD6C8FF), text = Color(0xFFF1EEFF), muted = Color(0xFFA39DC4),
    accent = Color(0xFFB69CFF), onAccent = Color(0xFF12091F), error = Color(0xFFFF8A9A), ok = Color(0xFF7EE0B5),
    adBg = Color(0xFF0A0814), outline = Color(0x14FFFFFF), track = Color(0x30FFFFFF), highlight = Color(0x40B69CFF),
)

/**
 * Açık: aynı mor kimlik, kâğıt beyazı zemin. Metin #1A1530 / #FFFFFF ≈ 17:1,
 * ikincil #5B5575 ≈ 7:1, vurgu #5B3FD9 ≈ 6,6:1 (hesap; cihazda doğrulanmadı).
 */
private val Light = Palette(
    bg = Color(0xFFF6F4FB), bgTop = Color(0xFFE9E2FB), sheet = Color(0xFFFFFFFF), card = Color(0xFFEFEBF8),
    chip = Color(0xFFE7E1F7), chipText = Color(0xFF3B2A7A), text = Color(0xFF1A1530), muted = Color(0xFF5B5575),
    accent = Color(0xFF5B3FD9), onAccent = Color(0xFFFFFFFF), error = Color(0xFFC0263F), ok = Color(0xFF1B7F52),
    adBg = Color(0xFFEDE9F6), outline = Color(0x1F1A1530), track = Color(0x2E1A1530), highlight = Color(0x335B3FD9),
)

/**
 * Renkler. Değerler seçili temaya göre döner; tema değişince okuyan her
 * composable (ve Canvas çizimi) kendiliğinden yenilenir.
 */
object SY {
    internal var dark by mutableStateOf(true)
    private val p: Palette get() = if (dark) Dark else Light

    val Bg get() = p.bg
    val BgTop get() = p.bgTop
    val Sheet get() = p.sheet
    val Card get() = p.card
    val Chip get() = p.chip
    val ChipText get() = p.chipText
    val Text get() = p.text
    val Muted get() = p.muted
    val A1 = Color(0xFF7C5CFF)
    val A2 = Color(0xFFFF4FD8)
    val Accent get() = p.accent
    val OnAccent get() = p.onAccent
    val Error get() = p.error
    val Ok get() = p.ok
    val AdBg get() = p.adBg
    /** İnce kenarlık. */
    val Outline get() = p.outline
    /** Pasif dalga çubukları / ilerleme izi. */
    val Track get() = p.track
    /** Okunan cümle vurgusu. */
    val Highlight get() = p.highlight

    val background: Brush get() = Brush.verticalGradient(0f to BgTop, 0.42f to Bg)
}

@Composable
fun isDarkTheme(mode: Int): Boolean = when (mode) {
    THEME_LIGHT -> false
    THEME_DARK -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun SesYaziTheme(mode: Int = THEME_SYSTEM, content: @Composable () -> Unit) {
    val dark = isDarkTheme(mode)
    // Alt ağaç okumadan önce yazılır (aynı kompozisyonda ileri yazım)
    if (SY.dark != dark) SY.dark = dark
    val scheme = if (dark) {
        darkColorScheme(
            primary = SY.Accent, onPrimary = SY.OnAccent, background = SY.Bg, surface = SY.Sheet,
            onSurface = SY.Text, onBackground = SY.Text, surfaceVariant = SY.Card, error = SY.Error,
            surfaceContainer = SY.Sheet, surfaceContainerHigh = SY.Card, surfaceContainerLow = SY.Sheet,
        )
    } else {
        lightColorScheme(
            primary = SY.Accent, onPrimary = SY.OnAccent, background = SY.Bg, surface = SY.Sheet,
            onSurface = SY.Text, onBackground = SY.Text, surfaceVariant = SY.Card, error = SY.Error,
            surfaceContainer = SY.Sheet, surfaceContainerHigh = SY.Card, surfaceContainerLow = SY.Sheet,
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
