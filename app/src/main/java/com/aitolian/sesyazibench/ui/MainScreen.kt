package com.aitolian.sesyazibench.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitolian.sesyazibench.MainState
import com.aitolian.sesyazibench.MainViewModel
import com.aitolian.sesyazibench.Phase
import com.aitolian.sesyazibench.Quality
import com.aitolian.sesyazibench.Tab
import com.aitolian.sesyazibench.ads.Ads
import com.aitolian.sesyazibench.ads.BannerAd
import com.aitolian.sesyazibench.data.Transcript
import com.aitolian.sesyazibench.R
import com.aitolian.sesyazibench.ShareIntegration
import androidx.compose.ui.res.stringResource
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.Segment
import com.aitolian.sesyazibench.engine.TRANSLATABLE
import com.aitolian.sesyazibench.engine.langOf
import java.io.File
import java.util.Locale

private val AUDIO_TYPES = arrayOf("audio/*", "video/*", "application/ogg")

/**
 * V1 · Neon Mor ana ekran.
 * [onNewAudio]: "+ Yeni ses" — geçiş reklamı (sınırlıysa atlanır) sonra verilen işi çalıştırır.
 */
@Composable
fun MainScreen(
    vm: MainViewModel,
    onNewAudio: (then: () -> Unit) -> Unit,
    onProcessingAd: () -> Unit,
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val adsReady by Ads.ready.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::onAudio)
    }
    // Uzun sürecek dökümde bekleme süresine geçiş reklamı (sınırlar Ads içinde)
    var handledAd by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(s.adRequest) {
        if (s.adRequest > handledAd) { handledAd = s.adRequest; onProcessingAd() } // döndürmede tekrar gösterme
    }
    LaunchedEffect(s.toast) {
        s.toast?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.toastShown() }
    }
    val busy = s.phase is Phase.Preparing || s.phase is Phase.Downloading || s.phase is Phase.Transcribing
    // Ana giriş: WhatsApp'ın Paylaş menüsü (klasör izni yok); dosya seçici yan seçenek
    val pickOtherFile = { if (!busy) picker.launch(AUDIO_TYPES) }
    val openWhatsApp = {
        if (!ShareIntegration.openWhatsApp(context)) {
            vm.toast("WhatsApp bulunamadı, dosyadan seçebilirsin")
            pickOtherFile()
        }
    }

    if (showSettings) {
        SettingsScreen(vm, s, onBack = { showSettings = false })
        return
    }

    Column(Modifier.fillMaxSize().background(SY.Bg).background(SY.background)) {
        Column(Modifier.statusBarsPadding().weight(1f)) {
            TopBar(onSettings = { showSettings = true })
            Hero(s, busy, onPick = openWhatsApp, onCancel = vm::cancelWork)
            Controls(s, busy, vm)
            Spacer(Modifier.height(14.dp))
            ResultSheet(
                s, vm, Modifier.weight(1f),
                onNew = { if (!busy) onNewAudio { vm.clearForNew() } },
                picker = { ShareGuide(onOpenWhatsApp = openWhatsApp, onOtherFile = pickOtherFile) },
            )
        }
        // Altta sabit banner — içerikle asla çakışmaz
        Box(Modifier.fillMaxWidth().background(SY.AdBg).navigationBarsPadding().padding(vertical = 4.dp)) {
            if (adsReady) BannerAd() else Spacer(Modifier.fillMaxWidth().height(50.dp))
        }
    }
}

@Composable
private fun TopBar(onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.app_name), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SY.Text, modifier = Modifier.weight(1f))
        Box(
            Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) { Text("⚙", fontSize = 20.sp, color = SY.Muted) }
    }
}

