package com.aitolian.sesyazibench.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

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
    TURBO("ggml-large-v3-turbo-q5_0.bin", "large-v3-turbo q5_0", 547),
    /** Deney (geliştirici araçları): q8_0 → ARM repack yolu; daha büyük dosya ve bellek. */
    TURBO_Q8("ggml-large-v3-turbo-q8_0.bin", "large-v3-turbo q8_0", 834);

    /** Büyük model: yüklenirken ikinci (dil algılama) bağlamı bellekte tutulmaz. */
    val isLarge: Boolean get() = this == TURBO || this == TURBO_Q8

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
    fun vadFile(context: Context) = File(dir(context), VadModel.FILE)

    private fun marker(target: File) = File(target.path + ".sha256")
    private fun minBytes(m: WhisperModel) = m.approxMb * 1_048_576L * 9 / 10
    private const val VAD_MIN = 800_000L
    private const val VAD_APPROX = 900_000L

    /**
     * Hazır = dosya var ve (SHA-256 doğrulanıp işaretlendi ya da — eski sürümden
     * kalan işaretsiz dosyalar için — beklenen boyutun %90'ından büyük).
     */
    fun isReady(context: Context, m: WhisperModel) = verified(file(context, m), minBytes(m))
    fun vadReady(context: Context) = verified(vadFile(context), VAD_MIN)

    private fun verified(f: File, minBytes: Long): Boolean {
        if (!f.exists()) return false
        val mk = marker(f)
        return if (mk.exists()) mk.readText().substringAfter(' ').trim().toLongOrNull() == f.length()
        else f.length() >= minBytes
    }

    /** Aynı dosyaya aynı anda tek indirme; ikinci çağıran ilkini bekler (ilerlemeyi de izler). */
    private val locks = ConcurrentHashMap<String, Mutex>()
    private val inFlight = ConcurrentHashMap<String, MutableStateFlow<Float>>()

    /** Whisper modeli + (yoksa) VAD modelini indirir; onProgress 0..1. İptal edilebilir. */
    suspend fun download(context: Context, m: WhisperModel, onProgress: (Float) -> Unit) {
        runCatching { ensure(VadModel.URL, vadFile(context), VAD_MIN, VAD_APPROX) {} }
            .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }
        ensure(m.url, file(context, m), minBytes(m), m.approxMb * 1_048_576L, onProgress)
        onProgress(1f)
    }

    /** VAD yoksa sessizce indirmeyi dener (whisper modeli zaten varsa). */
    suspend fun ensureVad(context: Context) {
        runCatching { ensure(VadModel.URL, vadFile(context), VAD_MIN, VAD_APPROX) {} }
            .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }
    }

    /** Eski (q5_1) model dosyalarını ve bir günden eski yarım indirmeleri siler. */
    fun cleanupLegacy(context: Context) {
        listOf("ggml-base-q5_1.bin", "ggml-small-q5_1.bin").forEach { File(dir(context), it).delete() }
        dir(context).listFiles()
            ?.filter { it.name.endsWith(".part") && it.lastModified() < System.currentTimeMillis() - 86_400_000 }
            ?.forEach { it.delete() }
    }

    fun delete(context: Context, m: WhisperModel) {
        val f = file(context, m)
        marker(f).delete()
        f.delete()
    }

    private suspend fun ensure(url: String, target: File, minBytes: Long, approxBytes: Long, onProgress: (Float) -> Unit) =
        coroutineScope {
            val key = target.name
            val watcher = inFlight[key]?.let { flow -> launch { flow.collect { onProgress(it) } } }
            try {
                locks.getOrPut(key) { Mutex() }.withLock {
                    watcher?.cancel()
                    if (verified(target, minBytes)) return@withLock
                    val flow = MutableStateFlow(0f)
                    inFlight[key] = flow
                    try {
                        fetch(url, target, approxBytes) { p -> flow.value = p; onProgress(p) }
                    } finally {
                        inFlight.remove(key)
                    }
                }
            } finally {
                watcher?.cancel()
            }
        }

    /**
     * İndirir ve doğrular: Content-Length ile boyut, Hugging Face'in LFS
     * yanıtındaki X-Linked-Etag (dosyanın SHA-256'sı) ile içerik. Yarım/bozuk
     * dosya asla "hazır" sayılmaz; mevcut sağlam dosyanın üzerine ancak
     * doğrulama geçince yazılır. Coroutine iptalinde bağlantı kapatılır.
     */
    private suspend fun fetch(url: String, target: File, approxBytes: Long, onProgress: (Float) -> Unit) =
        withContext(Dispatchers.IO) {
            val tmp = File(target.path + ".part")
            var conn = open(url)
            val job = currentCoroutineContext()[Job]
            val closer = job?.invokeOnCompletion { runCatching { conn.disconnect() } }
            try {
                var expectedSha: String? = null
                var redirects = 0
                while (true) {
                    val code = conn.responseCode
                    conn.getHeaderField("X-Linked-Etag")?.trim('"', ' ')?.lowercase()
                        ?.takeIf { it.length == 64 && it.all { c -> c in '0'..'9' || c in 'a'..'f' } }
                        ?.let { expectedSha = it }
                    if (code !in 300..399) break
                    if (++redirects > 5) throw IOException("Çok fazla yönlendirme")
                    val loc = conn.getHeaderField("Location") ?: throw IOException("Yönlendirme adresi yok")
                    val next = URL(conn.url, loc).toString()
                    conn.disconnect()
                    conn = open(next)
                }
                if (conn.responseCode != 200) throw IOException("HTTP ${conn.responseCode}")
                val expected = conn.contentLengthLong
                val total = expected.takeIf { it > 0 } ?: approxBytes
                val free = target.parentFile?.usableSpace ?: Long.MAX_VALUE
                if (free < total + 20L * 1_048_576) throw IOException("Telefonda yeterli boş alan yok (${total / 1_048_576} MB gerekli)")

                val sha = MessageDigest.getInstance("SHA-256")
                var done = 0L
                conn.inputStream.use { input ->
                    tmp.outputStream().use { out ->
                        val buf = ByteArray(1 shl 16)
                        var lastReport = 0L
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val n = input.read(buf)
                            if (n < 0) break
                            out.write(buf, 0, n)
                            sha.update(buf, 0, n)
                            done += n
                            if (done - lastReport > 512 * 1024) {
                                onProgress((done.toFloat() / total).coerceIn(0f, 0.99f)); lastReport = done
                            }
                        }
                        out.fd.sync()
                    }
                }
                if (expected > 0 && done != expected) throw IOException("İndirme yarım kaldı")
                val actual = sha.digest().joinToString("") { "%02x".format(it) }
                expectedSha?.let { if (it != actual) throw IOException("İndirilen dosya bozuk (doğrulama başarısız)") }
                val mk = marker(target)
                mk.delete()
                target.delete()
                if (!tmp.renameTo(target)) throw IOException("Dosya kaydedilemedi")
                mk.writeText("$actual ${target.length()}")
            } catch (t: Throwable) {
                tmp.delete()
                throw t
            } finally {
                closer?.dispose()
                runCatching { conn.disconnect() }
            }
        }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            // Yönlendirmeleri elle izliyoruz: ilk yanıttaki SHA-256 başlığını yakalamak için
            instanceFollowRedirects = false
        }
}
