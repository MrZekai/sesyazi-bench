package com.aitolian.sesyazibench

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.aitolian.sesyazibench.engine.EngineResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DeviceInfo {
    fun summary(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val soc = if (Build.VERSION.SDK_INT >= 31) "${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL}" else Build.HARDWARE
        return "${Build.MANUFACTURER} ${Build.MODEL} | Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) | " +
            "SoC $soc | RAM ${mi.totalMem / 1_048_576} MB | CPU ${Runtime.getRuntime().availableProcessors()} çekirdek"
    }
}

/** Her sonucu uygulama içi CSV'ye ekler; "Sonuçları paylaş" bu dosyayı gönderir. */
object ResultLog {
    private const val HEADER =
        "zaman,cihaz,dosya,motor,varyant,dil,algilanan_dil,ses_sn,yukleme_ms,islem_ms,rtf,hedef,hata,metin\n"

    fun file(context: Context) = File(File(context.filesDir, "results").apply { mkdirs() }, "benchmark.csv")

    fun append(context: Context, fileLabel: String, r: EngineResult) {
        val f = file(context)
        if (!f.exists()) f.writeText(HEADER)
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val row = listOf(
            now, DeviceInfo.summary(context), fileLabel, r.engine, r.variant, r.language,
            r.detectedLanguage ?: "", "%.1f".format(Locale.US, r.audioMs / 1000.0),
            r.loadMs.toString(), r.transcribeMs.toString(), "%.3f".format(Locale.US, r.rtf),
            if (r.passesTarget) "GECTI" else "KALDI", r.error ?: "", r.text,
        ).joinToString(",") { csv(it) }
        f.appendText(row + "\n")
    }

    fun clear(context: Context) { file(context).delete() }

    private fun csv(s: String) = "\"" + s.replace("\"", "\"\"").replace('\n', ' ') + "\""
}
