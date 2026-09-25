package com.aitolian.sesyazibench.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitolian.sesyazibench.MainState
import com.aitolian.sesyazibench.MainViewModel
import com.aitolian.sesyazibench.Phase
import com.aitolian.sesyazibench.Quality
import com.aitolian.sesyazibench.Tab
import com.aitolian.sesyazibench.isCloud
import com.aitolian.sesyazibench.ads.BannerAd
import com.aitolian.sesyazibench.data.Exports
import com.aitolian.sesyazibench.data.Transcript
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.Sentences
import com.aitolian.sesyazibench.engine.Segment
import com.aitolian.sesyazibench.engine.TRANSLATABLE
import com.aitolian.sesyazibench.engine.langOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

/** Satır aralığı çarpanları: sıkı / normal / geniş. */
private val LINE_MUL = floatArrayOf(1.35f, 1.55f, 1.8f)

/** Okuma alanının yatay boşluğu (toplam; eskiden 12 + 20 = 32 dp). */
private val READ_PAD = 20.dp

/**
 * Not ekranı: döküm başlar başlamaz otomatik açılan, tam ekran okuyucu.
 * Üstte yalnızca Geri · başlık · Aa · ⋮; altta Kopyala · Paylaş · Diğer.
 * Metin geldikçe akar ve (kullanıcı yukarı kaydırmadıkça) takip edilir;
 * ses çalarken okunan cümle vurgulanır.
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
    var showReader by rememberSaveable { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf<(() -> Unit)?>(null) }
    val font = s.readerFont
    val lineMul = LINE_MUL[s.readerLine.coerceIn(0, 2)]
    val working = s.phase is Phase.Transcribing || s.phase is Phase.Preparing || s.phase is Phase.Downloading

    /** Kaydedilmemiş düzenleme varsa önce sor. */
    fun leaveEditThen(action: () -> Unit) {
        if (editing && draft != draftBase) confirmDiscard = action
        else { editing = false; action() }
    }

    fun startEdit() {
        val cur = s.result ?: return
        if (working) { vm.toast("Döküm bitince düzenleyebilirsin"); return }
        vm.setTab(Tab.TEXT)
        val base = cur.editedText ?: paragraphs(cur.segments).joinToString("\n\n")
        draft = base
        draftBase = base
        editing = true
    }

    BackHandler {
        if (editing) leaveEditThen {} else onHome()
    }

    // Uzun metinde paragraflama her oynatma adımında yeniden hesaplanmasın
    val blocks = remember(r?.id, r?.segments) { r?.let { paragraphBlocks(it.segments) } ?: emptyList() }
    val sentences = remember(blocks) { blocks.flatten() }
    val translationParas = remember(s.translation) { s.translation?.let { paragraphs(it) } }
    val liveParas = remember(s.live) { paragraphs(s.live) }

    val shownText: String? = when {
        r == null -> null
        s.tab == Tab.TRANSLATION -> s.translation?.joinToString(" ") { it.text }
        else -> r.text
    }

    Column(Modifier.fillMaxSize().background(SY.Sheet).statusBarsPadding()) {
        NoteTopBar(
            s, vm, r, working,
            onHome = { leaveEditThen(onHome) },
            onReader = { showReader = true },
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

        if (r != null && s.hasAudio && !editing) MiniPlayer(s, vm)
        if (r != null && !editing) {
            NoteTabs(s.tab) { t -> if (t == Tab.TRANSLATION) vm.openTranslation() else vm.setTab(t) }
        }

        // Okuma alanı: düz yüzey, kenarlarda toplam 20 dp
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (r == null) LiveText(liveParas, s.livePartial, font, lineMul) else when {
                editing -> BasicTextField(
                    value = draft, onValueChange = { draft = it },
                    textStyle = TextStyle(color = SY.Text, fontSize = font.sp, lineHeight = (font * lineMul).sp),
                    cursorBrush = SolidColor(SY.Accent),
                    modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
                        .padding(horizontal = READ_PAD, vertical = 12.dp)
                        .semantics { contentDescription = "Not metni düzenleme alanı" },
                )
                s.tab == Tab.TRANSLATION -> TranslationBody(s, vm, font, lineMul, translationParas)
                else -> key(r.id, showTimes) {
                    val timed = showTimes && r.editedText == null
                    val synced = s.hasAudio && r.editedText == null && (s.playing || s.positionMs > 0)
                    val active: Int? = when {
                        !synced -> null
                        timed -> activeIndex(r.segments, s.positionMs)
                        else -> activeIndex(sentences, s.positionMs)
                    }
                    // Takip bloğu: zamanlı görünümde satır, paragraf görünümünde paragraf
                    val pos: Pair<Int, Int>? = if (timed || active == null) null else blockOf(blocks, active)
                    val activeBlock: Int? = if (timed) active else pos?.first
                    // Paragraf içinde okunan cümle: uzun paragrafta ekran cümleyi izlesin
                    val activeLocal: Int? = pos?.second
                    AudioFollowPane(activeBlock, activeLocal, s.playing, s.followAudio) { tracker ->
                        when {
                            timed -> TimedText(r.segments, s, vm, font, lineMul, active, tracker)
                            r.editedText != null -> PaperText(listOf(r.editedText), font, lineMul)
                            else -> SyncedParagraphs(blocks, active, font, lineMul, tracker)
                        }
                        if (s.suggestBest && s.hasAudio && !working && s.refining == null) SuggestBest(onRun = vm::refineWithBest)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            processLine(r) +
                                (if (r.editedText != null) " · düzenlendi" else "") +
                                (if (showTimes && r.editedText != null) " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" else ""),
                            fontSize = 12.sp, color = SY.Muted, lineHeight = 16.sp,
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }

        // Alt işlemler: Kopyala · Paylaş · Diğer
        if (r != null) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (editing) {
                    BarButton("Vazgeç", Modifier.weight(1f), filled = false) { leaveEditThen {} }
                    BarButton("Kaydet", Modifier.weight(1f), filled = true) { vm.saveEdit(draft); editing = false }
                } else {
                    // Çeviri sekmesinde çeviri hazır değilken kopyalanacak/paylaşılacak metin yok
                    val ready = !shownText.isNullOrBlank()
                    BarAction(Icons.Filled.ContentCopy, "Kopyala", Modifier.weight(1f), enabled = ready) {
                        shownText?.let { copyText(context, it); vm.toast("Kopyalandı") }
                    }
                    BarAction(Icons.Filled.Share, "Paylaş", Modifier.weight(1f), enabled = ready) {
                        shownText?.let { shareText(context, it) }
                    }
                    MoreAction(s, r, Modifier.weight(1f), onEdit = ::startEdit)
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

    if (showReader) {
        ReaderSheet(
            s, vm,
            showTimes = if (r != null && r.editedText == null) showTimes else null,
            onShowTimes = { showTimes = it },
            onDismiss = { showReader = false },
        )
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

// ---------------------------------------------------------------------------
// Üst çubuk: Geri → kısa başlık → Aa → ⋮
// ---------------------------------------------------------------------------

@Composable
private fun NoteTopBar(
    s: MainState, vm: MainViewModel, r: Transcript?, working: Boolean,
    onHome: () -> Unit, onReader: () -> Unit, onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTap(Icons.AutoMirrored.Filled.ArrowBack, "Ana sayfaya dön", onClick = onHome)
        Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
            Text(
                r?.let { titleOf(it) } ?: "Yazıya dökülüyor…",
                color = SY.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            val sub = if (r == null) s.fileName ?: "" else
                "${langOf(r.language)?.label ?: r.language} · ${Transcript.clock(r.durationMs)} · ${words(r.text)} kelime"
            Text(sub, color = SY.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconTap(Icons.Filled.FormatSize, "Okuma ayarları (yazı boyutu, satır aralığı, tema)", onClick = onReader)
        if (r != null) Box {
            IconTap(Icons.Filled.MoreVert, "Diğer seçenekler") { menu = true }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                if (s.hasAudio && !working) {
                    // En iyi: mevcut metin kalır, büyük model doğrudan bir kez çalışır (ön izleme tekrarlanmaz)
                    if (r.quality != Quality.BEST.name && s.refining == null) {
                        DropdownMenuItem(
                            text = { Text("En iyi kalite ile iyileştir") },
                            onClick = { menu = false; vm.refineWithBest() },
                        )
                    }
                    if (vm.cloudAvailable && !r.isCloud()) {
                        DropdownMenuItem(
                            text = { Text("⚡ Hızlı (internet) ile yeniden dök") },
                            onClick = { menu = false; vm.setEngineMode(1); vm.retranscribe() },
                        )
                    }
                    Quality.entries.filter { it != Quality.BEST && it.name != r.quality }.forEach { q ->
                        DropdownMenuItem(
                            text = { Text("${q.label} kalite ile telefonda yeniden dök") },
                            onClick = { menu = false; vm.retranscribeLocal(q) },
                        )
                    }
                    (listOf(Lang.AUTO) + TRANSLATABLE).filter { it.code != r.language && it != s.lang }.forEach { l ->
                        DropdownMenuItem(
                            text = { Text("Dil: ${l.label} ile yeniden dök") },
                            onClick = { menu = false; vm.retranscribeInLanguage(l) },
                        )
                    }
                    if (vm.prefs.devMode && s.refining == null) {
                        listOf(false, true).forEach { q8 ->
                            listOf(true, false).forEach { fallback ->
                                DropdownMenuItem(
                                    text = { Text("Test: Turbo ${if (q8) "q8" else "q5"} · tekrar deneme ${if (fallback) "açık" else "kapalı"}") },
                                    onClick = { menu = false; vm.rerunBestExperiment(q8, fallback) },
                                )
                            }
                        }
                    }
                }
                DropdownMenuItem(text = { Text("Bu notu sil", color = SY.Error) }, onClick = { menu = false; onDelete() })
            }
        }
    }
}

// ---------------------------------------------------------------------------
// "Aa" okuma paneli: yazı boyutu, satır aralığı, görünüm, sesle takip, tema
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSheet(
    s: MainState, vm: MainViewModel, showTimes: Boolean?, onShowTimes: (Boolean) -> Unit, onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SY.Sheet, contentColor = SY.Text,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Okuma ayarları", color = SY.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            SheetLabel("Yazı boyutu")
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepButton("A−", "Yazıyı küçült", enabled = s.readerFont > 14) { vm.setReaderFont(s.readerFont - 1) }
                Text(
                    "Örnek metin · ${s.readerFont}",
                    color = SY.Text, fontSize = s.readerFont.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                )
                StepButton("A+", "Yazıyı büyüt", enabled = s.readerFont < 30) { vm.setReaderFont(s.readerFont + 1) }
            }

            SheetLabel("Satır aralığı")
            Segmented(listOf("Sıkı", "Normal", "Geniş"), s.readerLine) { vm.setReaderLine(it) }

            if (showTimes != null) {
                SheetLabel("Görünüm")
                Segmented(listOf("Paragraf", "Zaman damgalı"), if (showTimes) 1 else 0) { onShowTimes(it == 1) }
            }

            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Switch) { vm.setFollowAudio(!s.followAudio) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Sesle takip", color = SY.Text, fontSize = 15.sp)
                    Text("Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur.", color = SY.Muted, fontSize = 12.5.sp)
                }
                Switch(
                    checked = s.followAudio, onCheckedChange = { vm.setFollowAudio(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = SY.Accent, checkedThumbColor = SY.OnAccent),
                )
            }

            SheetLabel("Tema")
            Segmented(listOf("Sistem", "Açık", "Koyu"), s.themeMode) { vm.setThemeMode(it) }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun SheetLabel(text: String) =
    Text(text, color = SY.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 6.dp))

@Composable
private fun StepButton(label: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(SY.Card)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (enabled) SY.Text else SY.Muted.copy(alpha = .5f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
}

// ---------------------------------------------------------------------------
// Durum şeritleri
// ---------------------------------------------------------------------------

@Composable
private fun StatusStrip(text: String, progress: Float?, onCancel: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(14.dp), color = SY.Accent, strokeWidth = 2.dp)
            Text(
                text + (progress?.let { " · %${(it * 100).toInt()}" } ?: ""),
                color = SY.Muted, fontSize = 12.5.sp, modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            CancelText(onCancel)
        }
        if (progress != null) ProgressLine(progress)
    }
}

@Composable
private fun LiveBar(percent: Int, eta: Int?, onCancel: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(SY.A2))
            Text(
                if (percent < 0) "Canlı · ⚡ Hızlı mod" else "Canlı · %$percent" + (eta?.let { " · ~$it sn" } ?: ""),
                color = SY.Muted, fontSize = 12.5.sp, modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            CancelText(onCancel)
        }
        if (percent >= 0) ProgressLine(percent / 100f)
    }
}

@Composable
private fun CancelText(onCancel: () -> Unit) {
    Box(
        Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = onCancel)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text("İptal", color = SY.Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
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
        Text(text, fontSize = 12.5.sp, color = SY.Text, modifier = Modifier.padding(start = 10.dp))
    }
}

// ---------------------------------------------------------------------------
// Oynatıcı: kompakt satır; isteyen genişletip sürgü ve ±5 sn kullanır
// ---------------------------------------------------------------------------

@Composable
private fun MiniPlayer(s: MainState, vm: MainViewModel) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val progress = if (s.audioMs > 0) s.positionMs.toFloat() / s.audioMs else 0f
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp).clip(RoundedCornerShape(16.dp)).background(SY.Card),
    ) {
        Row(Modifier.fillMaxWidth().padding(end = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTap(
                if (s.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                if (s.playing) "Sesi duraklat" else "Sesi oynat",
                tint = SY.OnAccent, bg = SY.Accent, onClick = vm::togglePlay,
            )
            Waveform(
                s.waveform, progress,
                Modifier.weight(1f).height(28.dp).padding(horizontal = 6.dp)
                    .semantics { contentDescription = "Ses dalgası; dokunduğun yere atlar" },
                onSeek = { f -> if (s.audioMs > 0) vm.seekTo((f * s.audioMs).toLong()) },
            )
            Text("${Transcript.clock(s.positionMs)} / ${Transcript.clock(s.audioMs)}", fontSize = 12.sp, color = SY.Muted)
            IconTap(
                if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                if (expanded) "Oynatıcıyı daralt" else "Oynatıcıyı genişlet",
                tint = SY.Muted,
            ) { expanded = !expanded }
        }
        if (expanded && s.audioMs > 0) {
            var dragging by remember { mutableStateOf(false) }
            var dragValue by remember { mutableFloatStateOf(0f) }
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                SkipButton("−5 sn", "5 saniye geri") { vm.seekTo(s.positionMs - 5_000) }
                Slider(
                    value = if (dragging) dragValue else progress.coerceIn(0f, 1f),
                    onValueChange = { dragging = true; dragValue = it },
                    onValueChangeFinished = {
                        if (dragging) vm.seekTo((dragValue * s.audioMs).toLong())
                        dragging = false
                    },
                    colors = SliderDefaults.colors(thumbColor = SY.Accent, activeTrackColor = SY.Accent, inactiveTrackColor = SY.Track),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                )
                SkipButton("+5 sn", "5 saniye ileri") { vm.seekTo(s.positionMs + 5_000) }
            }
        }
    }
}

