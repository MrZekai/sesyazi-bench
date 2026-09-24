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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import com.aitolian.sesyazibench.engine.WhisperModel
import kotlinx.coroutines.launch

// Yayın öncesi bu uygulamaya özel adreslerle güncellenmeli
private const val PRIVACY_URL = "https://mrzekai.github.io/privacy-policy.html"
private const val SUPPORT_EMAIL = "aitolianrock@gmail.com"

private const val LICENSES =
    "• whisper.cpp / ggml — MIT Lisansı, © Georgi Gerganov ve katkıda bulunanlar\n" +
    "• OpenAI Whisper model ağırlıkları — MIT Lisansı, © OpenAI\n" +
    "• Silero VAD — MIT Lisansı, © Silero Team\n" +
    "• Google ML Kit (Çeviri, Konuşma) — Google APIs Hizmet Şartları\n" +
    "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" +
    "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0"

private fun Quality.description() = when (this) {
    Quality.FAST -> "En hızlı. Kısa ve net mesajlar için; doğruluk daha düşük. Dil algılamada da kullanılır."
    Quality.BALANCED -> "Çoğu mesaj için önerilen. Orta seviye telefonlarda biraz bekletebilir."
    Quality.BEST -> "En doğru. Önce Dengeli/Hızlı ön izleme gelir, arka planda büyük modelle iyileştirilir."
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
    var confirmClear by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }

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
                LinkRow("Tüm notları sil (${s.history.size})") { confirmClear = true }
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
                LinkRow("Açık kaynak lisansları") { showLicenses = true }
                LinkRow("Uygulamayı puanla") {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))) }
                        .onFailure { openUrl(context, "https://play.google.com/store/apps/details?id=${context.packageName}") }
                }
            }

            if (devMode) DevTools(vm, s, onClose = { devMode = false })
            if (confirmClear) {
                AlertDialog(
                    onDismissRequest = { confirmClear = false },
                    containerColor = SY.Sheet, titleContentColor = SY.Text, textContentColor = SY.Muted,
                    title = { Text("Tüm notlar silinsin mi?") },
                    text = { Text("Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz.") },
                    confirmButton = { TextButton(onClick = { vm.clearHistory(); confirmClear = false }) { Text("Sil", color = SY.Error) } },
                    dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Vazgeç", color = SY.Accent) } },
                )
            }
            if (showLicenses) {
                AlertDialog(
                    onDismissRequest = { showLicenses = false },
                    containerColor = SY.Sheet, titleContentColor = SY.Text, textContentColor = SY.Muted,
                    title = { Text("Açık kaynak lisansları") },
                    text = { Text(LICENSES, fontSize = 12.5.sp) },
                    confirmButton = { TextButton(onClick = { showLicenses = false }) { Text("Kapat", color = SY.Accent) } },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DevTools(vm: MainViewModel, s: MainState, onClose: () -> Unit) {
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
    val info = remember { vm.deviceInfo() }
    var threads by remember { mutableIntStateOf(vm.prefs.threadOverride) }
    var fallbackMode by remember { mutableIntStateOf(vm.prefs.fallbackMode) }
    var bestPreview by remember { mutableStateOf(vm.prefs.bestPreview) }
    var turboQ8 by remember { mutableStateOf(vm.prefs.turboQ8) }
    var q8Refresh by remember { mutableIntStateOf(0) }
    val q8Progress = s.modelDownloads[WhisperModel.TURBO_Q8]
    val q8Ready = remember(q8Refresh, q8Progress) { vm.modelReady(WhisperModel.TURBO_Q8) }
    Group("Geliştirici") {
        Text(info, fontSize = 11.5.sp, color = SY.Muted)
        // Son dökümün aşama süreleri — hızın nerede kaybolduğunu gösterir
        Text("Son döküm süreleri", color = SY.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(s.lastTiming ?: "Henüz ölçüm yok. Bir ses dök.", fontSize = 12.sp, color = SY.Text)
        Text("Thread sayısı (A/B ölçümü)", color = SY.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0, 2, 3, 4, 6).forEach { n ->
                val sel = threads == n
                Pill(
                    if (n == 0) "Oto" else "$n", bg = if (sel) SY.Accent else SY.Chip, fg = if (sel) SY.OnAccent else SY.Text,
                    onClick = { threads = n; vm.setThreadOverride(n) },
                )
            }
        }

        // --- Hız/doğruluk deneyleri: her ölçüm satırına hangi ayarla çalışıldığı yazılır ---
        Text("Tekrar deneme (fallback)", color = SY.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text("Oto: Hızlı'da kapalı, Dengeli ve En iyi'de açık.", fontSize = 12.sp, color = SY.Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0 to "Oto", 1 to "Hep açık", 2 to "Hep kapalı").forEach { (m, label) ->
                val sel = fallbackMode == m
                Pill(label, bg = if (sel) SY.Accent else SY.Chip, fg = if (sel) SY.OnAccent else SY.Text,
                    onClick = { fallbackMode = m; vm.setFallbackMode(m) })
            }
        }
        Text("En iyi modu", color = SY.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(true to "Ön izlemeli", false to "Doğrudan büyük model").forEach { (on, label) ->
                val sel = bestPreview == on
                Pill(label, bg = if (sel) SY.Accent else SY.Chip, fg = if (sel) SY.OnAccent else SY.Text,
                    onClick = { bestPreview = on; vm.setBestPreview(on) })
            }
        }
        Text("En iyi modeli", color = SY.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(false to "q5_0 (547 MB)", true to "q8_0 (834 MB)").forEach { (on, label) ->
                val sel = turboQ8 == on
                Pill(label, bg = if (sel) SY.Accent else SY.Chip, fg = if (sel) SY.OnAccent else SY.Text,
                    onClick = { turboQ8 = on; vm.setTurboQ8(on) })
            }
        }
        ItemRow("large-v3-turbo q8_0", "${WhisperModel.TURBO_Q8.approxMb} MB · ${if (q8Ready) "indirildi" else "indirilmedi"}") {
            when {
                q8Progress != null -> LinearProgressIndicator(
                    progress = { q8Progress }, modifier = Modifier.width(70.dp), color = SY.Accent, trackColor = SY.Chip,
                )
                q8Ready -> TextAction("Sil", SY.Error) { vm.deleteModel(WhisperModel.TURBO_Q8); q8Refresh++ }
                else -> TextAction("İndir", SY.Accent) { vm.downloadModel(WhisperModel.TURBO_Q8) }
            }
        }
        Text(
            "Karşılaştırma için dili Türkçe seç, aynı sesi her ayarla en az 3 kez dök. İlk tur (model yükleme) soğuk sayılır.",
            fontSize = 12.sp, color = SY.Muted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("ML Kit Basic", bg = SY.Chip, fg = SY.Text, onClick = { runMlKit(false) })
            Pill("ML Kit Advanced", bg = SY.Chip, fg = SY.Text, onClick = { runMlKit(true) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("Süreleri paylaş", bg = SY.Chip, fg = SY.Text, onClick = {
                if (!shareCsv(context)) vm.toast("Henüz kayıt yok (geliştirici modu açıkken dökülen sesler kaydedilir)")
            })
            Pill("Sil", bg = SY.Chip, fg = SY.Text, onClick = { ResultLog.clear(context); vm.toast("Kayıtlar silindi") })
            Pill("Kapat", bg = SY.Chip, fg = SY.Text, onClick = { vm.disableDevMode(); onClose() })
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

private fun shareCsv(context: Context): Boolean {
    val f = ResultLog.file(context)
    if (!f.exists()) return false
    val uri = FileProvider.getUriForFile(context, context.packageName + ".files", f)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Süreleri paylaş"))
    return true
}
