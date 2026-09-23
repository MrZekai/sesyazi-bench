package com.aitolian.sesyazibench.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Kuantize whisper.cpp modelleri (Hugging Face, MIT lisanslı Whisper ağırlıkları).
 *
 * base/small için q8_0: ggml ARM'da q8_0 ağırlıklarını açılışta dotprod/i8mm
 * çekirdeklerine uygun düzene yeniden paketliyor (repack); q5_1 bu hızlı yoldan
 * yararlanamıyor. Dosya biraz daha büyük ama döküm belirgin daha hızlı.
 */
enum class WhisperModel(val fileName: String, val label: String, val approxMb: Int) {
    TINY("ggml-tiny-q5_1.bin", "tiny q5_1", 31),
    BASE("ggml-base-q8_0.bin", "base q8_0", 78),
    SMALL("ggml-small-q8_0.bin", "small q8_0", 252),
    TURBO("ggml-large-v3-turbo-q5_0.bin", "large-v3-turbo q5_0", 547);

    val url: String get() = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$fileName"
}

/** Silero VAD (MIT) — sessizlik ve müziği ayıklar, halüsinasyonu azaltır. ~0,9 MB. */
object VadModel {
    const val FILE = "ggml-silero-v6.2.0.bin"
    const val URL = "https://huggingface.co/ggml-org/whisper-vad/resolve/main/$FILE"
}

object ModelStore {
    fun dir(context: Context) = File(context.filesDir, "models").apply { mkdirs() }
    fun file(context: Context, m: WhisperModel) = File(dir(context), m.fileName)
    fun isReady(context: Context, m: WhisperModel) = file(context, m).let { it.exists() && it.length() > 1_000_000 }

    fun vadFile(context: Context) = File(dir(context), VadModel.FILE)
    fun vadReady(context: Context) = vadFile(context).let { it.exists() && it.length() > 100_000 }

    /** Whisper modeli + (yoksa) VAD modelini indirir; onProgress 0..1. */
    suspend fun download(context: Context, m: WhisperModel, onProgress: (Float) -> Unit) {
        if (!vadReady(context)) runCatching { fetch(VadModel.URL, vadFile(context), 900_000L) {} }
        if (!isReady(context, m)) fetch(m.url, file(context, m), m.approxMb * 1_048_576L, onProgress)
        onProgress(1f)
    }

    /** Eski (q5_1) model dosyalarını siler — yerlerini hızlı q8_0 sürümleri aldı. */
    fun cleanupLegacy(context: Context) {
        listOf("ggml-base-q5_1.bin", "ggml-small-q5_1.bin").forEach { File(dir(context), it).delete() }
        dir(context).listFiles()?.filter { it.name.endsWith(".part") && it.lastModified() < System.currentTimeMillis() - 86_400_000 }
            ?.forEach { it.delete() }
    }

    /** VAD yoksa sessizce indirmeyi dener (whisper modeli zaten varsa). */
    suspend fun ensureVad(context: Context) {
        if (!vadReady(context)) runCatching { fetch(VadModel.URL, vadFile(context), 900_000L) {} }
    }

    private suspend fun fetch(url: String, target: File, approxBytes: Long, onProgress: (Float) -> Unit) =
        withContext(Dispatchers.IO) {
            val tmp = File(target.path + ".part")
            var conn = open(url)
            var redirects = 0
            while (conn.responseCode in 300..399 && redirects < 5) {
                val next = conn.getHeaderField("Location")
                conn.disconnect()
                conn = open(URL(URL(url), next).toString())
                redirects++
            }
            check(conn.responseCode == 200) { "HTTP ${conn.responseCode}" }
            val expected = conn.contentLengthLong
            val total = expected.takeIf { it > 0 } ?: approxBytes
            var done = 0L
            try {
                conn.inputStream.use { input ->
                    tmp.outputStream().use { out ->
                        val buf = ByteArray(1 shl 16)
                        var lastReport = 0L
                        while (true) {
                            val n = input.read(buf)
                            if (n < 0) break
                            out.write(buf, 0, n)
                            done += n
                            if (done - lastReport > 512 * 1024) {
                                onProgress((done.toFloat() / total).coerceIn(0f, 0.99f)); lastReport = done
                            }
                        }
                    }
                }
            } finally {
                conn.disconnect()
            }
            if (expected > 0 && done != expected) {
                tmp.delete(); error("İndirme yarım kaldı")
            }
            target.delete()
            check(tmp.renameTo(target)) { "Dosya kaydedilemedi" }
        }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
        }
}