@Composable
private fun SkipButton(label: String, description: String, onClick: () -> Unit) {
    Box(
        Modifier.heightIn(min = 48.dp).width(56.dp).clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { Text(label, color = SY.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun NoteTabs(tab: Tab, onTab: (Tab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(CircleShape).background(SY.Card).padding(3.dp),
    ) {
        listOf(Tab.TEXT to "Metin", Tab.TRANSLATION to "Çeviri").forEach { (t, label) ->
            val sel = t == tab
            Box(
                Modifier.weight(1f).heightIn(min = 40.dp).clip(CircleShape).background(if (sel) SY.Accent else Color.Transparent)
                    .clickable(role = Role.Tab) { onTab(t) },
                contentAlignment = Alignment.Center,
            ) { Text(label, fontSize = 14.sp, color = if (sel) SY.OnAccent else SY.Text, fontWeight = FontWeight.Medium) }
        }
    }
}

// ---------------------------------------------------------------------------
// Canlı döküm: kesinleşen metin sabit, ara metin soluk; kontrollü otomatik takip
// ---------------------------------------------------------------------------

@Composable
private fun LiveText(paras: List<String>, partial: String?, font: Int, lineMul: Float) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var follow by remember { mutableStateOf(true) }
    // Kullanıcı sürüklerse takip durur
    LaunchedEffect(scroll) {
        scroll.interactionSource.interactions.collect { if (it is DragInteraction.Start) follow = false }
    }
    // Kullanıcı en alta kendisi dönüp bıraktıysa takip yeniden başlar
    LaunchedEffect(scroll) {
        snapshotFlow { !scroll.isScrollInProgress && scroll.maxValue != Int.MAX_VALUE && scroll.value >= scroll.maxValue - 24 }
            .collect { atBottom -> if (atBottom) follow = true }
    }
    // Yeni metin geldikçe (içerik uzadıkça) en alta kay
    LaunchedEffect(scroll) {
        snapshotFlow { scroll.maxValue }.collect { m ->
            if (follow && m != Int.MAX_VALUE && scroll.value < m) scrollSafely(scroll, m)
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = READ_PAD, vertical = 12.dp)) {
            if (paras.isEmpty() && partial == null) {
                Text("Metin birazdan burada belirecek…", color = SY.Muted, fontSize = font.sp)
            } else {
                if (paras.isNotEmpty()) PaperText(paras, font, lineMul)
                // Hızlı modun henüz kesinleşmemiş ara metni: hafif farklı tonda
                if (partial != null) {
                    Text(partial, color = SY.Text.copy(alpha = .62f), fontSize = font.sp, lineHeight = (font * lineMul).sp)
                }
                Text("▍", color = SY.Accent, fontSize = font.sp)
            }
            Spacer(Modifier.height(56.dp)) // "Canlıya dön" düğmesi son satırı örtmesin
        }
        if (!follow) {
            Pill(
                "Canlıya dön", bg = SY.Accent, fg = SY.OnAccent, icon = Icons.Filled.ArrowDownward,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                onClick = {
                    follow = true
                    scope.launch { scrollSafely(scroll, scroll.maxValue) }
                },
            )
        }
    }
}

/** Kaydırma animasyonu kullanıcı dokunuşuyla kesilirse etkiyi sonlandırmadan devam eder. */
private suspend fun scrollSafely(scroll: ScrollState, to: Int) {
    try {
        scroll.animateScrollTo(to.coerceIn(0, scroll.maxValue))
    } catch (e: CancellationException) {
        currentCoroutineContext().ensureActive() // gerçek iptalse yukarı ilet
    }
}

// ---------------------------------------------------------------------------
// Sesle senkron okuma
// ---------------------------------------------------------------------------

/** Kaydırılan alanın ve blokların (paragraf/satır) ekrandaki yerleri. */
private class BlockTracker {
    var viewport: LayoutCoordinates? = null
    val blocks = HashMap<Int, LayoutCoordinates>()
    /** Paragraf metin yerleşimi ve içindeki cümlelerin başlangıç karakterleri. */
    val layouts = HashMap<Int, TextLayoutResult>()
    val starts = HashMap<Int, IntArray>()

    /**
     * Okunan yer (blok; paragrafta [local] cümlenin satırı) rahat okuma bandının
     * dışındaysa kaydırılacak yeni konum; içindeyse null.
     */
    fun target(i: Int, local: Int?, scroll: ScrollState, force: Boolean): Int? {
        val v = viewport?.takeIf { it.isAttached } ?: return null
        val c = blocks[i]?.takeIf { it.isAttached } ?: return null
        var y = v.localPositionOf(c, Offset.Zero).y
        val lay = layouts[i]
        val st = starts[i]
        if (local != null && lay != null && st != null && local in st.indices) {
            val off = st[local].coerceIn(0, lay.layoutInput.text.length)
            y += lay.getLineTop(lay.getLineForOffset(off))
        }
        val h = v.size.height.toFloat()
        if (!force && y >= h * 0.08f && y <= h * 0.55f) return null
        return (scroll.value + y - h * 0.18f).roundToInt().coerceIn(0, scroll.maxValue)
    }
}

/**
 * Okuma alanı. Ses çalarken [activeBlock] rahat okuma bandından çıkarsa ekran
 * onu izler; kullanıcı elle kaydırınca takip durur ve "Okunan yere dön" çıkar.
 */
@Composable
private fun AudioFollowPane(
    activeBlock: Int?, activeLocal: Int?, playing: Boolean, followAudio: Boolean,
    content: @Composable ColumnScope.(BlockTracker) -> Unit,
) {
    val scroll = rememberScrollState()
    val tracker = remember { BlockTracker() }
    val scope = rememberCoroutineScope()
    var hold by remember { mutableStateOf(false) }
    LaunchedEffect(scroll) {
        scroll.interactionSource.interactions.collect { if (it is DragInteraction.Start) hold = true }
    }
    // Oynat'a yeniden basınca takip kaldığı yerden sürer
    LaunchedEffect(playing) { if (playing) hold = false }
    // Sessiz aralıklarda (etkin cümle yok) "Okunan yere dön" yanıp sönmesin
    // (durum değil: çalarken ekran zaten konum adımlarıyla yenileniyor)
    val last = remember { IntArray(2) { -1 } }
    if (activeBlock != null) { last[0] = activeBlock; last[1] = activeLocal ?: -1 }
    LaunchedEffect(activeBlock, activeLocal, playing, followAudio, hold) {
        if (activeBlock == null || !playing || !followAudio || hold) return@LaunchedEffect
        tracker.target(activeBlock, activeLocal, scroll, force = false)?.let { scrollSafely(scroll, it) }
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().onPlaced { tracker.viewport = it }
                .verticalScroll(scroll).padding(horizontal = READ_PAD, vertical = 12.dp),
        ) { content(tracker) }
        val back = last[0].takeIf { it >= 0 }
        val backLocal = last[1].takeIf { it >= 0 }
        if (hold && playing && followAudio && back != null) {
            Pill(
                "Okunan yere dön", bg = SY.Accent, fg = SY.OnAccent, icon = Icons.Filled.ArrowDownward,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                onClick = {
                    hold = false
                    tracker.target(back, backLocal, scroll, force = true)?.let { t -> scope.launch { scrollSafely(scroll, t) } }
                },
            )
        }
    }
}

/** Paragraflar; okunan cümle hafif vurgulu. Metin seçilebilir kalır. */
@Composable
private fun SyncedParagraphs(blocks: List<List<Segment>>, active: Int?, font: Int, lineMul: Float, tracker: BlockTracker) {
    val hl = SY.Highlight
    SelectionContainer {
        Column {
            var offset = 0
            blocks.forEachIndexed { bi, para ->
                val first = offset
                offset += para.size
                val local = active?.let { it - first }?.takeIf { it in para.indices }
                val text = remember(para, local, hl) {
                    buildAnnotatedString {
                        para.forEachIndexed { i, seg ->
                            if (i > 0) append(' ')
                            if (i == local) withStyle(SpanStyle(background = hl)) { append(seg.text) } else append(seg.text)
                        }
                    }
                }
                // Cümle başlangıç karakterleri (ekranın uzun paragrafta cümleyi izlemesi için)
                tracker.starts[bi] = remember(para) {
                    var o = 0
                    IntArray(para.size) { i -> val at = o; o += para[i].text.length + 1; at }
                }
                Text(
                    text, color = SY.Text, fontSize = font.sp, lineHeight = (font * lineMul).sp,
                    onTextLayout = { tracker.layouts[bi] = it },
                    modifier = Modifier.padding(bottom = (font * 0.8f).dp).onPlaced { tracker.blocks[bi] = it },
                )
            }
        }
    }
}

@Composable
private fun PaperText(paragraphs: List<String>, font: Int, lineMul: Float) {
    SelectionContainer {
        Column {
            paragraphs.forEach { p ->
                Text(
                    p, color = SY.Text, fontSize = font.sp, lineHeight = (font * lineMul).sp,
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
private fun TimedText(
    segments: List<Segment>, s: MainState, vm: MainViewModel, font: Int, lineMul: Float, active: Int?, tracker: BlockTracker,
) {
    Column {
        segments.forEachIndexed { i, seg ->
            val current = i == active
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (current) SY.Highlight else Color.Transparent)
                    .onPlaced { tracker.blocks[i] = it }
                    .padding(vertical = 2.dp),
            ) {
                Box(
                    Modifier.width(60.dp).heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = s.hasAudio, onClickLabel = "Buradan dinle", role = Role.Button) { vm.seekTo(seg.startMs) },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        // "≈": zaman gerçek hizalama değil, tahmin (motor zaman vermedi)
                        (if (seg.approx) "≈" else "") + Transcript.clock(seg.startMs),
                        color = SY.Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
                SelectionContainer(Modifier.weight(1f)) {
                    Text(
                        seg.text, color = SY.Text, fontSize = (font - 1).sp, lineHeight = ((font - 1) * lineMul).sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, end = 4.dp),
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Çeviri
// ---------------------------------------------------------------------------

@Composable
private fun TranslationBody(s: MainState, vm: MainViewModel, font: Int, lineMul: Float, paras: List<String>?) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    val r = s.result ?: return
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = READ_PAD, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${langOf(r.language)?.label ?: r.language} →", color = SY.Muted, fontSize = 13.5.sp, modifier = Modifier.padding(end = 8.dp))
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
                Text(status, color = SY.Muted, fontSize = 13.5.sp, modifier = Modifier.padding(start = 10.dp))
            }
            paras != null -> PaperText(paras, font, lineMul)
            else -> Pill("Çevir", bg = SY.Accent, fg = SY.OnAccent, onClick = { vm.translate(s.translationTarget) })
        }
        Spacer(Modifier.height(14.dp))
        // Küçük ikincil seçenek; metnin Google'a gideceği açıkça yazılı
        Row(
            Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(10.dp))
                .clickable(role = Role.Button, onClickLabel = "Google Çeviri'de aç") {
                    if (!openGoogleTranslate(context, r.text, r.language, s.translationTarget.code)) {
                        vm.toast("Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır")
                    }
                }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Google Çeviri'de aç ↗", color = SY.Accent, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
        }
        Text("Metin Google'a gönderilir · internet gerekir", color = SY.Muted, fontSize = 12.sp)
        Text(
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir.",
            fontSize = 12.sp, color = SY.Muted, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
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
            Text("Hatalı kelimeler mi var?", fontSize = 14.sp, color = SY.Text, fontWeight = FontWeight.Medium)
            Text("\"En iyi\" kalite Türkçede daha doğru ama yavaş. Bu metin ekranda kalır, bitince güncellenir.", fontSize = 12.5.sp, color = SY.Muted)
        }
        Pill("En iyi ile dene", bg = SY.Accent, fg = SY.OnAccent, modifier = Modifier.padding(start = 8.dp), onClick = onRun)
    }
}

// ---------------------------------------------------------------------------
// Alt işlemler
// ---------------------------------------------------------------------------

/** "Diğer": düzenle ve dışa aktar (TXT = ekrandaki metin, SRT = zaman damgalı). */
@Composable
private fun MoreAction(s: MainState, r: Transcript, modifier: Modifier, onEdit: () -> Unit) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        BarAction(Icons.Filled.MoreHoriz, "Diğer", Modifier.fillMaxWidth()) { open = true }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Metni düzenle") }, onClick = { open = false; onEdit() })
            val tr = s.tab == Tab.TRANSLATION
            val suffix = if (tr) "_${s.translationTarget.code}" else ""
            DropdownMenuItem(
                text = { Text(if (tr) "Çeviriyi dosya olarak paylaş (.txt)" else "Metin dosyası olarak paylaş (.txt)") },
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
private fun BarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier,
    enabled: Boolean = true, onClick: () -> Unit,
) {
    // Devre dışıyken soluk görünür; clickable(enabled = false) erişilebilirlikte "devre dışı" bildirir
    val a = if (enabled) 1f else 0.38f
    Row(
        modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(14.dp)).background(SY.Card.copy(alpha = SY.Card.alpha * (if (enabled) 1f else 0.6f)))
            .clickable(enabled = enabled, onClickLabel = label, role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SY.Accent.copy(alpha = a), modifier = Modifier.size(20.dp))
        Text(
            label, color = SY.Text.copy(alpha = a), fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1,
            overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun BarButton(label: String, modifier: Modifier, filled: Boolean, onClick: () -> Unit) {
    Box(
        modifier.height(48.dp).clip(CircleShape).background(if (filled) SY.Accent else SY.Card)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (filled) SY.OnAccent else SY.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
}

// ---------------------------------------------------------------------------
// Yardımcılar
// ---------------------------------------------------------------------------

/**
 * Süre satırı. En iyi ile iyileştirilmiş notta ön izleme ve En iyi turları ayrı
 * yazılır (toplam tek sayı olarak gösterilmez; eski kayıtlarda tek sayı).
 */
private fun processLine(r: Transcript): String {
    val tr = Locale("tr")
    fun sec(ms: Long) = "%.1f".format(tr, ms / 1000.0)
    val q = r.quality?.let { n -> Quality.entries.firstOrNull { it.name == n } }
    return when {
        r.quality == com.aitolian.sesyazibench.QUALITY_WIT_MIX && r.previewMs == 0L ->
            "✓ ⚡ Hızlı mod · ${sec(r.processMs)} sn · " +
                (if (r.segments.any { it.text.startsWith("[⚠") }) "bazı bölümler yazıya dökülemedi" else "bazı bölümler telefonda tamamlandı")
        r.quality == com.aitolian.sesyazibench.QUALITY_WIT && r.previewMs == 0L ->
            "✓ ⚡ Hızlı mod · ${sec(r.processMs)} sn'de yazıya döküldü"
        r.previewMs > 0 -> "✓ Ön izleme ${sec(r.previewMs)} sn · En iyi ${sec(r.processMs)} sn · cihazda yazıya döküldü"
        q != null -> "✓ ${q.label} · ${sec(r.processMs)} sn'de cihazda yazıya döküldü"
        else -> "✓ ${sec(r.processMs)} sn'de cihazda yazıya döküldü"
    }
}

/** Not başlığı: metnin ilk birkaç kelimesi (dosya adı "PTT-2026…opus" gibi anlamsız olduğundan). */
internal fun titleOf(t: Transcript): String {
    val w = t.text.split(Regex("\\s+")).filter { it.isNotBlank() }
    return if (w.isEmpty()) t.fileName else w.take(6).joinToString(" ").trimEnd(',', '.', '!', '?') + if (w.size > 6) "…" else ""
}

internal fun noteDate(t: Transcript): String =
    SimpleDateFormat("d MMM HH:mm", Locale("tr")).format(Date(t.id))

private fun words(t: String) = t.split(Regex("\\s+")).count { it.isNotBlank() }

/**
 * Okuma birimlerini ~3'erli paragraflara böler (zamanlarıyla); not defteri gibi rahat okunur.
 * Metinde noktalama yoksa bütün döküm tek "cümle" olur: o zaman motorun kendi
 * parçaları kullanılır (vurgu ve paragraflar anlamlı kalsın).
 */
internal fun paragraphBlocks(segs: List<Segment>): List<List<Segment>> {
    // Okuma birimleri: aynı başlangıç zamanını paylaşan cümleler tek birim (sahte cümle zamanı yok)
    val sentences = Sentences.readingUnits(segs)
    val avg = if (sentences.isEmpty()) 0 else sentences.sumOf { it.text.length } / sentences.size
    val units = if (avg > 280 && segs.size > sentences.size) segs.filter { it.text.isNotBlank() } else sentences
    return units.chunked(3)
}

/** Paragraf metinleri (dışa aktarma, düzenleme, çeviri, canlı akış). */
internal fun paragraphs(segs: List<Segment>): List<String> =
    paragraphBlocks(segs).map { p -> p.joinToString(" ") { it.text } }

internal fun activeIndex(items: List<Segment>, posMs: Long): Int? = Sentences.activeIndex(items, posMs)

/** Düz cümle sırasından (paragraf, paragraf içindeki cümle). */
private fun blockOf(blocks: List<List<Segment>>, sentence: Int): Pair<Int, Int>? {
    var n = 0
    blocks.forEachIndexed { i, p ->
        if (sentence < n + p.size) return i to (sentence - n)
        n += p.size
    }
    return null
}

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