@Composable
private fun Hero(s: MainState, busy: Boolean, onPick: () -> Unit, onCancel: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(128.dp), contentAlignment = Alignment.Center) {
            when (val p = s.phase) {
                is Phase.Transcribing -> ProgressRing(p.percent / 100f, "%${p.percent}")
                is Phase.Downloading -> ProgressRing(p.progress, "%${(p.progress * 100).toInt()}")
                is Phase.Preparing -> CircularProgressIndicator(
                    Modifier.size(118.dp), color = SY.Accent, strokeWidth = 8.dp, trackColor = SY.Card,
                )
                else -> OrbButton(onPick)
            }
        }
        Spacer(Modifier.height(10.dp))
        val (title, sub) = when (val p = s.phase) {
            is Phase.Preparing -> p.message to "Ses telefonundan çıkmaz"
            is Phase.Downloading -> "Model indiriliyor…" to "Tek seferlik · ${s.quality.model.approxMb} MB"
            is Phase.Transcribing -> "Yazıya dökülüyor…" to
                (s.etaSec?.let { "Tahmini ~$it sn · " } ?: "") + "internet gerekmez, ses telefondan çıkmaz"
            is Phase.Failed -> "Bir sorun oldu" to p.message
            Phase.Idle -> "Sesli mesajı yazıya dök" to "WhatsApp'tan 3 dokunuşla · ses telefondan çıkmaz"
        }
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = SY.Text)
        Text(
            sub, fontSize = 12.sp, textAlign = TextAlign.Center,
            color = if (s.phase is Phase.Failed) SY.Error else SY.Muted,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 3.dp),
        )
        if (s.phase is Phase.Transcribing) {
            Pill("İptal", bg = SY.Chip, fg = SY.Text, modifier = Modifier.padding(top = 4.dp), onClick = onCancel)
        }
        if (s.phase is Phase.Failed && !busy) {
            Pill("Tekrar dene", bg = SY.Accent, fg = SY.OnAccent, modifier = Modifier.padding(top = 4.dp), onClick = onPick)
        }
    }
}

@Composable
private fun OrbButton(onClick: () -> Unit) {
    Box(
        Modifier.size(118.dp)
            .shadow(28.dp, CircleShape, ambientColor = SY.A1, spotColor = SY.A1)
            .clip(CircleShape)
            .background(Brush.sweepGradient(listOf(SY.A1, SY.A2, SY.A1)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(104.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(SY.A2, SY.A1), center = Offset(110f, 90f), radius = 260f)),
            contentAlignment = Alignment.Center,
        ) { WaveIcon(SY.OnAccent, 44.dp) }
    }
}

