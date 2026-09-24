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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.aitolian.sesyazibench.data.Exports
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
    val noteKey = r?.id ?: s.previousResult?.id
    // Döndürmede korunur; başka nota geçince sıfırlanır
    var editing by rememberSaveable(noteKey) { mutableStateOf(false) }
    var draft by rememberSaveable(noteKey) { mutableStateOf("") }
    var draftBase by rememberSaveable(noteKey) { mutableStateOf("") }
    var showTimes by rememberSaveable(noteKey) { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf<(() -> Unit)?>(null) }
    val font = s.readerFont
    val working = s.phase is Phase.Transcribing || s.phase is Phase.Preparing || s.phase is Phase.Downloading

    /** Kaydedilmemiş düzenleme varsa önce sor. */
    fun leaveEditThen(action: () -> Unit) {
        if (editing && draft != draftBase) confirmDiscard = action
        else { editing = false; action() }
    }

    BackHandler {
        if (editing) leaveEditThen {} else onHome()
    }

    // Uzun metinde paragraflama her oynatma adımında yeniden hesaplanmasın
    val paragraphList = remember(r?.id, r?.segments) { r?.let { paragraphs(it.segments) } ?: emptyList() }
    val translationParas = remember(s.translation) { s.translation?.let { paragraphs(it) } }
    val liveParas = remember(s.live) { paragraphs(s.live) }

    val shownText: String? = when {
        r == null -> null
        s.tab == Tab.TRANSLATION -> s.translation?.joinToString(" ") { it.text }
        else -> r.text
    }

    Column(Modifier.fillMaxSize().background(SY.Bg).statusBarsPadding()) {
        NoteTopBar(
            s, vm, r, working,
            onHome = { leaveEditThen(onHome) },
            showTimes = showTimes, onToggleTimes = { showTimes = !showTimes },
            onDelete = { leaveEditThen(vm::deleteCurrent) },
        )

        // Durum şeritleri: hazırlık / model indirme / canlı döküm / arka plan iyileştirme
        when (val phase = s.phase) {
            is Phase.Preparing -> StatusStrip(phase.message, null, onCancel = vm::cancelWork)
            is Phase.Downloading -> StatusStrip("Model indiriliyor · ${phase.mb} MB", phase.progress, onCancel = vm::cancelWork)
            is Phase.Transcribing -> LiveBar(phase.percent, s.etaSec, onCancel = vm::cancelWork)
            else -> Unit
        }
        s.refining?.let { RefineStrip(it) }

        if (r != null && s.hasAudio) MiniPlayer(s, vm)
        if (r != null && !editing) {
            NoteTabs(s.tab) { t -> if (t == Tab.TRANSLATION) vm.openTranslation() else vm.setTab(t) }
        }

        // Kâğıt
        Box(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp)).background(SY.Sheet)
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
        ) {
            if (r == null) LiveText(liveParas, font) else when {
                editing -> BasicTextField(
                    value = draft, onValueChange = { draft = it },
                    textStyle = TextStyle(color = SY.Text, fontSize = font.sp, lineHeight = (font * 1.55f).sp),
                    cursorBrush = SolidColor(SY.Accent),
                    modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp)
                        .semantics { contentDescription = "Not metni düzenleme alanı" },
                )
                s.tab == Tab.TRANSLATION -> TranslationBody(s, vm, font, translationParas)
                else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                    if (showTimes && r.editedText == null) TimedText(r.segments, s, vm, font)
                    else PaperText(r.editedText?.let { listOf(it) } ?: paragraphList, font)
                    if (s.suggestBest && s.hasAudio && !working) SuggestBest(onRun = vm::rerunWithBest)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "✓ ${"%.1f".format(Locale("tr"), r.processMs / 1000.0)} sn'de cihazda yazıya döküldü" +
                            (if (r.editedText != null) " · düzenlendi" else "") +
                            (if (showTimes && r.editedText != null) " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" else ""),
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
                    BarButton("Vazgeç", Modifier.weight(1f), filled = false) { leaveEditThen {} }
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
                        if (working) { vm.toast("Döküm bitince düzenleyebilirsin"); return@BarAction }
                        vm.setTab(Tab.TEXT)
                        val base = r.editedText ?: paragraphList.joinToString("\n\n")
                        draft = base
                        draftBase = base
                        editing = true
                    }
                    ExportAction(s, r, Modifier.weight(1f))
                }
            }
        }

        // Klavye açıkken reklam yazma alanını daraltmasın
        if (!editing) {
            Box(Modifier.fillMaxWidth().background(SY.AdBg).navigationBarsPadding().padding(vertical = 4.dp)) {
                if (adsReady) BannerAd() else Spacer(Modifier.fillMaxWidth().height(50.dp))
            }
        } else {
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    confirmDiscard?.let { next ->
        AlertDialog(
            onDismissRequest = { confirmDiscard = null },
            containerColor = SY.Sheet, titleContentColor = SY.Text, textContentColor = SY.Muted,
            title = { Text("Değişiklikler kaydedilmedi") },
            text = { Text("Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur.") },
            confirmButton = {
                TextButton(onClick = { vm.saveEdit(draft); editing = false; confirmDiscard = null; next() }) {
                    Text("Kaydet", color = SY.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { editing = false; confirmDiscard = null; next() }) { Text("Kaydetme", color = SY.Error) }
            },
        )
    }
}

/** Dışa aktar: TXT (ekrandaki metin) ya da SRT (zaman damgalı). */
@Composable
private fun ExportAction(s: MainState, r: Transcript, modifier: Modifier) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        BarAction("⤓", "Dışa aktar", Modifier.fillMaxWidth()) { open = true }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            val tr = s.tab == Tab.TRANSLATION
            val suffix = if (tr) "_${s.translationTarget.code}" else ""
            DropdownMenuItem(
                text = { Text(if (tr) "Çeviri metni (.txt)" else "Metin dosyası (.txt)") },
                onClick = {
                    open = false
                    val text = if (tr) s.translation?.let { paragraphs(it).joinToString("\n\n") } else r.editedText ?: paragraphs(r.segments).joinToString("\n\n")
                    if (text == null) toastLater(context, "Çeviri henüz hazır değil")
                    else Exports.shareTxt(context, r, text, suffix)
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        when {
                            tr -> "Çeviri altyazısı (.srt)"
                            r.editedText != null -> "Altyazı (.srt) · orijinal döküm"
                            else -> "Altyazı (.srt)"
                        },
                    )
                },
                onClick = {
                    open = false
                    val segs = if (tr) s.translation else r.segments
                    if (segs == null) toastLater(context, "Çeviri henüz hazır değil")
                    else {
                        val suf = suffix + if (!tr && r.editedText != null) "_orijinal" else ""
                        Exports.shareSrt(context, r, r.copy(segments = segs).toSrt(), suf)
                    }
                },
            )
        }
    }
}

