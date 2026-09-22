package com.aitolian.sesyazibench.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitolian.sesyazibench.BenchViewModel
import com.aitolian.sesyazibench.DeviceInfo
import com.aitolian.sesyazibench.ResultLog
import com.aitolian.sesyazibench.engine.EngineResult
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.WhisperEngine
import com.aitolian.sesyazibench.engine.WhisperModel
import java.util.Locale

private val Pass = Color(0xFF15803D)
private val Fail = Color(0xFFB91C1C)

@Composable
fun BenchScreen(vm: BenchViewModel) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        val s by vm.state.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val snackbar = remember { SnackbarHostState() }
        val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let(vm::load)
        }
        LaunchedEffect(s.message) {
            s.message?.let { snackbar.showSnackbar(it); vm.dismissMessage() }
        }

        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { pad ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("SesYazı Bench", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Hedef: 60 sn ses ≤ 15 sn (RTF ≤ 0,25)",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        DeviceInfo.summary(context) + " | whisper thread: ${WhisperEngine.threadCount()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                item {
                    Section("1 · Ses") {
                        Text(
                            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → SesYazı Bench. Ya da dosya seç.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Button(onClick = { picker.launch(arrayOf("audio/*", "video/*", "application/ogg")) }) {
                            Text("Dosya seç")
                        }
                        val a = s.audio
                        if (a != null) {
                            Text("${s.fileLabel} · ${fmtSec(a.durationMs)} sn · ${a.sourceMime} " +
                                "${a.sourceRate} Hz ${a.sourceChannels} kanal · çözme ${s.decodeMs} ms")
                        }
                    }
                }

                item {
                    Section("2 · Dil") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(Lang.entries) { l ->
                                FilterChip(selected = s.lang == l, onClick = { vm.setLang(l) }, label = { Text(l.label) })
                            }
                        }
                    }
                }

                item {
                    Section("3 · Whisper modeli (tek seferlik indirme)") {
                        WhisperModel.entries.forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = s.model == m, onClick = { vm.setModel(m) })
                                Text("${m.label} · ~${m.approxMb} MB", modifier = Modifier.weight(1f))
                                val p = s.downloadProgress[m]
                                when {
                                    p != null -> LinearProgressIndicator(progress = { p }, modifier = Modifier.width(80.dp))
                                    m in s.modelsReady -> Text("hazır ✓", color = Pass)
                                    else -> TextButton(onClick = { vm.download(m) }) { Text("İndir") }
                                }
                            }
                        }
                    }
                }

                item {
                    Section("4 · Çalıştır") {
                        val enabled = s.busy == null && s.audio != null
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = vm::runWhisper, enabled = enabled && s.model in s.modelsReady) { Text("Whisper") }
                            OutlinedButton(onClick = { vm.runMlKit(false) }, enabled = enabled) { Text("ML Kit") }
                            OutlinedButton(onClick = { vm.runMlKit(true) }, enabled = enabled) { Text("ML Kit+") }
                        }
                        Button(onClick = vm::runAll, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                            Text("Hepsini sırayla çalıştır")
                        }
                        s.busy?.let {
                            Text(it)
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                items(s.results) { r -> ResultCard(r) }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { shareCsv(context) }) { Text("Sonuçları paylaş (CSV)") }
                        TextButton(onClick = { ResultLog.clear(context) }) { Text("Kayıtları sil") }
                    }
                    Spacer(Modifier.padding(24.dp))
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun ResultCard(r: EngineResult) {
    val ok = r.passesTarget
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${r.engine} · ${r.variant}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    when {
                        r.error != null -> "HATA"
                        ok -> "GEÇTİ"
                        else -> "YAVAŞ"
                    },
                    color = if (ok) Pass else Fail,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                "Ses ${fmtSec(r.audioMs)} sn · yükleme ${r.loadMs} ms · işlem ${fmtSec(r.transcribeMs)} sn · " +
                    "RTF ${"%.2f".format(Locale.US, r.rtf)}" +
                    (r.detectedLanguage?.let { " · dil: $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
            )
            r.error?.let { Text(it, color = Fail, style = MaterialTheme.typography.bodySmall) }
            if (r.text.isNotBlank()) SelectionContainer { Text(r.text) }
        }
    }
}

private fun fmtSec(ms: Long) = "%.1f".format(Locale.US, ms / 1000.0)

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