@Composable
private fun ProgressRing(progress: Float, label: String) {
    Box(Modifier.size(118.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(118.dp)) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(SY.Card, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                Brush.sweepGradient(listOf(SY.A1, SY.A2, SY.A1)), -90f, 360f * progress.coerceIn(0f, 1f), false,
                Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Text(label, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = SY.Accent)
    }
}

@Composable
private fun WaveIcon(color: Color, iconSize: Dp) {
    Canvas(Modifier.size(iconSize)) {
        val w = size.width
        val bar = w / 10f
        val heights = listOf(0.2f, 0.45f, 0.8f, 0.35f, 0.6f)
        heights.forEachIndexed { i, h ->
            val x = w * 0.14f + i * bar * 1.7f
            val hh = size.height * h
            drawRoundRect(color, Offset(x, (size.height - hh) / 2), Size(bar, hh), CornerRadius(bar / 2))
        }
    }
}

@Composable
private fun Controls(s: MainState, busy: Boolean, vm: MainViewModel) {
    var langOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Pill("🌐 ${s.lang.label} ▾", bg = SY.Chip, fg = SY.Text, onClick = { if (!busy) langOpen = true })
            DropdownMenu(expanded = langOpen, onDismissRequest = { langOpen = false }) {
                (listOf(Lang.AUTO) + TRANSLATABLE).forEach { l ->
                    DropdownMenuItem(text = { Text(l.label) }, onClick = {
                        langOpen = false
                        if (l != s.lang) { vm.setLang(l); vm.retranscribe() }
                    })
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Quality.entries.forEach { q ->
            val sel = s.quality == q
            Pill(
                q.label, bg = if (sel) SY.Accent else SY.Chip, fg = if (sel) SY.OnAccent else SY.Text,
                modifier = Modifier.padding(end = 8.dp),
                onClick = {
                    if (!busy && !sel) {
                        vm.setQuality(q)
                        if (q == Quality.BEST) vm.toast("En iyi: ${q.model.approxMb} MB model, orta seviye telefonlarda yavaş")
                        vm.retranscribe()
                    }
                },
            )
        }
    }
}

@Composable
private fun ResultSheet(
    s: MainState,
    vm: MainViewModel,
    modifier: Modifier,
    onNew: () -> Unit,
    picker: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Column(
        modifier.fillMaxWidth()
            .clip(shape)
            .background(SY.Sheet)
            .border(1.dp, Color(0x12FFFFFF), shape)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Box(
            Modifier.align(Alignment.CenterHorizontally).size(40.dp, 4.dp).clip(CircleShape)
                .background(SY.Muted.copy(alpha = .4f)),
        )
        Spacer(Modifier.height(12.dp))
        val r = s.result
        if (r == null && s.live.isNotEmpty()) {
            LivePane(s)
        } else if (r == null) {
            picker()
        } else {
            PlayerRow(s, vm)
            s.refining?.let { RefineBanner(it) }
            Spacer(Modifier.height(12.dp))
            Tabs(s.tab) { t -> if (t == Tab.TRANSLATION) vm.openTranslation() else vm.setTab(t) }
            Spacer(Modifier.height(8.dp))
            when (s.tab) {
                Tab.TEXT -> Segments(r.segments, s, vm, highlight = true)
                Tab.TRANSLATION -> TranslationPane(s, vm)
            }
            // Kopyala / Paylaş / SRT, açık olan sekmenin metnini kullanır
            val shown = if (s.tab == Tab.TRANSLATION) s.translation else r.segments
            val suffix = if (s.tab == Tab.TRANSLATION) "_${s.translationTarget.code}" else ""
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Action("⧉", "Kopyala", Modifier.weight(1f)) {
                    if (shown == null) vm.toast("Çeviri henüz hazır değil")
                    else { copy(context, shown.joinToString(" ") { it.text }); vm.toast("Kopyalandı") }
                }
                Action("↗", "Paylaş", Modifier.weight(1f)) {
                    if (shown == null) vm.toast("Çeviri henüz hazır değil") else shareText(context, shown.joinToString(" ") { it.text })
                }
                Action("文A", "Çevir", Modifier.weight(1f)) { vm.openTranslation() }
                Action("⤓", "SRT / TXT", Modifier.weight(1f)) {
                    if (shown == null) vm.toast("Çeviri henüz hazır değil") else shareSrt(context, r, shown, suffix)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "✓ ${"%.1f".format(Locale("tr"), r.processMs / 1000.0)} sn · cihazda işlendi · ${langName(r.language)}",
                    fontSize = 12.sp, color = SY.Muted, modifier = Modifier.weight(1f),
                )
                Text(
                    "+ Yeni ses", fontSize = 13.sp, color = SY.Accent, fontWeight = FontWeight.Medium,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onNew).padding(6.dp),
                )
            }
            if (s.suggestBest && s.hasAudio) SuggestBestCard(onRun = vm::rerunWithBest)
        }
        History(s, vm)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LivePane(s: MainState) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(SY.A2))
            Text("Canlı · metin geldikçe yazılıyor", fontSize = 12.sp, color = SY.Muted, modifier = Modifier.padding(start = 8.dp))
        }
        s.live.forEach { seg ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp)) {
                Text(
                    Transcript.clock(seg.startMs), fontSize = 12.sp, color = SY.Accent, fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(44.dp).padding(top = 3.dp),
                )
                Text(seg.text, fontSize = 16.sp, lineHeight = 22.sp, color = SY.Text, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RefineBanner(text: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(SY.A1.copy(alpha = .25f), SY.A2.copy(alpha = .18f))))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(16.dp), color = SY.Accent, strokeWidth = 2.dp)
        Text(text, fontSize = 12.5.sp, color = SY.Text, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun SuggestBestCard(onRun: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(14.dp))
            .border(1.dp, SY.Accent.copy(alpha = .35f), RoundedCornerShape(14.dp))
            .background(SY.Card).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Türkçe için daha doğru sonuç", fontSize = 13.5.sp, color = SY.Text, fontWeight = FontWeight.Medium)
            Text("\"En iyi\" mod önce hızlı metni gösterir, sonra arka planda iyileştirir.", fontSize = 12.sp, color = SY.Muted)
        }
        Pill("En iyi ile dene", bg = SY.Accent, fg = SY.OnAccent, modifier = Modifier.padding(start = 8.dp), onClick = onRun)
    }
}