private fun toastLater(context: Context, msg: String) =
    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()

@Composable
private fun NoteTopBar(
    s: MainState, vm: MainViewModel, r: Transcript?, working: Boolean, onHome: () -> Unit,
    showTimes: Boolean, onToggleTimes: () -> Unit, onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        RoundIcon("⌂", "Ana sayfaya dön", onHome)
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(
                r?.let { titleOf(it) } ?: "Yazıya dökülüyor…",
                color = SY.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            val sub = if (r == null) s.fileName ?: "" else
                "${langOf(r.language)?.label ?: r.language} · ${Transcript.clock(r.durationMs)} · ${words(r.text)} kelime"
            Text(sub, color = SY.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        SmallIcon("A−", "Yazıyı küçült") { vm.setReaderFont(s.readerFont - 2) }
        SmallIcon("A+", "Yazıyı büyüt") { vm.setReaderFont(s.readerFont + 2) }
        if (r != null) Box {
            SmallIcon("⋮", "Diğer seçenekler") { menu = true }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(if (showTimes) "Paragraf görünümü" else "Zaman damgalı görünüm") },
                    onClick = { menu = false; onToggleTimes() },
                )
                if (s.hasAudio && !working) {
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
private fun StatusStrip(text: String, progress: Float?, onCancel: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(14.dp), color = SY.Accent, strokeWidth = 2.dp)
            Text(
                text + (progress?.let { " · %${(it * 100).toInt()}" } ?: ""),
                color = SY.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            CancelText(onCancel)
        }
        if (progress != null) ProgressLine(progress)
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
            CancelText(onCancel)
        }
        ProgressLine(percent / 100f)
    }
}

@Composable
private fun CancelText(onCancel: () -> Unit) {
    Text(
        "İptal", color = SY.Accent, fontSize = 13.sp,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = onCancel)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}

