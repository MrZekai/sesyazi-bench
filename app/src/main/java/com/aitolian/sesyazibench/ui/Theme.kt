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

/**
 * Koyu (2026): nötr kömür zemin, tek vurgu (indigo-mavi). Mor/neon yok.
 * Metin #ECEEF2 / #16191F ≈ 15:1.
 */
private val Dark = Palette(
    bg = Color(0xFF0F1115), bgTop = Color(0xFF0F1115), sheet = Color(0xFF16191F), card = Color(0xFF1D2128),
    chip = Color(0xFF232833), chipText = Color(0xFFDDE3FF), text = Color(0xFFECEEF2), muted = Color(0xFFA3A9B4),
    accent = Color(0xFF8DA2FF), onAccent = Color(0xFF0B1020), error = Color(0xFFFF8A8A), ok = Color(0xFF6FD6A8),
    adBg = Color(0xFF0C0E12), outline = Color(0xFF2A2F38), track = Color(0xFF353B46), highlight = Color(0x338DA2FF),
)

/**
 * Açık (varsayılan, 2026): kırık beyaz sayfa, beyaz yüzeyler, koyu mürekkep metin,
 * tek vurgu #3B5BDB. Metin #14171C / #FFFFFF ≈ 18:1; ikincil #5B6270 ≈ 6:1;
 * vurgu ≈ 5,9:1 (hesap; cihazda doğrulanmadı).
 */
private val Light = Palette(
    bg = Color(0xFFF5F6F8), bgTop = Color(0xFFF5F6F8), sheet = Color(0xFFFFFFFF), card = Color(0xFFF0F2F5),
    chip = Color(0xFFEEF1F6), chipText = Color(0xFF26324D), text = Color(0xFF14171C), muted = Color(0xFF5B6270),
    accent = Color(0xFF3B5BDB), onAccent = Color(0xFFFFFFFF), error = Color(0xFFC62828), ok = Color(0xFF1B7F52),
    adBg = Color(0xFFF0F2F5), outline = Color(0xFFE3E6EB), track = Color(0xFFD5D9E0), highlight = Color(0x263B5BDB),
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
    /** Marka geçişi (ses dalgası işareti, ilerleme çizgisi): indigo → camgöbeği. */
    val A1 = Color(0xFF4361EE)
    val A2 = Color(0xFF4CC9F0)
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

    /** Düz zemin (eski mor degrade kaldırıldı). */
    val background: Brush get() = Brush.verticalGradient(listOf(Bg, Bg))
}

@Composable
fun isDarkTheme(mode: Int): Boolean = when (mode) {
    THEME_LIGHT -> false
    THEME_DARK -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun SesYaziTheme(mode: Int = THEME_LIGHT, content: @Composable () -> Unit) {
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
