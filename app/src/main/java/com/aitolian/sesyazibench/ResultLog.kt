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
 * Aşama süreleri günlüğü (yalnızca geliştirici modunda yazılır). Ses ve metin
 * İÇERMEZ — sadece süreler, model ve cihaz bilgisi. "Süreleri paylaş" bu
 * dosyayı gönderir.
 */
object ResultLog {
    private const val HEADER =
        "zaman,cihaz,cpu,thread,motor,model,mod,tur,secilen_dil,algilanan_dil,dil_algilama_yolu,fallback,ses_sn," +
            "ice_aktarma_ms,model_yukleme_ms,dil_algilama_ms,encoder_ms,pencere,decoder_ve_diger_ms,stt_ms," +
            "ilk_cumle_ms,ilk_gorunen_metin_ms,uctan_uca_ms,rtf_stt,rtf_uctan_uca,ham_parca,on_filtre_sonrasi_parca,temiz_parca,hata\n"

    fun file(context: Context) = File(File(context.filesDir, "results").apply { mkdirs() }, "timings-v3.csv")

    /**
     * @param importMs dosya kopyalama + çözme (yalnızca ilk turda)
     * @param firstVisibleMs oturum başından ilk cümlenin ekrana geldiği ana (-1: yok)
     * @param totalMs oturum başından bu turun bittiği ana
     * @param cleanCount son temizlikten sonra kalan parça sayısı.
     * Ham sayı r.rawSegmentCount; r.segments.size ilk gürültü filtresinden sonradır.
     * Bunlar kelime hata oranı değildir; metin içeriği günlüğe yazılmaz.
     *
     * dil_algilama_yolu: "secili" = kullanıcı dili seçti (algılama yok), "base" = ayrı
     * küçük modelle (süresi dil_algilama_ms'te), "model_ici" = büyük modelin içinde
     * (süresi stt_ms'e DAHİL, dil_algilama_ms 0 görünür).
     */
    fun append(
        context: Context, r: EngineResult, mode: String, pass: String, fallback: Boolean,
        importMs: Long, firstVisibleMs: Long, totalMs: Long, cleanCount: Int,
    ) {
        val f = file(context)
        if (!f.exists()) f.writeText(HEADER)
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val audioS = r.audioMs / 1000.0
        val row = listOf(
            now, DeviceInfo.summary(context), com.aitolian.sesyazibench.engine.WhisperEngine.cpuReport(),
            r.threads.toString(), r.engine, r.variant, mode, pass, r.language, r.detectedLanguage ?: "",
            r.detectPath, if (fallback) "acik" else "kapali", "%.1f".format(Locale.US, audioS),
            importMs.toString(), r.loadMs.toString(), r.detectMs.toString(), r.encodeMs.toString(),
            r.windows.toString(), r.decodeMs.toString(), r.transcribeMs.toString(), r.firstSegmentMs.toString(),
            firstVisibleMs.toString(), totalMs.toString(),
            "%.3f".format(Locale.US, r.rtf),
            "%.3f".format(Locale.US, if (r.audioMs > 0) totalMs.toDouble() / r.audioMs else 0.0),
            r.rawSegmentCount.toString(), r.segments.size.toString(), cleanCount.toString(), r.error ?: "",
        ).joinToString(",") { csv(it) }
        f.appendText(row + "\n")
    }

    /** Ekranda gösterilecek tek satırlık özet (saniye). */
    fun summary(
        r: EngineResult, mode: String, pass: String, importMs: Long, firstVisibleMs: Long, totalMs: Long,
        fallback: Boolean, cleanCount: Int,
    ): String {
        fun s(ms: Long) = if (ms < 0) "-" else "%.1f".format(Locale.US, ms / 1000.0)
        val dil = when (r.detectPath) {
            "base" -> "dil (küçük model) ${s(r.detectMs)}"
            "model_ici" -> "dil: model içinde (STT'ye dahil)"
            else -> "dil: seçili"
        }
        return "$mode/$pass · ${r.variant} · tekrar deneme ${if (fallback) "açık" else "kapalı"} · ses ${s(r.audioMs)} sn · " +
            "içe aktarma ${s(importMs)} · yükleme ${s(r.loadMs)} · $dil · encoder ${s(r.encodeMs)} (${r.windows} pencere) · " +
            "decoder+diğer ${s(r.decodeMs)} · STT ${s(r.transcribeMs)} · parça (ham/ön filtre/son) ${r.rawSegmentCount}/${r.segments.size}/$cleanCount · " +
            "ilk metin ${s(firstVisibleMs)} · toplam ${s(totalMs)} sn · RTF ${"%.2f".format(Locale.US, r.rtf)} · ${r.threads} thread" +
            (r.error?.let { " · HATA $it" } ?: "")
    }

    fun clear(context: Context) {
        file(context).delete()
        File(File(context.filesDir, "results"), "timings-v2.csv").delete()
        File(File(context.filesDir, "results"), "timings.csv").delete() // v1.9 günlüğü
        File(File(context.filesDir, "results"), "benchmark.csv").delete() // eski sürüm günlüğü
    }

    private fun csv(s: String) = "\"" + s.replace("\"", "\"\"").replace('\n', ' ') + "\""
}
