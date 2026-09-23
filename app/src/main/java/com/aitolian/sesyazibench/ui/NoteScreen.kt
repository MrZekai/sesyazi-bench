package com.aitolian.sesyazibench.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitolian.sesyazibench.MainState
import com.aitolian.sesyazibench.MainViewModel
import com.aitolian.sesyazibench.Phase
import com.aitolian.sesyazibench.Quality
import com.aitolian.sesyazibench.Tab
import com.aitolian.sesyazibench.ads.BannerAd
import com.aitolian.sesyazibench.data.Transcript
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.OnDeviceTranslator
import com.aitolian.sesyazibench.engine.Segment
import com.aitolian.sesyazibench.engine.TRANSLATABLE
import com.aitolian.sesyazibench.engine.langOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Not ekranı: döküm başlar başlamaz otomatik açılan, tam ekran "not defteri".
 * Metin geldikçe akar; bitince okunur, seçilir, düzenlenir, çevrilir, paylaşılır.
 */
@Composable
fun NoteScreen(s: MainState, vm: MainViewModel, adsReady: Boolean, onHome: () -> Unit) {
    val context = LocalContext.current
    val r = s.result
    var editing by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf("") }
    var showTimes by rememberSaveable { mutableStateOf(false) }
    val font = s.readerFont
    // Yeni döküm gelince düzenleme modundan çık
    LaunchedEffect(r?.id) { editing = false; showTimes = false }

    BackHandler {
        if (editing) editing = false else onHome()
    }

    val shownSegs: List<Segment>? = when {
        r == null -> null
        s.tab == Tab.TRANSLATION -> s.translation
        else -> r.segments
    }
    val shownText: String? = when {
        r == null -> null
        s.tab == Tab.TRANSLATION -> s.translation?.joinToString(" ") { it.text }
        else -> r.text
    }

    Column(Modifier.fillMaxSize().background(SY.Bg).statusBarsPadding()) {
        NoteTopBar(s, vm, r, onHome, showTimes, onToggleTimes = { showTimes = !showTimes }, onDelete = vm::deleteCurrent)

        // Canlı ilerleme / arka plan iyileştirme
        val phase = s.phase
        if (phase is Phase.Transcribing) {
            LiveBar(phase.percent, s.etaSec, onCancel = vm::goHome)
        }
        s.refining?.let { RefineStrip(it) }

        if (r != null && (s.hasAudio || s.audioMs > 0)) MiniPlayer(s, vm)
        if (r != null && !editing) {
            NoteTabs(s.tab) { t -> if (t == Tab.TRANSLATION) vm.openTranslation() else vm.setTab(t) }
        }

        // Kâğıt
        Box(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp)).background(SY.Sheet)
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
        ) {
            if (r == null) LiveText(s.live, font) else when {
                editing -> BasicTextField(
                    value = draft, onValueChange = { draft = it },
                    textStyle = TextStyle(color = SY.Text, fontSize = font.sp, lineHeight = (font * 1.55f).sp),
                    cursorBrush = SolidColor(SY.Accent),
                    modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
                )
                s.tab == Tab.TRANSLATION -> TranslationBody(s, vm, font)
                else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                    if (showTimes && r.editedText == null) TimedText(r.segments, s, vm, font)
                    else PaperText(r.editedText?.let { listOf(it) } ?: paragraphs(r.segments), font)
                    if (s.suggestBest && s.hasAudio) SuggestBest(onRun = vm::rerunWithBest)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "✓ ${"%.1f".format(Locale("tr"), r.processMs / 1000.0)} sn'de cihazda yazıya döküldü" +
                            if (r.editedText != null) " · düzenlendi" else "",
                        fontSize = 11.5.sp, color = SY.Muted,
                    )
                }
            }
        }

        // Alt eylem çubuğu
        if (r != null) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (editing) {
                    BarButton("Vazgeç", Modifier.weight(1f), filled = false) { editing = false }
                    BarButton("Kaydet", Modifier.weight(1f), filled = true) { vm.saveEdit(draft); editing = false }
                } else {
                    BarAction("⧉", "Kopyala", Modifier.weight(1f)) {
                        if (shownText == null) vm.toast("Çeviri henüz hazır değil")
                        else { copyText(context, shownText); vm.toast("Kopyalandı") }
                    }
                    BarAction("↗", "Paylaş", Modifier.weight(1f)) {
                        if (shownText == null) vm.toast("Çeviri henüz hazır değil") else shareText(context, shownText)
                    }
                    BarAction("文A", "Çevir", Modifier.weight(1f)) { vm.openTranslation() }
                    BarAction("✎", "Düzenle", Modifier.weight(1f)) {
                        vm.setTab(Tab.TEXT)
                        draft = r.editedText ?: paragraphs(r.segments).joinToString("\n\n")
                        editing = true
                    }
                    BarAction("⤓", "SRT/TXT", Modifier.weight(1f)) {
                        val segs = shownSegs
                        if (segs == null) vm.toast("Çeviri henüz hazır değil")
                        else shareSrt(context, r, segs, if (s.tab == Tab.TRANSLATION) "_${s.translationTarget.code}" else "")
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth().background(SY.AdBg).navigationBarsPadding().padding(vertical = 4.dp)) {
            if (adsReady) BannerAd() else Spacer(Modifier.fillMaxWidth().height(50.dp))
        }
    }
}

