package com.aitolian.sesyazibench.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitolian.sesyazibench.MainState
import com.aitolian.sesyazibench.MainViewModel
import com.aitolian.sesyazibench.Phase
import com.aitolian.sesyazibench.R
import com.aitolian.sesyazibench.ShareIntegration
import com.aitolian.sesyazibench.ads.Ads
import com.aitolian.sesyazibench.ads.BannerAd
import com.aitolian.sesyazibench.ads.NativeAdCard

private val AUDIO_TYPES = arrayOf("audio/*", "video/*", "application/ogg")

/** Tüm notlar ekranı: kapalı / liste / arama kutusu odaklı. */
private const val NOTES_CLOSED = 0
private const val NOTES_LIST = 1
private const val NOTES_SEARCH = 2

/**
 * Ana sayfa: uygulama adı + ayarlar → anlaşılır ses girişleri → dil →
 * notlarda arama → son notlar → (ilk kullanımda açık) kullanım kılavuzu.
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
    var notesMode by rememberSaveable { mutableIntStateOf(NOTES_CLOSED) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::onAudio)
    }
    // Döküm başı geçiş reklamı (sınırlar Ads içinde)
    var handledAd by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(s.adRequest) {
        if (s.adRequest > handledAd) { handledAd = s.adRequest; onProcessingAd() } // döndürmede tekrar gösterme
    }
    LaunchedEffect(s.toast) {
        s.toast?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.toastShown() }
    }
    val busy = s.phase is Phase.Preparing || s.phase is Phase.Transcribing
    val pickFile = { if (!busy) picker.launch(AUDIO_TYPES) }
    val openWhatsApp = {
        if (!ShareIntegration.openWhatsApp(context)) {
            vm.toast("WhatsApp bulunamadı, dosyadan seçebilirsin")
            pickFile()
        }
    }

    if (showSettings) {
        SettingsScreen(vm, s, onBack = { showSettings = false })
        return
    }
    // Döküm metni gelmeye başlayınca, yeniden dökümde ya da bir not açılınca: tam ekran okuyucu
    if (s.result != null || s.live.isNotEmpty() || s.livePartial != null || s.previousResult != null) {
        NoteScreen(s, vm, adsReady, onHome = vm::goHome)
        return
    }
    if (notesMode != NOTES_CLOSED) {
        AllNotesScreen(s, vm, focusSearch = notesMode == NOTES_SEARCH, onBack = { notesMode = NOTES_CLOSED })
        return
    }
    val canGoHome = !busy && s.phase is Phase.Failed
    val goHome = { vm.clearForNew() }
    // Geri tuşu: hata ekranından uygulamayı kapatmak yerine başlangıca dön
    BackHandler(enabled = canGoHome) { goHome() }

    Column(Modifier.fillMaxSize().background(SY.Bg)) {
        Column(
            Modifier.statusBarsPadding().weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TopBar(onHome = if (canGoHome) goHome else null, onSettings = { showSettings = true })
            if (busy || s.phase is Phase.Failed) {
                StatusCard(s, busy, onCancel = vm::cancelWork, onRetry = { if (vm.canRetry()) vm.retranscribe() else openWhatsApp() })
            }
            if (!busy) Hero(s, firstUse = s.history.isEmpty(), onWhatsApp = openWhatsApp, onFile = pickFile)
            Controls(s, busy, vm)
            if (s.history.isNotEmpty()) SearchEntry { notesMode = NOTES_SEARCH }
            UndoBar(s, vm)
            History(s, vm, onAllNotes = { notesMode = NOTES_LIST })
            // İçerik akışında tek yerel reklam: notlardan ve düğmelerden ayrı, "Reklam" etiketli
            if (adsReady) NativeAdCard(Modifier.padding(vertical = 4.dp))
            GuideSection(firstUse = s.history.isEmpty())
            Spacer(Modifier.height(8.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun TopBar(onHome: (() -> Unit)?, onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onHome != null) {
            IconTap(Icons.AutoMirrored.Filled.ArrowBack, "Ana sayfaya dön", onClick = onHome)
            Spacer(Modifier.width(4.dp))
        } else {
            // Küçük marka işareti: ses dalgası (düğme değil)
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(SY.A1, SY.A2))),
                contentAlignment = Alignment.Center,
            ) { WaveMark(androidx.compose.ui.graphics.Color.White, 20.dp) }
            Spacer(Modifier.width(10.dp))
        }
        Text(
            stringResource(R.string.app_name), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = SY.Text,
            letterSpacing = (-0.2).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
        )
        IconTap(Icons.Filled.Settings, "Ayarlar", tint = SY.Muted, onClick = onSettings)
    }
}

/**
 * Üst bölüm: tek net vaat + iki anlaşılır eylem. Birincil: WhatsApp'ı aç (paylaşım
 * oradan yapılır; düğme kendi başına döküm başlatmaz). İkincil: dosya seç.
 */