@Composable
private fun ProgressLine(p: Float) {
    Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(SY.Card)) {
        Box(
            Modifier.fillMaxWidth(p.coerceIn(0f, 1f)).fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(SY.A1, SY.A2))),
        )
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
            Modifier.size(36.dp).clip(CircleShape).background(SY.Accent)
                .clickable(onClickLabel = if (s.playing) "Duraklat" else "Oynat", role = Role.Button, onClick = vm::togglePlay)
                .semantics { contentDescription = if (s.playing) "Sesi duraklat" else "Sesi oynat" },
            contentAlignment = Alignment.Center,
        ) { Text(if (s.playing) "❚❚" else "▶", color = SY.OnAccent, fontSize = 12.sp) }
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
                    .clickable(role = Role.Tab) { onTab(t) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) { Text(label, fontSize = 13.sp, color = if (sel) SY.OnAccent else SY.Muted, fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable
private fun LiveText(paras: List<String>, font: Int) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        if (paras.isEmpty()) {
            Text("Metin birazdan burada belirecek…", color = SY.Muted, fontSize = font.sp)
        } else {
            PaperText(paras, font)
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

/**
 * Zaman damgalı görünüm: satırın saatine dokun → sese atla (ses henüz
 * çalmadıysa ilk Play o konumdan başlar). Metnin kendisi seçilebilir; seçim
 * ile atlama çakışmasın diye atlama yalnızca saat etiketinde.
 */
@Composable
private fun TimedText(segments: List<Segment>, s: MainState, vm: MainViewModel, font: Int) {
    Column {
        segments.forEach { seg ->
            val current = s.playing && s.positionMs >= seg.startMs && s.positionMs < seg.endMs
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (current) SY.Card else Color.Transparent)
                    .padding(vertical = 4.dp, horizontal = 2.dp),
            ) {
                Text(
                    Transcript.clock(seg.startMs), color = SY.Accent, fontSize = 12.5.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(52.dp).clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = s.hasAudio, onClickLabel = "Buradan dinle", role = Role.Button) { vm.seekTo(seg.startMs) }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                )
                SelectionContainer(Modifier.weight(1f)) {
                    Text(seg.text, color = SY.Text, fontSize = (font - 1).sp, lineHeight = (font * 1.4f).sp,
                        modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun TranslationBody(s: MainState, vm: MainViewModel, font: Int, paras: List<String>?) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    val r = s.result ?: return
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${langOf(r.language)?.label ?: r.language} →", color = SY.Muted, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp))
            Box {
                Pill("${s.translationTarget.label} ▾", bg = SY.Chip, fg = SY.Text, onClick = { open = true })
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    TRANSLATABLE.filter { it.code != r.language }.forEach { l ->
                        DropdownMenuItem(text = { Text(l.label) }, onClick = { open = false; vm.translate(l) })
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        val status = s.translating
        when {
            status != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = SY.Accent, strokeWidth = 2.dp)
                Text(status, color = SY.Muted, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
            }
            paras != null -> PaperText(paras, font)
            else -> Pill("Çevir", bg = SY.Accent, fg = SY.OnAccent, onClick = { vm.translate(s.translationTarget) })
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SY.Card)
                .clickable(role = Role.Button) {
                    if (!openGoogleTranslate(context, r.text, r.language, s.translationTarget.code)) {
                        vm.toast("Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır")
                    }
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Daha doğal çeviri: Google Çeviri'de aç", color = SY.Text, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                Text("Ücretsiz · internet gerekir · metin Google'a gönderilir", color = SY.Muted, fontSize = 11.5.sp)
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
private fun RoundIcon(label: String, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(SY.Card)
            .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { Text(label, fontSize = 19.sp, color = SY.Text) }
}

@Composable
private fun SmallIcon(label: String, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { Text(label, fontSize = 15.sp, color = SY.Text, fontWeight = FontWeight.Medium) }
}

@Composable
private fun BarAction(icon: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).clickable(onClickLabel = label, role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(12.dp)).background(SY.Card), contentAlignment = Alignment.Center) {
            Text(icon, color = SY.Accent, fontSize = 16.sp)
        }
        Text(label, color = SY.Muted, fontSize = 11.sp, maxLines = 1, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun BarButton(label: String, modifier: Modifier, filled: Boolean, onClick: () -> Unit) {
    Box(
        modifier.height(48.dp).clip(CircleShape).background(if (filled) SY.Accent else SY.Card)
            .clickable(role = Role.Button, onClick = onClick),
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
 * Tarayıcı adresi uzunluk sınırı yüzünden metin sığmazsa metni kesmez: tamamını
 * panoya kopyalar, sayfayı boş açar ve false döner (çağıran kullanıcıya söyler).
 */
fun openGoogleTranslate(context: Context, text: String, source: String, target: String): Boolean {
    val app = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage("com.google.android.apps.translate")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(app)
        return true
    } catch (_: ActivityNotFoundException) {
        // Uygulama yok → tarayıcı
    }
    val base = "https://translate.google.com/?sl=$source&tl=$target&op=translate"
    val encoded = Uri.encode(text)
    val fits = encoded.length <= 4_000
    if (!fits) copyText(context, text)
    val url = if (fits) "$base&text=$encoded" else base
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    return fits
}
