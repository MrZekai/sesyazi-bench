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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.selection.SelectionContainer
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
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showAllNotes by rememberSaveable { mutableStateOf(false) }
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

    // İlk kullanımda motor seçimi (Hızlı/Gizli) — hangi ekranda olursa olsun
    if (s.askEngine) EngineChoiceDialog(onChoose = vm::setEngineMode)

    if (showSettings) {
        SettingsScreen(vm, s, onBack = { showSettings = false })
        return
    }
    // Döküm metni gelmeye başlayınca, yeniden dökümde ya da bir not açılınca: tam ekran not defteri
    if (s.result != null || s.live.isNotEmpty() || s.livePartial != null || s.previousResult != null) {
        NoteScreen(s, vm, adsReady, onHome = vm::goHome)
        return
    }
    if (showAllNotes) {
        AllNotesScreen(s, vm, onBack = { showAllNotes = false })
        return
    }
    val canGoHome = !busy && s.phase is Phase.Failed
    val goHome = { vm.clearForNew() }
    // Geri tuşu: sonuç/hata ekranından uygulamayı kapatmak yerine başlangıca dön
    BackHandler(enabled = canGoHome) { goHome() }

    Column(Modifier.fillMaxSize().background(SY.Bg).background(SY.background)) {
        Column(Modifier.statusBarsPadding().weight(1f)) {
            TopBar(onHome = if (canGoHome) goHome else null, onSettings = { showSettings = true })
            Hero(
                s, busy, onPick = openWhatsApp, onCancel = vm::cancelWork,
                onRetry = { if (vm.canRetry()) vm.retranscribe() else openWhatsApp() },
            )
            Controls(s, busy, vm)
            Spacer(Modifier.height(14.dp))
            HomeSheet(
                s, vm, Modifier.weight(1f), onAllNotes = { showAllNotes = true },
                guide = { ShareGuide(onOpenWhatsApp = openWhatsApp, onOtherFile = pickOtherFile) },
            )
        }
        // Altta sabit banner — içerikle asla çakışmaz
        Box(Modifier.fillMaxWidth().background(SY.AdBg).navigationBarsPadding().padding(vertical = 4.dp)) {
            if (adsReady) BannerAd() else Spacer(Modifier.fillMaxWidth().height(50.dp))
        }
    }
}

@Composable
private fun TopBar(onHome: (() -> Unit)?, onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = if (onHome != null) 8.dp else 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Sonuç/hata ekranındayken başlangıca (boş ana sayfa) dönüş
        if (onHome != null) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(SY.Card)
                    .clickable(onClickLabel = "Ana sayfa", role = Role.Button, onClick = onHome)
                    .semantics { contentDescription = "Ana sayfaya dön" },
                contentAlignment = Alignment.Center,
            ) { Text("⌂", fontSize = 20.sp, color = SY.Text) }
            Spacer(Modifier.width(10.dp))
        }
        Text(stringResource(R.string.app_name), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SY.Text, modifier = Modifier.weight(1f))
        Box(
            Modifier.size(48.dp).clip(CircleShape)
                .clickable(onClickLabel = "Ayarlar", role = Role.Button, onClick = onSettings)
                .semantics { contentDescription = "Ayarlar" },
            contentAlignment = Alignment.Center,
        ) { Text("⚙", fontSize = 20.sp, color = SY.Muted) }
    }
}