@Composable
private fun NoteTopBar(
    s: MainState, vm: MainViewModel, r: Transcript?, onHome: () -> Unit,
    showTimes: Boolean, onToggleTimes: () -> Unit, onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        RoundIcon("⌂", onHome)
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(
                r?.let { titleOf(it) } ?: "Yazıya dökülüyor…",
                color = SY.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            val sub = if (r == null) s.fileName ?: "" else
                "${langOf(r.language)?.label ?: r.language} · ${Transcript.clock(r.durationMs)} · ${words(r.text)} kelime"
            Text(sub, color = SY.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        SmallIcon("A−") { vm.setReaderFont(s.readerFont - 2) }
        SmallIcon("A+") { vm.setReaderFont(s.readerFont + 2) }
        if (r != null) Box {
            SmallIcon("⋮") { menu = true }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(if (showTimes) "Paragraf görünümü" else "Zaman damgalı görünüm") },
                    onClick = { menu = false; onToggleTimes() },
                )
                if (s.hasAudio) {
                    Quality.entries.filter { it != s.quality }.forEach { q ->
                        DropdownMenuItem(
                            text = { Text("${q.label} kalite ile yeniden dök") },
                            onClick = { menu = false; vm.setQuality(q); vm.retranscribe() },
                        )
                    }
                    listOf(Lang.AUTO, Lang.TR, Lang.EN).filter { it.code != r.language && it != s.lang }.forEach { l ->
                        DropdownMenuItem(
                            text = { Text("Dil: ${l.label} ile yeniden dök") },
                            onClick = { menu = false; vm.setLang(l); vm.retranscribe() },
                        )
                    }
                }
                DropdownMenuItem(text = { Text("Bu notu sil", color = SY.Error) }, onClick = { menu = false; onDelete() })
            }
        }
    }
}

@Composable
private fun LiveBar(percent: Int, eta: Int?, onCancel: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(SY.A2))
            Text(
                "Canlı · %$percent" + (eta?.let { " · ~$it sn" } ?: ""),
                color = SY.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            Text(
                "İptal", color = SY.Accent, fontSize = 12.5.sp,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onCancel).padding(6.dp),
            )
        }
        Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(SY.Card)) {
            Box(
                Modifier.fillMaxWidth(percent.coerceIn(0, 100) / 100f).fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(SY.A1, SY.A2))),
            )
        }
    }
}

@Composable
private fun RefineStrip(text: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(SY.A1.copy(alpha = .25f), SY.A2.copy(alpha = .18f))))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(14.dp), color = SY.Accent, strokeWidth = 2.dp)
        Text(text, fontSize = 12.sp, color = SY.Text, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun MiniPlayer(s: MainState, vm: MainViewModel) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(RoundedCornerShape(14.dp))
            .background(SY.Card).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(if (s.hasAudio) SY.Accent else SY.Chip)
                .clickable(enabled = s.hasAudio, onClick = vm::togglePlay),
            contentAlignment = Alignment.Center,
        ) { Text(if (s.playing) "❚❚" else "▶", color = if (s.hasAudio) SY.OnAccent else SY.Muted, fontSize = 11.sp) }
        Waveform(
            s.waveform, if (s.audioMs > 0) s.positionMs.toFloat() / s.audioMs else 0f,
            Modifier.weight(1f).height(22.dp).padding(horizontal = 10.dp),
        )
        Text("${Transcript.clock(s.positionMs)} / ${Transcript.clock(s.audioMs)}", fontSize = 11.5.sp, color = SY.Muted)
    }
}