@Composable
private fun PlayerRow(s: MainState, vm: MainViewModel) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SY.Card)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(if (s.hasAudio) SY.Accent else SY.Chip)
                .clickable(enabled = s.hasAudio, onClick = vm::togglePlay),
            contentAlignment = Alignment.Center,
        ) { Text(if (s.playing) "❚❚" else "▶", color = if (s.hasAudio) SY.OnAccent else SY.Muted, fontSize = 12.sp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row {
                Text(
                    s.fileName ?: "", fontSize = 13.sp, color = SY.Text, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                )
                Text("${Transcript.clock(s.positionMs)} / ${Transcript.clock(s.audioMs)}", fontSize = 12.sp, color = SY.Muted)
            }
            Waveform(
                s.waveform, if (s.audioMs > 0) s.positionMs.toFloat() / s.audioMs else 0f,
                Modifier.fillMaxWidth().height(24.dp).padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun Waveform(peaks: FloatArray, progress: Float, modifier: Modifier) {
    Canvas(modifier) {
        val n = if (peaks.isEmpty()) 46 else peaks.size
        val step = size.width / n
        val bw = (step * 0.5f).coerceAtLeast(2f)
        for (i in 0 until n) {
            val v = if (peaks.isEmpty()) 0.15f else peaks[i]
            val h = size.height * v
            val color = if (progress > 0f && i.toFloat() / n <= progress) SY.Accent else Color(0x30FFFFFF)
            drawRoundRect(color, Offset(i * step, (size.height - h) / 2), Size(bw, h), CornerRadius(bw / 2))
        }
    }
}

@Composable
private fun Tabs(tab: Tab, onTab: (Tab) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(CircleShape).background(SY.Card).padding(3.dp)) {
        listOf(Tab.TEXT to "Metin", Tab.TRANSLATION to "Çeviri").forEach { (t, label) ->
            val sel = t == tab
            Box(
                Modifier.weight(1f).clip(CircleShape).background(if (sel) SY.Accent else Color.Transparent)
                    .clickable { onTab(t) }.padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label, fontSize = 13.5.sp, color = if (sel) SY.OnAccent else SY.Muted,
                    fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun Segments(segments: List<Segment>, s: MainState, vm: MainViewModel, highlight: Boolean) {
    Column {
        segments.forEach { seg ->
            val current = highlight && s.playing && s.positionMs >= seg.startMs && s.positionMs < seg.endMs
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(if (current) SY.Card else Color.Transparent)
                    .clickable(enabled = s.hasAudio) { vm.seekTo(seg.startMs) }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            ) {
                if (current) Box(Modifier.width(3.dp).height(20.dp).background(SY.Accent))
                Text(
                    Transcript.clock(seg.startMs), fontSize = 12.sp, color = SY.Accent, fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(44.dp).padding(start = if (current) 6.dp else 0.dp, top = 3.dp),
                )
                Text(seg.text, fontSize = 16.sp, lineHeight = 22.sp, color = SY.Text, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TranslationPane(s: MainState, vm: MainViewModel) {
    var open by remember { mutableStateOf(false) }
    val source = s.result?.language
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Text(
                "${langOf(source)?.label ?: source} →", fontSize = 13.sp, color = SY.Muted,
                modifier = Modifier.padding(end = 8.dp),
            )
            Box {
                Pill("${s.translationTarget.label} ▾", bg = SY.Chip, fg = SY.Text, onClick = { if (s.translating == null) open = true })
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    TRANSLATABLE.filter { it.code != source }.forEach { l ->
                        DropdownMenuItem(text = { Text(l.label) }, onClick = { open = false; vm.translate(l) })
                    }
                }
            }
        }
        val status = s.translating
        val tr = s.translation
        when {
            status != null -> Row(
                Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(Modifier.size(18.dp), color = SY.Accent, strokeWidth = 2.dp)
                Text(status, color = SY.Muted, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
            }
            tr != null -> Segments(tr, s, vm, highlight = false)
            else -> Pill(
                "Çevir", bg = SY.Accent, fg = SY.OnAccent, modifier = Modifier.padding(10.dp),
                onClick = { vm.translate(s.translationTarget) },
            )
        }
        Text(
            "Çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir.",
            fontSize = 11.5.sp, color = SY.Muted, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun Action(icon: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(14.dp)).background(SY.Card)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Text(icon, color = SY.Accent, fontSize = 17.sp) }
        Text(label, color = SY.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun History(s: MainState, vm: MainViewModel) {
    val items = s.history.filter { it.id != s.result?.id }.take(8)
    if (items.isEmpty()) return
    Row(
        Modifier.fillMaxWidth().padding(top = 14.dp).horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Son:", fontSize = 12.sp, color = SY.Muted)
        items.forEach { t ->
            Pill(t.preview, bg = SY.Card, fg = SY.Text, bold = false, modifier = Modifier.padding(start = 8.dp)) {
                vm.openHistory(t)
            }
        }
    }
}

@Composable
fun Pill(
    text: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    bold: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier.clip(CircleShape).background(bg).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text, color = fg, fontSize = 13.sp, maxLines = 1,
            fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

private fun langName(code: String) = langOf(code)?.label ?: code

private fun copy(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("SesYazı", text))
}

private fun shareText(context: Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    context.startActivity(Intent.createChooser(i, "Paylaş"))
}

private fun shareSrt(context: Context, t: Transcript, segments: List<Segment>, suffix: String) {
    val dir = File(context.filesDir, "results").apply { mkdirs() }
    val base = t.fileName.substringBeforeLast('.').ifBlank { "sesyazi" } + suffix
    val view = t.copy(segments = segments)
    val srt = File(dir, "$base.srt").apply { writeText(view.toSrt()) }
    val uri = FileProvider.getUriForFile(context, context.packageName + ".files", srt)
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "application/x-subrip"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, view.toTxt())
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(i, "SRT / TXT paylaş"))
}