@Composable
private fun Hero(s: MainState, busy: Boolean, onPick: () -> Unit, onCancel: () -> Unit, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(128.dp), contentAlignment = Alignment.Center) {
            when (val p = s.phase) {
                is Phase.Transcribing -> if (p.percent < 0) CircularProgressIndicator(
                    Modifier.size(118.dp), color = SY.Accent, strokeWidth = 8.dp, trackColor = SY.Card,
                ) else ProgressRing(p.percent / 100f, "%${p.percent}")
                is Phase.Downloading -> ProgressRing(p.progress, "%${(p.progress * 100).toInt()}")
                is Phase.Preparing -> CircularProgressIndicator(
                    Modifier.size(118.dp), color = SY.Accent, strokeWidth = 8.dp, trackColor = SY.Card,
                )
                else -> OrbButton(onPick)
            }
        }
        Spacer(Modifier.height(10.dp))
        val (title, sub) = when (val p = s.phase) {
            is Phase.Preparing -> p.message to (if (s.engineMode == 1) "Hızlı mod · internet" else "Ses telefonundan çıkmaz")
            is Phase.Downloading -> "Model indiriliyor…" to "Tek seferlik · ${p.mb} MB"
            is Phase.Transcribing -> "Yazıya dökülüyor…" to
                if (p.percent < 0) "⚡ Hızlı mod · birkaç saniye"
                else (s.etaSec?.let { "Tahmini ~$it sn · " } ?: "") + "internet gerekmez, ses telefondan çıkmaz"
            is Phase.Failed -> "Bir sorun oldu" to p.message
            Phase.Idle -> "Sesli mesajı yazıya dök" to
                if (s.engineMode == 1) "WhatsApp'tan 3 dokunuşla · ⚡ saniyeler içinde"
                else "WhatsApp'tan 3 dokunuşla · ses telefondan çıkmaz"
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
            Pill("Tekrar dene", bg = SY.Accent, fg = SY.OnAccent, modifier = Modifier.padding(top = 4.dp), onClick = onRetry)
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
                        if (l != s.lang) vm.retranscribeInLanguage(l)
                    })
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        // Motor: Hızlı (internet) / Telefonda. Hızlı modda kalite seçimi gizli
        // (yalnızca internet yoksa telefonda varsayılan kaliteyle dökülür).
        if (vm.cloudAvailable) {
            val fast = s.engineMode == 1
            Pill(
                if (fast) "⚡ Hızlı" else "🔒 Telefonda",
                bg = if (fast) SY.Accent else SY.Chip, fg = if (fast) SY.OnAccent else SY.Text,
                modifier = Modifier.padding(end = 8.dp),
                onClick = { if (!busy) vm.setEngineMode(if (fast) 2 else 1) },
            )
        }
        if (s.engineMode != 1) Quality.entries.forEach { q ->
            val sel = s.quality == q
            Pill(
                q.label, bg = if (sel) SY.Accent else SY.Chip, fg = if (sel) SY.OnAccent else SY.Text,
                modifier = Modifier.padding(end = 8.dp),
                onClick = {
                    if (!busy && !sel) {
                        vm.setQuality(q)
                        if (q == Quality.BEST) vm.toast("En iyi: ${q.model.approxMb} MB model, orta seviye telefonlarda yavaş")
                        vm.retranscribe(forceLocal = true)
                    }
                },
            )
        }
    }
}

/**
 * İlk kullanımda motor seçimi. Hızlı: ses Meta Wit.ai'ye gönderilir (saniyeler).
 * Telefonda: ses cihazdan çıkmaz (daha yavaş). Ayarlar'dan her zaman değişir.
 */
@Composable
private fun EngineChoiceDialog(onChoose: (Int) -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {}, // seçim yapılmadan kapanmasın (döküm bu cevabı bekliyor)
        containerColor = SY.Sheet, titleContentColor = SY.Text, textContentColor = SY.Muted,
        title = { Text("Nasıl yazıya dökelim?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("⚡ Hızlı (önerilen): Metin birkaç saniyede hazır. Ses, yazıya dökülmek için internet üzerinden Meta Wit.ai'ye gönderilir.", fontSize = 13.5.sp)
                Text("🔒 Telefonda: Ses telefonundan hiç çıkmaz, internetsiz çalışır. Uzun seslerde dakikalar sürebilir.", fontSize = 13.5.sp)
                Text("İnternet olmadığında Hızlı mod da otomatik olarak telefonda çalışır. Tercihini Ayarlar'dan değiştirebilirsin.", fontSize = 12.sp)
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onChoose(1) }) { Text("⚡ Hızlı", color = SY.Accent) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = { onChoose(2) }) { Text("🔒 Telefonda", color = SY.Text) }
        },
    )
}

@Composable
private fun HomeSheet(
    s: MainState,
    vm: MainViewModel,
    modifier: Modifier,
    onAllNotes: () -> Unit,
    guide: @Composable () -> Unit,
) {
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
        UndoBar(s, vm)
        // Notlar önce (dönen kullanıcı için), kılavuz altta
        History(s, vm, onAllNotes)
        guide()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun Waveform(peaks: FloatArray, progress: Float, modifier: Modifier) {
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

/** Geçmişten silinen kayıt için 5 sn'lik "Geri al". */
@Composable
private fun UndoBar(s: MainState, vm: MainViewModel) {
    val d = s.undoDeleted ?: return
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(12.dp)).background(SY.Card)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Silindi: ${d.preview}", color = SY.Muted, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Text(
            "Geri al", color = SY.Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = vm::undoDelete).padding(6.dp),
        )
    }
}


/** Son notlar: not defteri kartları. Dokun → aç, basılı tut → sil (geri alınabilir). */
@Composable
private fun History(s: MainState, vm: MainViewModel, onAllNotes: () -> Unit) {
    if (s.history.isEmpty()) return
    Row(Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("NOTLARIM", color = SY.Accent, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(
            "Tümü (${s.history.size}) · Ara ›", color = SY.Accent, fontSize = 12.5.sp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = onAllNotes)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        s.history.take(5).forEach { t -> NoteCard(t, onOpen = { vm.openHistory(t) }, onDelete = { vm.deleteHistory(t) }) }
    }
    Text(
        "Silmek için nota basılı tut", fontSize = 11.sp, color = SY.Muted,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 16.dp),
    )
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
        modifier.clip(CircleShape).background(bg).clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text, color = fg, fontSize = 13.sp, maxLines = 1,
            fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

private fun langName(code: String) = langOf(code)?.label ?: code

internal fun copyText(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), text))
}

internal fun shareText(context: Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    context.startActivity(Intent.createChooser(i, "Paylaş"))
}
