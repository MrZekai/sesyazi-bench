package com.aitolian.sesyazibench.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/** Ayarlar: modeller + geliştirici test araçları (ana ekranı kirletmesin diye burada). */
@Composable
fun SettingsDialog(vm: MainViewModel, s: MainState, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    var pendingAdvanced by remember { mutableStateOf(false) }
    // ML Kit BASIC, dosyadan okusa bile Android tanıyıcısı mikrofon izni istiyor
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.runMlKitTest(pendingAdvanced) else vm.toast("ML Kit testi için mikrofon izni gerekli")
    }
    fun runMlKit(advanced: Boolean) {
        pendingAdvanced = advanced
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            vm.runMlKitTest(advanced)
        } else {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SY.Sheet,
        titleContentColor = SY.Text,
        textContentColor = SY.Text,
        title = { Text("Ayarlar") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Modeller", fontWeight = FontWeight.SemiBold, color = SY.Accent)
                key(refresh) {
                    Quality.entries.forEach { q ->
                        val ready = vm.modelReady(q.model)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${q.label} · ${q.model.label} · ~${q.model.approxMb} MB",
                                fontSize = 13.sp, modifier = Modifier.weight(1f),
                            )
                            if (ready) {
                                TextButton(onClick = { vm.deleteModel(q.model); refresh++ }) { Text("Sil", color = SY.Error) }
                            } else {
                                Text("indirilmedi", fontSize = 12.sp, color = SY.Muted)
                            }
                        }
                    }
                }
                Text(
                    "Modeller ilk kullanımda otomatik indirilir. Ses hiçbir sunucuya gönderilmez.",
                    fontSize = 12.sp, color = SY.Muted,
                )

                HorizontalDivider(color = SY.Card)
                Text("Test araçları (geliştirici)", fontWeight = FontWeight.SemiBold, color = SY.Accent)
                Text(vm.deviceInfo(), fontSize = 11.5.sp, color = SY.Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("ML Kit Basic", bg = SY.Chip, fg = SY.Text, onClick = { runMlKit(false) })
                    Pill("ML Kit Advanced", bg = SY.Chip, fg = SY.Text, onClick = { runMlKit(true) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("CSV paylaş", bg = SY.Chip, fg = SY.Text, onClick = { shareCsv(context) })
                    Pill("CSV sil", bg = SY.Chip, fg = SY.Text, onClick = { ResultLog.clear(context); vm.toast("Kayıtlar silindi") })
                }
                if (s.testLog.isNotEmpty()) {
                    Column(Modifier.fillMaxWidth().padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        s.testLog.forEach { Text(it, fontSize = 12.sp, color = SY.Text) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat", color = SY.Accent) } },
    )
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
