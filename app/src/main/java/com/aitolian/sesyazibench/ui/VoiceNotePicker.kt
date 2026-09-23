package com.aitolian.sesyazibench.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitolian.sesyazibench.MainState
import com.aitolian.sesyazibench.data.VoiceNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ana giriş: WhatsApp sesli mesajları. İzin yoksa tek seferlik izin kartı,
 * varsa son sesli mesajlar listesi. Genel dosya seçici yalnızca yan seçenek.
 */
@Composable
fun VoiceNotePicker(
    s: MainState,
    onPick: (VoiceNote) -> Unit,
    onGrant: () -> Unit,
    onOtherFile: () -> Unit,
    onRefresh: () -> Unit,
    maxItems: Int = 12,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!s.waGranted) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SY.Card).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("WhatsApp sesli mesajların burada listelensin", color = SY.Text, fontSize = 15.sp,
                    fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                Text(
                    "Bir kez izin ver: açılan ekranda \"WhatsApp Voice Notes\" klasörünü onayla. " +
                        "Sadece bu klasör okunur, başka hiçbir dosyana erişilmez.",
                    color = SY.Muted, fontSize = 12.5.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
                )
                Pill("Erişim ver", bg = SY.Accent, fg = SY.OnAccent, onClick = onGrant)
                Text(
                    "ya da WhatsApp'ta sesli mesaja uzun bas → Paylaş → SesYazı",
                    color = SY.Muted, fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text("Son sesli mesajlar", color = SY.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f))
                if (s.voiceNotesLoading) CircularProgressIndicator(Modifier.size(16.dp), color = SY.Accent, strokeWidth = 2.dp)
                else Text("↻ Yenile", color = SY.Accent, fontSize = 13.sp,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onRefresh).padding(6.dp))
            }
            if (!s.voiceNotesLoading && s.voiceNotes.isEmpty()) {
                Text(
                    "Sesli mesaj bulunamadı. WhatsApp'ta bir sesli mesaj alıp tekrar dene.",
                    color = SY.Muted, fontSize = 12.5.sp, modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            s.voiceNotes.take(maxItems).forEach { n -> VoiceNoteRow(n) { onPick(n) } }
        }
        Text(
            "Başka bir ses / video dosyası seç",
            color = SY.Accent, fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onOtherFile).padding(8.dp),
        )
    }
}

private val dateFmt = SimpleDateFormat("d MMM · HH:mm", Locale("tr"))

@Composable
private fun VoiceNoteRow(n: VoiceNote, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SY.Card).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(SY.Chip), contentAlignment = Alignment.Center) {
            Text("🎤", fontSize = 15.sp)
        }
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(dateFmt.format(Date(n.modified)), color = SY.Text, fontSize = 14.sp)
            Text(n.name, color = SY.Muted, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("~%d:%02d".format(n.approxSec / 60, n.approxSec % 60), color = SY.Muted, fontSize = 12.sp)
        Text("  Yazıya dök ›", color = SY.Accent, fontSize = 12.5.sp)
    }
}
