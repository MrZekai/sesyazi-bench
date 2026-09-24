package com.aitolian.sesyazibench.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitolian.sesyazibench.R

/** En az 48 dp dokunma alanlı ikon düğmesi (görünen ikon daha küçük olabilir). */
@Composable
fun IconTap(
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    tint: Color = SY.Text,
    bg: Color? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier.size(48.dp).clip(CircleShape)
            .clickable(enabled = enabled, onClickLabel = description, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).then(if (bg != null) Modifier.background(bg) else Modifier),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = if (enabled) tint else tint.copy(alpha = .4f), modifier = Modifier.size(22.dp)) }
    }
}

@Composable
fun Pill(
    text: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    bold: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier.heightIn(min = 40.dp).clip(CircleShape).background(bg).clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.padding(end = 6.dp).size(18.dp))
        Text(
            text, color = fg, fontSize = 13.5.sp, maxLines = 1,
            fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

/** Seçeneklerden biri seçili, eşit genişlikte düğme grubu (tema, satır aralığı, görünüm). */
@Composable
fun Segmented(options: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    Row(modifier.fillMaxWidth().clip(CircleShape).background(SY.Card).padding(3.dp)) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                Modifier.weight(1f).heightIn(min = 44.dp).clip(CircleShape)
                    .background(if (sel) SY.Accent else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelect(i) }
                    .semantics { this.selected = sel }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label, fontSize = 13.5.sp, maxLines = 1, textAlign = TextAlign.Center,
                    color = if (sel) SY.OnAccent else SY.Text, fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

/** Dalga formu; dokunulan yere atlamak için [onSeek] (0..1) verilebilir. */
@Composable
internal fun Waveform(peaks: FloatArray, progress: Float, modifier: Modifier, onSeek: ((Float) -> Unit)? = null) {
    val seek by rememberUpdatedState(onSeek)
    val tap = if (onSeek != null) Modifier.pointerInput(Unit) {
        detectTapGestures { o -> if (size.width > 0) seek?.invoke((o.x / size.width).coerceIn(0f, 1f)) }
    } else Modifier
    val active = SY.Accent
    val idle = SY.Track
    Canvas(modifier.then(tap)) {
        val n = if (peaks.isEmpty()) 46 else peaks.size
        val step = size.width / n
        val bw = (step * 0.5f).coerceAtLeast(2f)
        for (i in 0 until n) {
            val v = if (peaks.isEmpty()) 0.15f else peaks[i].coerceIn(0.08f, 1f)
            val h = size.height * v
            val color = if (progress > 0f && i.toFloat() / n <= progress) active else idle
            drawRoundRect(color, Offset(i * step, (size.height - h) / 2), Size(bw, h), CornerRadius(bw / 2))
        }
    }
}

/** Marka işareti: ses dalgası (düğme değil, yalnızca kimlik öğesi). */
@Composable
internal fun WaveMark(color: Color, markSize: Dp) {
    Canvas(Modifier.size(markSize)) {
        val w = size.width
        val bar = w / 10f
        val heights = listOf(0.3f, 0.6f, 0.95f, 0.5f, 0.75f)
        heights.forEachIndexed { i, h ->
            val x = w * 0.08f + i * bar * 1.9f
            val hh = size.height * h
            drawRoundRect(color, Offset(x, (size.height - hh) / 2), Size(bar, hh), CornerRadius(bar / 2))
        }
    }
}

internal fun copyText(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), text))
}

internal fun shareText(context: Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    context.startActivity(Intent.createChooser(i, "Paylaş"))
}
