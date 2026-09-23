package com.aitolian.sesyazibench.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.aitolian.sesyazibench.MainState
import com.aitolian.sesyazibench.MainViewModel
import com.aitolian.sesyazibench.Quality
import com.aitolian.sesyazibench.ResultLog
import com.aitolian.sesyazibench.ads.Ads
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.OnDeviceTranslator
import com.aitolian.sesyazibench.engine.TRANSLATABLE
import kotlinx.coroutines.launch

// Yayın öncesi bu uygulamaya özel adreslerle güncellenmeli
private const val PRIVACY_URL = "https://mrzekai.github.io/privacy-policy.html"
private const val SUPPORT_EMAIL = "aitolianrock@gmail.com"

private fun Quality.description() = when (this) {
    Quality.FAST -> "En hızlı. Kısa ve net mesajlar için; doğruluk daha düşük."
    Quality.BALANCED -> "Çoğu mesaj için önerilen. Orta seviye telefonlarda biraz bekletebilir."
    Quality.BEST -> "En doğru. Önce hızlı metin gelir, arka planda iyileştirilir."
}

@Composable
fun SettingsScreen(vm: MainViewModel, s: MainState, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    var refresh by remember { mutableIntStateOf(0) }
    var packs by remember { mutableStateOf<List<Lang>?>(null) }
    var versionTaps by remember { mutableIntStateOf(0) }
    var devMode by remember { mutableStateOf(vm.prefs.devMode) }
    var notify by remember { mutableStateOf(vm.prefs.notifyWhenDone) }

    LaunchedEffect(refresh) { packs = runCatching { OnDeviceTranslator.downloadedLanguages() }.getOrDefault(emptyList()) }

    Column(Modifier.fillMaxSize().background(SY.Bg).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Text("←", fontSize = 22.sp, color = SY.Text)
            }
            Text("Ayarlar", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = SY.Text)
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // --- Transkript ---
            Group("Transkript") {
                LangRow("Varsayılan konuşma dili", s.lang, listOf(Lang.AUTO) + TRANSLATABLE, vm::setDefaultLang)
                Divider()
                Text("Varsayılan kalite", color = SY.Text, fontSize = 14.5.sp, modifier = Modifier.padding(top = 4.dp))
                Quality.entries.forEach { q ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { vm.setDefaultQuality(q) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = s.quality == q, onClick = { vm.setDefaultQuality(q) },
                            colors = RadioButtonDefaults.colors(selectedColor = SY.Accent, unselectedColor = SY.Muted),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(q.label, color = SY.Text, fontSize = 14.sp)
                            Text(q.description(), color = SY.Muted, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- Çeviri ---
            Group("Çeviri") {
                LangRow("Varsayılan çeviri dili", s.translationTarget, TRANSLATABLE, vm::setDefaultTarget)
                Divider()
                Text("İndirilen dil paketleri", color = SY.Text, fontSize = 14.5.sp)
                val list = packs
                when {
                    list == null -> Hint("Yükleniyor…")
                    list.isEmpty() -> Hint("Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB).")
                    else -> list.forEach { l ->
                        ItemRow(l.label, "~30 MB") {
                            TextAction("Sil", SY.Error) {
                                scope.launch { runCatching { OnDeviceTranslator.delete(l) }; refresh++; vm.toast("${l.label} paketi silindi") }
                            }
                        }
                    }
                }
                Hint("Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez.")
            }

            // --- Depolama (modeller) ---
            Group("Depolama") {
                Quality.entries.forEach { q ->
                    val m = q.model
                    val progress = s.modelDownloads[m]
                    val ready = remember(refresh, progress) { vm.modelReady(m) }
                    ItemRow("${q.label} modeli", "${m.approxMb} MB · ${if (ready) "indirildi" else "indirilmedi"}") {
                        when {
                            progress != null -> LinearProgressIndicator(
                                progress = { progress }, modifier = Modifier.width(70.dp), color = SY.Accent, trackColor = SY.Chip,
                            )
                            ready -> TextAction("Sil", SY.Error) { vm.deleteModel(m); refresh++ }
                            else -> TextAction("İndir", SY.Accent) { vm.downloadModel(m) }
                        }
                    }
                }
                Hint("Modeller Wi‑Fi'da indirmen önerilir. Silinen model gerektiğinde tekrar indirilir.")
            }

            // --- Bildirimler ---
            Group("Bildirimler") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("İşlem bitince haber ver", color = SY.Text, fontSize = 14.sp)
                        Text("Uygulamadan çıktığında uzun dökümler bitince bildirim gelir.", color = SY.Muted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = notify,
                        onCheckedChange = { notify = it; vm.prefs.notifyWhenDone = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = SY.Accent, checkedThumbColor = SY.OnAccent),
                    )
                }
            }

            // --- Gizlilik ---
            Group("Gizlilik") {
                Text(
                    "Sesin ve metnin telefonundan çıkmaz. Yazıya dökme ve çeviri tamamen cihazda yapılır. " +
                        "Reklamlar Google AdMob tarafından gösterilir.",
                    color = SY.Muted, fontSize = 12.5.sp,
                )
                LinkRow("Gizlilik politikası") { openUrl(context, PRIVACY_URL) }
                if (activity != null && Ads.privacyOptionsRequired(activity)) {
                    LinkRow("Reklam tercihleri") { Ads.showPrivacyOptions(activity) }
                }
            }

            // --- Hakkında ---
            Group("Hakkında") {
                ItemRow("Sürüm", appVersion(context), modifier = Modifier.clickable {
                    versionTaps++
                    if (versionTaps >= 7 && !devMode) { devMode = true; vm.prefs.devMode = true; vm.toast("Geliştirici araçları açıldı") }
                }) {}
                LinkRow("Geri bildirim gönder") {
                    val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL"))
                        .putExtra(Intent.EXTRA_SUBJECT, context.getString(com.aitolian.sesyazibench.R.string.app_name) + " geri bildirim (${appVersion(context)})")
                    runCatching { context.startActivity(i) }
                }
                LinkRow("Uygulamayı puanla") {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))) }
                        .onFailure { openUrl(context, "https://play.google.com/store/apps/details?id=${context.packageName}") }
                }
            }

            if (devMode) DevTools(vm, s)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DevTools(vm: MainViewModel, s: MainState) {
    val context = LocalContext.current
    var pendingAdvanced by remember { mutableStateOf(false) }
    // ML Kit BASIC, dosyadan okusa bile Android tanıyıcısı mikrofon izni istiyor
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.runMlKitTest(pendingAdvanced) else vm.toast("ML Kit testi için mikrofon izni gerekli")
    }
    fun runMlKit(advanced: Boolean) {
        pendingAdvanced = advanced
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            vm.runMlKitTest(advanced)
        } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }
    Group("Geliştirici") {
        Text(vm.deviceInfo(), fontSize = 11.5.sp, color = SY.Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("ML Kit Basic", bg = SY.Chip, fg = SY.Text, onClick = { runMlKit(false) })
            Pill("ML Kit Advanced", bg = SY.Chip, fg = SY.Text, onClick = { runMlKit(true) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("CSV paylaş", bg = SY.Chip, fg = SY.Text, onClick = { shareCsv(context) })
            Pill("CSV sil", bg = SY.Chip, fg = SY.Text, onClick = { ResultLog.clear(context); vm.toast("Kayıtlar silindi") })
            Pill("Kapat", bg = SY.Chip, fg = SY.Text, onClick = { vm.prefs.devMode = false; vm.toast("Ayarlar'ı yeniden aç") })
        }
        s.testLog.forEach { Text(it, fontSize = 12.sp, color = SY.Text) }
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title.uppercase(), color = SY.Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SY.Sheet).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { content() }
    }
}

