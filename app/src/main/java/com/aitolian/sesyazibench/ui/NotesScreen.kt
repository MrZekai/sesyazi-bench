package com.aitolian.sesyazibench.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.aitolian.sesyazibench.MainState
import com.aitolian.sesyazibench.MainViewModel
import com.aitolian.sesyazibench.data.Transcript
import java.util.Locale

/**
 * Tüm notlar + arama. Arama Türkçe harflere duyarsızdır (ı/i, ş/s, ğ/g…),
 * eşleşen yer kartta vurgulanır. Dokun → aç, basılı tut → sil (geri alınabilir).
 */
@Composable
fun AllNotesScreen(s: MainState, vm: MainViewModel, focusSearch: Boolean = false, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (focusSearch) runCatching { focus.requestFocus() } }
    var query by rememberSaveable { mutableStateOf("") }
    val q = remember(query) { fold(query.trim()) }
    val results = remember(s.history, q) {
        if (q.isEmpty()) s.history.map { it to null }
        else s.history.mapNotNull { t ->
            val idx = fold(t.text).indexOf(q)
            when {
                idx >= 0 -> t to idx
                fold(t.fileName).contains(q) -> t to null
                else -> null
            }
        }
    }

    Column(Modifier.fillMaxSize().background(SY.Bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTap(Icons.AutoMirrored.Filled.ArrowBack, "Geri", onClick = onBack)
            Text("Notlarım", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = SY.Text, modifier = Modifier.weight(1f))
            Text("${s.history.size}/${com.aitolian.sesyazibench.data.HistoryStore.MAX}", color = SY.Muted, fontSize = 12.sp,
                modifier = Modifier.padding(end = 12.dp))
        }
        // Arama kutusu
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).heightIn(min = 50.dp)
                .clip(RoundedCornerShape(25.dp)).background(SY.Sheet)
                .border(1.dp, SY.Outline, RoundedCornerShape(25.dp)).padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = SY.Muted, modifier = Modifier.size(22.dp))
            Box(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                if (query.isEmpty()) Text("Notlarda ara…", color = SY.Muted, fontSize = 15.sp)
                BasicTextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    textStyle = TextStyle(color = SY.Text, fontSize = 15.sp),
                    cursorBrush = SolidColor(SY.Accent),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus).semantics { contentDescription = "Notlarda ara" },
                )
            }
            if (query.isNotEmpty()) IconTap(Icons.Filled.Close, "Aramayı temizle", tint = SY.Muted) { query = "" }
        }
        if (results.isEmpty()) {
            Text(
                if (q.isEmpty()) "Henüz not yok." else "\"$query\" hiçbir notta geçmiyor.",
                color = SY.Muted, fontSize = 14.sp, modifier = Modifier.padding(24.dp),
            )
        }
        UndoRow(s, vm)
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(results, key = { it.first.id }) { (t, hit) ->
                NoteCard(
                    t, onOpen = { vm.openHistory(t) }, onDelete = { vm.deleteHistory(t) },
                    snippet = hit?.let { highlight(t.text, it, q.length) },
                )
            }
        }
        Text(
            "En fazla ${com.aitolian.sesyazibench.data.HistoryStore.MAX} not saklanır; yenisi gelince en eskisi silinir.",
            color = SY.Muted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun UndoRow(s: MainState, vm: MainViewModel) {
    val d = s.undoDeleted ?: return
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
            .background(SY.Card).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Silindi: ${d.preview}", color = SY.Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Box(
            Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = vm::undoDelete)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) { Text("Geri al", color = SY.Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
    }
}

/** Not kartı (Tüm notlar): beyaz yüzey, ince kenarlık. */
@Composable
internal fun NoteCard(t: Transcript, onOpen: () -> Unit, onDelete: () -> Unit, snippet: AnnotatedString? = null) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SY.Sheet)
            .border(1.dp, SY.Outline, RoundedCornerShape(18.dp)),
    ) { NoteRow(t, onOpen, onDelete, snippet) }
}

/** Not satırı: başlık, küçük bilgi satırı, bir-iki satır ön izleme. Dokun → aç, basılı tut → sil. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NoteRow(t: Transcript, onOpen: () -> Unit, onDelete: () -> Unit, snippet: AnnotatedString? = null) {
    Column(
        Modifier.fillMaxWidth()
            .combinedClickable(
                onClickLabel = "Notu aç", onLongClickLabel = "Notu sil",
                onClick = onOpen, onLongClick = onDelete,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            titleOf(t), color = SY.Text, fontSize = 16.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${Transcript.clock(t.durationMs)} · ${noteDate(t)}", color = SY.Muted, fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
        val preview: AnnotatedString? = snippet ?: previewAfterTitle(t)?.let { AnnotatedString(it) }
        if (preview != null) {
            Text(
                preview, color = SY.Muted, fontSize = 14.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/** Başlıktaki (ilk 6) kelimeden sonraki metin; kısa notta tekrar olmasın diye null. */
internal fun previewAfterTitle(t: Transcript): String? {
    val w = t.text.split(Regex("\\s+")).filter { it.isNotBlank() }
    return if (w.size <= 6) null else "…" + w.drop(6).joinToString(" ")
}

private val TR = Locale("tr")

/**
 * Aramada harf farklarını yok say. Karakter sayısını korur (katlanmış metindeki
 * indeks orijinal metinde de geçerli olsun diye tek karakter → tek karakter).
 */
internal fun fold(s: String): String = buildString(s.length) {
    for (c in s) {
        val l = c.toString().lowercase(TR).singleOrNull() ?: c.lowercaseChar()
        append(
            when (l) {
                'ı' -> 'i'; 'ş' -> 's'; 'ğ' -> 'g'; 'ü' -> 'u'; 'ö' -> 'o'; 'ç' -> 'c'
                'â' -> 'a'; 'î' -> 'i'; 'û' -> 'u'
                else -> l
            },
        )
    }
}

/** Eşleşmenin çevresinden kısa bir parça, eşleşen kısım vurgulu. */
private fun highlight(text: String, idx: Int, len: Int): AnnotatedString {
    val from = (idx - 40).coerceAtLeast(0)
    val to = (idx + len + 80).coerceAtMost(text.length)
    return buildAnnotatedString {
        if (from > 0) append("…")
        append(text.substring(from, idx))
        withStyle(SpanStyle(color = SY.Accent, fontWeight = FontWeight.SemiBold)) { append(text.substring(idx, (idx + len).coerceAtMost(text.length))) }
        append(text.substring((idx + len).coerceAtMost(text.length), to))
        if (to < text.length) append("…")
    }
}