@Composable
private fun NoteTabs(tab: Tab, onTab: (Tab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(CircleShape).background(SY.Card).padding(3.dp),
    ) {
        listOf(Tab.TEXT to "Metin", Tab.TRANSLATION to "Çeviri").forEach { (t, label) ->
            val sel = t == tab
            Box(
                Modifier.weight(1f).clip(CircleShape).background(if (sel) SY.Accent else Color.Transparent)
                    .clickable { onTab(t) }.padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) { Text(label, fontSize = 13.sp, color = if (sel) SY.OnAccent else SY.Muted, fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable
private fun LiveText(live: List<Segment>, font: Int) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        if (live.isEmpty()) {
            Text("Metin birazdan burada belirecek…", color = SY.Muted, fontSize = font.sp)
        } else {
            PaperText(paragraphs(live), font)
            Text("▍", color = SY.Accent, fontSize = font.sp)
        }
    }
}

@Composable
private fun PaperText(paragraphs: List<String>, font: Int) {
    SelectionContainer {
        Column {
            paragraphs.forEach { p ->
                Text(
                    p, color = SY.Text, fontSize = font.sp, lineHeight = (font * 1.55f).sp,
                    modifier = Modifier.padding(bottom = (font * 0.8f).dp),
                )
            }
        }
    }
}

/** Zaman damgalı görünüm: satıra dokun → sese atla. */
@Composable
private fun TimedText(segments: List<Segment>, s: MainState, vm: MainViewModel, font: Int) {
    SelectionContainer {
        Column {
            segments.forEach { seg ->
                val current = s.playing && s.positionMs >= seg.startMs && s.positionMs < seg.endMs
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(if (current) SY.Card else Color.Transparent)
                        .clickable(enabled = s.hasAudio) { vm.seekTo(seg.startMs) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                ) {
                    Text(
                        Transcript.clock(seg.startMs), color = SY.Accent, fontSize = 12.sp,
                        modifier = Modifier.width(44.dp).padding(top = 3.dp),
                    )
                    Text(seg.text, color = SY.Text, fontSize = (font - 1).sp, lineHeight = (font * 1.4f).sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TranslationBody(s: MainState, vm: MainViewModel, font: Int) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    val r = s.result ?: return
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${langOf(r.language)?.label ?: r.language} →", color = SY.Muted, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp))
            Box {
                Pill("${s.translationTarget.label} ▾", bg = SY.Chip, fg = SY.Text, onClick = { if (s.translating == null) open = true })
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    TRANSLATABLE.filter { it.code != r.language }.forEach { l ->
                        DropdownMenuItem(text = { Text(l.label) }, onClick = { open = false; vm.translate(l) })
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        val status = s.translating
        val tr = s.translation
        when {
            status != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = SY.Accent, strokeWidth = 2.dp)
                Text(status, color = SY.Muted, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
            }
            tr != null -> PaperText(paragraphs(tr), font)
            else -> Pill("Çevir", bg = SY.Accent, fg = SY.OnAccent, onClick = { vm.translate(s.translationTarget) })
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SY.Card)
                .clickable { openGoogleTranslate(context, r.text, r.language, s.translationTarget.code) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Daha doğal çeviri: Google Çeviri'de aç", color = SY.Text, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                Text("Ücretsiz · internet gerekir · metin Google'a gider", color = SY.Muted, fontSize = 11.5.sp)
            }
            Text("↗", color = SY.Accent, fontSize = 17.sp)
        }
        Text(
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir.",
            fontSize = 11.sp, color = SY.Muted, modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun SuggestBest(onRun: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(14.dp))
            .border(1.dp, SY.Accent.copy(alpha = .35f), RoundedCornerShape(14.dp)).background(SY.Card).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Hatalı kelimeler mi var?", fontSize = 13.5.sp, color = SY.Text, fontWeight = FontWeight.Medium)
            Text("\"En iyi\" kalite Türkçede daha doğru, ama daha yavaş.", fontSize = 12.sp, color = SY.Muted)
        }
        Pill("En iyi ile dene", bg = SY.Accent, fg = SY.OnAccent, modifier = Modifier.padding(start = 8.dp), onClick = onRun)
    }
}

@Composable
private fun RoundIcon(label: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(SY.Card).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 19.sp, color = SY.Text)
    }
}

@Composable
private fun SmallIcon(label: String, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 15.sp, color = SY.Text, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BarAction(icon: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(12.dp)).background(SY.Card), contentAlignment = Alignment.Center) {
            Text(icon, color = SY.Accent, fontSize = 16.sp)
        }
        Text(label, color = SY.Muted, fontSize = 11.sp, maxLines = 1, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun BarButton(label: String, modifier: Modifier, filled: Boolean, onClick: () -> Unit) {
    Box(
        modifier.height(46.dp).clip(CircleShape).background(if (filled) SY.Accent else SY.Card).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (filled) SY.OnAccent else SY.Text, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold) }
}

/** Not başlığı: metnin ilk birkaç kelimesi (dosya adı "PTT-2026…opus" gibi anlamsız olduğundan). */
internal fun titleOf(t: Transcript): String {
    val w = t.text.split(Regex("\\s+")).filter { it.isNotBlank() }
    return if (w.isEmpty()) t.fileName else w.take(6).joinToString(" ").trimEnd(',', '.', '!', '?') + if (w.size > 6) "…" else ""
}

internal fun noteDate(t: Transcript): String =
    SimpleDateFormat("d MMM HH:mm", Locale("tr")).format(Date(t.id))

private fun words(t: String) = t.split(Regex("\\s+")).count { it.isNotBlank() }

/** Cümleleri ~3'erli paragraflara böler; not defteri gibi rahat okunur. */
internal fun paragraphs(segs: List<Segment>): List<String> =
    OnDeviceTranslator.toSentences(segs).map { it.text }.chunked(3).map { it.joinToString(" ") }

/**
 * Metni Google Çeviri uygulamasında açar (ücretsiz, API anahtarı yok; çeviri
 * Google'ın uygulamasında yapılır, internet gerekir). Uygulama yoksa tarayıcı.
 */
fun openGoogleTranslate(context: Context, text: String, source: String, target: String) {
    val app = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage("com.google.android.apps.translate")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(app)
    } catch (_: ActivityNotFoundException) {
        val url = "https://translate.google.com/?sl=$source&tl=$target&op=translate&text=" + Uri.encode(text.take(4500))
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
