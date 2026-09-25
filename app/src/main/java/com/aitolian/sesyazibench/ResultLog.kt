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

/**
 * Süre günlüğü (yalnızca geliştirici modunda yazılır). Ses ve metin İÇERMEZ.
 */
object ResultLog {
    private const val HEADER =
        "zaman,cihaz,motor,dil,ses_sn,ice_aktarma_ms,stt_ms,parca,ilk_cumle_ms,ilk_gorunen_metin_ms,uctan_uca_ms,rtf_uctan_uca,parca_sayisi,hata\n"

    fun file(context: Context) = File(File(context.filesDir, "results").apply { mkdirs() }, "timings-v4.csv")

    fun append(context: Context, r: EngineResult, importMs: Long, firstVisibleMs: Long, totalMs: Long, cleanCount: Int) {
        val f = file(context)
        if (!f.exists()) f.writeText(HEADER)
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val row = listOf(
            now, DeviceInfo.summary(context), r.engine, r.language, "%.1f".format(Locale.US, r.audioMs / 1000.0),
            importMs.toString(), r.transcribeMs.toString(), r.windows.toString(), r.firstSegmentMs.toString(),
            firstVisibleMs.toString(), totalMs.toString(),
            "%.3f".format(Locale.US, if (r.audioMs > 0) totalMs.toDouble() / r.audioMs else 0.0),
            cleanCount.toString(), r.error ?: "",
        ).joinToString(",") { csv(it) }
        f.appendText(row + "\n")
    }

    /** Ekranda gösterilecek tek satırlık özet (saniye). */
    fun summary(r: EngineResult, importMs: Long, firstVisibleMs: Long, totalMs: Long, cleanCount: Int): String {
        fun s(ms: Long) = if (ms < 0) "-" else "%.1f".format(Locale.US, ms / 1000.0)
        return "Wit ${r.language} · ses ${s(r.audioMs)} sn · içe aktarma ${s(importMs)} · internet ${s(r.transcribeMs)} " +
            "(${r.windows} parça) · ilk metin ${s(firstVisibleMs)} · toplam ${s(totalMs)} sn · $cleanCount cümle" +
            (r.error?.let { " · HATA $it" } ?: "")
    }

    fun clear(context: Context) {
        File(context.filesDir, "results").deleteRecursively()
    }

    private fun csv(s: String) = "\"" + s.replace("\"", "\"\"").replace('\n', ' ') + "\""
}
