package com.aitolian.sesyazibench.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitolian.sesyazibench.MainState
import com.aitolian.sesyazibench.MainViewModel
import com.aitolian.sesyazibench.engine.OnDeviceTranslator
import com.aitolian.sesyazibench.engine.Segment
import com.aitolian.sesyazibench.engine.langOf

/**
 * Tam ekran okuma: metnin tamamı baştan aşağı, zaman damgasız paragraflar,
 * ayarlanabilir yazı boyutu. Tamamı seçilebilir/kopyalanabilir.
 */
@Composable
fun ReaderScreen(s: MainState, vm: MainViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val r = s.result ?: return
    val hasTranslation = s.translation != null
    var showTranslation by rememberSaveable { mutableStateOf(false) }
    val showingTr = showTranslation && hasTranslation
    val segs = if (showingTr) s.translation!! else r.segments
    val text = paragraphs(segs)
    val plain = segs.joinToString(" ") { it.text }
    BackHandler(onBack = onClose)

    Column(Modifier.fillMaxSize().background(SY.Bg).statusBarsPadding().navigationBarsPadding()) {
        // Üst çubuk: geri · başlık · yazı boyutu
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBtn("←", onClose)
            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(r.fileName, color = SY.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val lang = langOf(if (showingTr) s.translationTarget.code else r.language)?.label ?: r.language
                Text("$lang · ${wordCount(plain)} kelime", color = SY.Muted, fontSize = 12.sp)
            }
            IconBtn("A−") { vm.setReaderFont(s.readerFont - 2) }
            IconBtn("A+") { vm.setReaderFont(s.readerFont + 2) }
        }
        if (hasTranslation) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 2.dp).clip(CircleShape).background(SY.Card).padding(3.dp)) {
                listOf(false to "Metin", true to "Çeviri").forEach { (tr, label) ->
                    val sel = tr == showingTr
                    Box(
                        Modifier.weight(1f).clip(CircleShape).background(if (sel) SY.Accent else Color.Transparent)
                            .clickable { showTranslation = tr }.padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text(label, fontSize = 13.5.sp, color = if (sel) SY.OnAccent else SY.Muted) }
                }
            }
        }
        // Metin
        SelectionContainer(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 14.dp)) {
                text.forEach { p ->
                    Text(
                        p, color = SY.Text, fontSize = s.readerFont.sp, lineHeight = (s.readerFont * 1.55f).sp,
                        modifier = Modifier.padding(bottom = (s.readerFont * 0.8f).dp),
                    )
                }
            }
        }
        // Alt eylemler
        Row(
            Modifier.fillMaxWidth().background(SY.Sheet).padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReaderAction("⧉  Kopyala", Modifier.weight(1f)) { copyText(context, plain); vm.toast("Kopyalandı") }
            ReaderAction("↗  Paylaş", Modifier.weight(1f)) { shareText(context, plain) }
            ReaderAction("G  Google Çeviri", Modifier.weight(1.25f)) {
                openGoogleTranslate(context, r.segments.joinToString(" ") { it.text }, r.language, s.translationTarget.code)
            }
        }
    }
}

@Composable
private fun IconBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = SY.Text, fontSize = 17.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun ReaderAction(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(44.dp).clip(RoundedCornerShape(14.dp)).background(SY.Card).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = SY.Accent, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, maxLines = 1) }
}

/** Cümleleri ~3'erli paragraflara böler; okuması kolay, zaman damgasız. */
private fun paragraphs(segs: List<Segment>): List<String> =
    OnDeviceTranslator.toSentences(segs).map { it.text }.chunked(3).map { it.joinToString(" ") }

private fun wordCount(t: String) = t.split(Regex("\\s+")).count { it.isNotBlank() }

/**
 * Metni Google Çeviri uygulamasında açar (ücretsiz, API anahtarı yok; çeviri
 * Google'ın kendi uygulamasında yapılır, internet gerekir). Uygulama yoksa
 * tarayıcıda translate.google.com açılır.
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