@Composable
private fun Hero(s: MainState, firstUse: Boolean, onWhatsApp: () -> Unit, onFile: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Dinleyemiyorsan,\noku.", color = SY.Text, fontSize = 32.sp, lineHeight = 36.sp,
            fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp,
        )
        Text(
            if (firstUse) "WhatsApp'ta sesli mesaja uzun bas → Paylaş → MuteRead. Metin saniyeler içinde hazır."
            else "Sesli mesajı MuteRead ile paylaş, saniyeler içinde oku.",
            color = SY.Muted, fontSize = 15.sp, lineHeight = 21.sp,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(18.dp)).background(SY.Accent)
                .clickable(role = Role.Button, onClickLabel = "WhatsApp'ı aç", onClick = onWhatsApp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.Chat, contentDescription = null, tint = SY.OnAccent, modifier = Modifier.size(22.dp))
            Text(
                "WhatsApp'ı aç", color = SY.OnAccent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Row(
            Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(18.dp)).background(SY.Sheet)
                .border(1.dp, SY.Outline, RoundedCornerShape(18.dp))
                .clickable(role = Role.Button, onClickLabel = "Ses ya da video dosyası seç", onClick = onFile)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = SY.Accent, modifier = Modifier.size(22.dp))
            Text(
                "Ses / video dosyası seç", color = SY.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

/** Hazırlık / indirme / döküm / hata: kompakt durum kartı (eski 128 dp'lik alan yerine). */
@Composable
private fun StatusCard(s: MainState, busy: Boolean, onCancel: () -> Unit, onRetry: () -> Unit) {
    val failed = s.phase is Phase.Failed
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(SY.Sheet)
            .border(1.dp, if (failed) SY.Error.copy(alpha = .4f) else SY.Outline, RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            when (val p = s.phase) {
                is Phase.Transcribing -> if (p.percent < 0) CircularProgressIndicator(
                    Modifier.size(56.dp), color = SY.Accent, strokeWidth = 5.dp, trackColor = SY.Card,
                ) else ProgressRing(p.percent / 100f, "%${p.percent}")
                is Phase.Preparing -> CircularProgressIndicator(
                    Modifier.size(56.dp), color = SY.Accent, strokeWidth = 5.dp, trackColor = SY.Card,
                )
                else -> Text("!", color = SY.Error, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
        }
        val (title, sub) = when (val p = s.phase) {
            is Phase.Preparing -> p.message to "⚡ Birazdan yazıya dökülüyor"
            is Phase.Transcribing -> "Yazıya dökülüyor…" to "⚡ Saniyeler içinde · ${s.lang.label}"
            is Phase.Failed -> "Bir sorun oldu" to p.message
            Phase.Idle -> "" to ""
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = SY.Text)
            Text(sub, fontSize = 13.sp, color = if (failed) SY.Error else SY.Muted, lineHeight = 18.sp)
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (s.phase is Phase.Transcribing || s.phase is Phase.Preparing) {
                    Pill("İptal", bg = SY.Chip, fg = SY.Text, onClick = onCancel)
                }
                if (failed && !busy) Pill("Tekrar dene", bg = SY.Accent, fg = SY.OnAccent, onClick = onRetry)
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, label: String) {
    val track = SY.Card
    Box(Modifier.size(60.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(60.dp)) {
            val stroke = 6.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                Brush.sweepGradient(listOf(SY.A1, SY.A2, SY.A1)), -90f, 360f * progress.coerceIn(0f, 1f), false,
                Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SY.Accent)
    }
}

@Composable
private fun Controls(s: MainState, busy: Boolean, vm: MainViewModel) {
    var langOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Pill(
                "Konuşma dili: ${s.lang.label} ▾", bg = SY.Chip, fg = SY.Text, icon = Icons.Filled.Language,
                onClick = { if (!busy) langOpen = true },
            )
            DropdownMenu(expanded = langOpen, onDismissRequest = { langOpen = false }) {
                vm.speechLangs.forEach { l ->
                    DropdownMenuItem(text = { Text(l.label) }, onClick = {
                        langOpen = false
                        if (l != s.lang) vm.retranscribeInLanguage(l)
                    })
                }
            }
        }
    }
}

/** Notlarda arama girişi: dokununca arama kutusu odaklı "Notlarım" açılır. */
@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 50.dp).clip(RoundedCornerShape(25.dp)).background(SY.Sheet)
            .border(1.dp, SY.Outline, RoundedCornerShape(25.dp))
            .clickable(role = Role.Button, onClickLabel = "Notlarda ara", onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = SY.Muted, modifier = Modifier.size(20.dp))
        Text("Notlarda ara", color = SY.Muted, fontSize = 15.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

/** Geçmişten silinen kayıt için 5 sn'lik "Geri al". */
@Composable
private fun UndoBar(s: MainState, vm: MainViewModel) {
    val d = s.undoDeleted ?: return
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SY.Card).padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Silindi: ${d.preview}", color = SY.Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Box(
            Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = vm::undoDelete)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) { Text("Geri al", color = SY.Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
    }
}

/** Son notlar: tek beyaz yüzeyde hafif satırlar (en fazla 3). Dokun → aç, basılı tut → sil. */
@Composable
private fun History(s: MainState, vm: MainViewModel, onAllNotes: () -> Unit) {
    if (s.history.isEmpty()) return
    Column {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Son notlar", color = SY.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Box(
                Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(10.dp)).clickable(role = Role.Button, onClick = onAllNotes)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Tümü (${s.history.size})", color = SY.Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SY.Sheet)
                .border(1.dp, SY.Outline, RoundedCornerShape(20.dp)),
        ) {
            val items = s.history.take(3)
            items.forEachIndexed { i, t ->
                NoteRow(t, onOpen = { vm.openHistory(t) }, onDelete = { vm.deleteHistory(t) })
                if (i < items.lastIndex) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(SY.Outline))
                }
            }
        }
        Text(
            "Silmek için nota basılı tut", fontSize = 12.sp, color = SY.Muted,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp),
        )
    }
}

/** Kılavuz: ilk kullanımda açık; sonra "Nasıl kullanılır?" altında kapalı. */
@Composable
private fun GuideSection(firstUse: Boolean) {
    var open by rememberSaveable(firstUse) { mutableStateOf(firstUse) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClickLabel = if (open) "Kılavuzu kapat" else "Kılavuzu aç") { open = !open }
                .semantics { contentDescription = "Nasıl kullanılır?" }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Nasıl kullanılır?", color = SY.Accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(
                if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null, tint = SY.Accent,
            )
        }
        if (open) ShareGuide()
    }
}
