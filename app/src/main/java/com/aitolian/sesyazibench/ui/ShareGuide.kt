package com.aitolian.sesyazibench.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitolian.sesyazibench.R

private val WaGreen = Color(0xFF25D366)
private val WaBubble = Color(0xFF1F5C4A)
private val WaDark = Color(0xFF0B141A)
private val WaBar = Color(0xFF1F2C33)

/**
 * Ana giriş: WhatsApp'ın kendi "Paylaş" menüsü. Klasör izni yok; kullanıcı
 * 3 adımı görsel olarak görür ve tek dokunuşla WhatsApp'a geçer.
 */
@Composable
fun ShareGuide(onOpenWhatsApp: () -> Unit, onOtherFile: () -> Unit) {
    val app = stringResource(R.string.app_name)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "NASIL KULLANILIR", color = SY.Accent, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp),
        )
        Step(1, "Sesli mesaja uzun bas", "WhatsApp sohbetinde, mesaj seçilene kadar") { IllLongPress() }
        Step(2, "Paylaş simgesine dokun", "Üst çubukta (bazı telefonlarda ⋮ menüsünde)") { IllShareIcon() }
        Step(3, "Listeden $app uygulamasını seç", "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur.") {
            IllShareSheet(app)
        }
        Box(
            Modifier.padding(top = 4.dp).fillMaxWidth().height(50.dp).clip(CircleShape).background(WaGreen)
                .clickable(onClick = onOpenWhatsApp),
            contentAlignment = Alignment.Center,
        ) { Text("💬  WhatsApp'ı aç", color = Color(0xFF062B16), fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        Text(
            "Başka bir ses / video dosyası seç", color = SY.Accent, fontSize = 13.5.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onOtherFile).padding(8.dp),
        )
    }
}

@Composable
private fun Step(n: Int, title: String, sub: String, illustration: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SY.Card).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(SY.Accent), contentAlignment = Alignment.Center) {
            Text("$n", color = SY.OnAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, color = SY.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(sub, color = SY.Muted, fontSize = 12.sp, lineHeight = 15.sp)
        }
        Box(Modifier.width(112.dp).height(62.dp).clip(RoundedCornerShape(12.dp)).background(WaDark)) { illustration() }
    }
}

/** Seçili (çerçeveli) sesli mesaj balonu + parmak. */
@Composable
private fun IllLongPress() {
    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier.align(Alignment.Center).width(92.dp).height(30.dp).clip(RoundedCornerShape(10.dp))
                .background(WaBubble).border(2.dp, SY.Accent, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("▶", color = Color(0xFFD7E6DF), fontSize = 10.sp)
            Canvas(Modifier.padding(start = 6.dp).width(52.dp).height(16.dp)) {
                val hs = listOf(3, 5, 2, 6, 4, 7, 3, 5, 2, 4, 6, 3)
                hs.forEachIndexed { i, h ->
                    val hh = h * 2f * density
                    drawRoundRect(Color(0xFF8FA3AD), Offset(i * 4f * density, (size.height - hh) / 2),
                        Size(2f * density, hh), CornerRadius(density))
                }
            }
        }
        Text("👆", fontSize = 18.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp))
    }
}

/** WhatsApp seçim çubuğu; Paylaş simgesi vurgulu. */
@Composable
private fun IllShareIcon() {
    Row(
        Modifier.fillMaxWidth().height(28.dp).background(WaBar).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("★", color = Color(0xFFAEBAC1), fontSize = 12.sp)
        Text("🗑", fontSize = 11.sp)
        Box(Modifier.size(22.dp).clip(CircleShape).background(SY.Accent), contentAlignment = Alignment.Center) {
            Text("↗", color = SY.OnAccent, fontSize = 12.sp)
        }
        Text("⋮", color = Color(0xFFAEBAC1), fontSize = 13.sp)
    }
}

/** Android paylaş listesi; bu uygulama ilk sırada vurgulu. */
@Composable
private fun IllShareSheet(app: String) {
    Row(
        Modifier.fillMaxSize().background(Color(0xFF2A2A2E)).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShareTarget(app, Brush.sweepGradient(listOf(SY.A1, SY.A2, SY.A1)), highlighted = true)
        ShareTarget("Drive", Brush.linearGradient(listOf(Color(0xFF4285F4), Color(0xFF4285F4))), false)
        ShareTarget("Gmail", Brush.linearGradient(listOf(Color(0xFFEA4335), Color(0xFFEA4335))), false)
    }
}

@Composable
private fun ShareTarget(label: String, brush: Brush, highlighted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(if (highlighted) 1f else 0.5f)) {
        Box(
            Modifier.size(26.dp).clip(CircleShape).background(brush)
                .then(if (highlighted) Modifier.border(2.dp, Color.White, CircleShape) else Modifier),
        )
        Text(label, color = Color.White, fontSize = 8.sp, maxLines = 1, modifier = Modifier.padding(top = 2.dp))
    }
}