@Composable
private fun LangRow(title: String, value: Lang, options: List<Lang>, onSelect: (Lang) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = SY.Text, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Box {
            Pill("${value.label} ▾", bg = SY.Chip, fg = SY.Text, onClick = { open = true })
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { l ->
                    DropdownMenuItem(text = { Text(l.label) }, onClick = { open = false; onSelect(l) })
                }
            }
        }
    }
}

@Composable
private fun ItemRow(title: String, sub: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = SY.Text, fontSize = 14.sp)
            Text(sub, color = SY.Muted, fontSize = 12.sp)
        }
        trailing()
    }
}

@Composable
private fun LinkRow(title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = SY.Text, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text("›", color = SY.Muted, fontSize = 18.sp)
    }
}

@Composable
private fun TextAction(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Text(label, color = color, fontSize = 13.5.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp))
}

@Composable
private fun Hint(text: String) = Text(text, color = SY.Muted, fontSize = 12.sp)

@Composable
private fun Divider() = HorizontalDivider(color = SY.Card)

private fun appVersion(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "?"

private fun openUrl(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private fun shareCsv(context: Context) {
    val f = ResultLog.file(context)
    if (!f.exists()) return
    val uri = FileProvider.getUriForFile(context, context.packageName + ".files", f)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Sonuçları paylaş"))
}
