package com.aitolian.sesyazibench.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Kuantize whisper.cpp modelleri (Hugging Face, MIT lisanslı Whisper ağırlıkları). */
enum class WhisperModel(val fileName: String, val label: String, val approxMb: Int) {
    TINY("ggml-tiny-q5_1.bin", "tiny q5_1", 31),
    BASE("ggml-base-q5_1.bin", "base q5_1", 57),
    SMALL("ggml-small-q5_1.bin", "small q5_1", 181),
    TURBO("ggml-large-v3-turbo-q5_0.bin", "large-v3-turbo q5_0", 547);

    val url: String get() = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$fileName"
}

object ModelStore {
    fun dir(context: Context) = File(context.filesDir, "models").apply { mkdirs() }
    fun file(context: Context, m: WhisperModel) = File(dir(context), m.fileName)
    fun isReady(context: Context, m: WhisperModel) = file(context, m).let { it.exists() && it.length() > 1_000_000 }

    /** İndirir; onProgress 0..1 arası çağrılır. */
    suspend fun download(context: Context, m: WhisperModel, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val target = file(context, m)
        val tmp = File(target.path + ".part")
        var conn = URL(m.url).openConnection() as HttpURLConnection
        // Hugging Face CDN'e yönlendirir; farklı host'a yönlendirmeyi elle takip et
        var redirects = 0
        while (conn.responseCode in 300..399 && redirects < 5) {
            val next = conn.getHeaderField("Location")
            conn.disconnect()
            conn = URL(URL(m.url), next).openConnection() as HttpURLConnection
            redirects++
        }
        check(conn.responseCode == 200) { "HTTP ${conn.responseCode}" }
        val total = conn.contentLengthLong.takeIf { it > 0 } ?: (m.approxMb * 1_048_576L)
        conn.inputStream.use { input ->
            tmp.outputStream().use { out ->
                val buf = ByteArray(1 shl 16)
                var done = 0L
                var lastReport = 0L
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    done += n
                    if (done - lastReport > 512 * 1024) {
                        onProgress((done.toFloat() / total).coerceIn(0f, 1f)); lastReport = done
                    }
                }
            }
        }
        conn.disconnect()
        check(tmp.renameTo(target)) { "Dosya kaydedilemedi" }
        onProgress(1f)
    }
}
