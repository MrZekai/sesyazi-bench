package com.aitolian.sesyazibench.engine

import android.os.SystemClock
import com.aitolian.sesyazibench.BuildConfig
import com.aitolian.sesyazibench.audio.AudioDecoder
import com.aitolian.sesyazibench.audio.DecodedAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Hızlı mod: Meta Wit.ai /dictation (ücretsiz, ticari kullanım dahil).
 *
 * Kendi akışımız:
 *  - Ses, sessiz noktalardan en fazla [CHUNK_MAX_MS] uzunluğunda parçalara bölünür
 *    (Wit uzun seslerde hata verebiliyor; ayrıca parçalar paralel gönderilir → uzun
 *    seste de hızlı).
 *  - Parçalar en fazla [PARALLEL] eşzamanlı istekle gönderilir; sonuçlar sırayla birleşir.
 *  - İlk parçanın ara sonuçları (is_final=false) canlı gösterilir: metin kelime
 *    kelime belirir.
 *  - Her dil ayrı bir Wit uygulamasıdır; anahtarlar derlemede WIT_TOKENS (GitHub
 *    secret) ile gelir. Anahtarı olmayan dil → çağıran Whisper'a düşer.
 *
 * Hata (ağ yok, 401, 429 kota, 5xx) → error dolu sonuç; çağıran Whisper'a geçer.
 */
class WitEngine(private val token: String) {

    companion object {
        private const val API = "https://api.wit.ai/dictation?v=20240304"
        private const val CONTENT_TYPE = "audio/raw;encoding=signed-integer;bits=16;rate=16000;endian=little"
        const val CHUNK_MAX_MS = 50_000L
        private const val CHUNK_MIN_MS = 25_000L
        private const val PARALLEL = 3
        const val ENGINE = "wit.ai"

        /** Derlemede gelen dil → anahtar eşlemesi. */
        val tokens: Map<String, String> by lazy {
            runCatching {
                val o = JSONObject(BuildConfig.WIT_TOKENS.ifBlank { "{}" })
                o.keys().asSequence().associateWith { o.getString(it).trim() }.filterValues { it.isNotEmpty() }
            }.getOrDefault(emptyMap())
        }

        fun supports(langCode: String): Boolean = tokens.containsKey(langCode)
        fun forLang(langCode: String): WitEngine? = tokens[langCode]?.let { WitEngine(it) }

        /**
         * Sesi sessiz noktalardan böler. Her parça CHUNK_MIN..CHUNK_MAX arasında;
         * kesim yeri o aralıktaki en sessiz 200 ms'lik pencerenin ortası.
         * Döner: (başlangıç örneği, bitiş örneği) listesi.
         */
        fun splitPoints(samples: FloatArray): List<Pair<Int, Int>> {
            val rate = AudioDecoder.TARGET_RATE
            val maxLen = (CHUNK_MAX_MS * rate / 1000).toInt()
            val minLen = (CHUNK_MIN_MS * rate / 1000).toInt()
            val win = rate / 5 // 200 ms
            val out = mutableListOf<Pair<Int, Int>>()
            var start = 0
            while (samples.size - start > maxLen) {
                var bestPos = start + maxLen
                var bestEnergy = Double.MAX_VALUE
                var p = start + minLen
                while (p + win <= start + maxLen) {
                    var e = 0.0
                    for (k in p until p + win) e += samples[k] * samples[k]
                    if (e < bestEnergy) { bestEnergy = e; bestPos = p + win / 2 }
                    p += win / 2
                }
                out += start to bestPos
                start = bestPos
            }
            out += start to samples.size
            return out
        }
    }

    /**
     * @param onPartial ilk (henüz bitmemiş) parçanın canlı ara metni; null → ara metin bitti
     * @param onSegment sırası gelen kesinleşmiş cümle
     */
    suspend fun transcribe(
        audio: DecodedAudio,
        langCode: String,
        cancel: AtomicBoolean,
        onPartial: (String?) -> Unit = {},
        onSegment: (Segment) -> Unit = {},
    ): EngineResult = withContext(Dispatchers.IO) {
        val base = EngineResult(ENGINE, "wit $langCode", langCode, langCode, audio.durationMs, 0, 0, "", detectPath = "secili")
        val t0 = SystemClock.elapsedRealtime()
        val parts = splitPoints(audio.samples)
        val gate = Semaphore(PARALLEL)
        var firstSegmentMs = -1L
        // Sırayla yayınlama: parça i bitince, önceki tüm parçalar da bittiyse segmentleri akıt
        val done = arrayOfNulls<List<Segment>>(parts.size)
        var emitted = 0
        val lock = Any()

        fun flush() = synchronized(lock) {
            while (emitted < parts.size && done[emitted] != null) {
                done[emitted]!!.forEach {
                    if (firstSegmentMs < 0) firstSegmentMs = SystemClock.elapsedRealtime() - t0
                    onSegment(it)
                }
                emitted++
                if (emitted < parts.size) onPartial(null)
            }
        }

        try {
            coroutineScope {
                val jobs = parts.mapIndexed { i, (from, to) ->
                    async {
                        gate.withPermit {
                            if (cancel.get()) throw IOException(WhisperEngine.CANCELLED)
                            val offsetMs = from * 1000L / AudioDecoder.TARGET_RATE
                            val segs = postChunk(
                                pcm16(audio.samples, from, to), offsetMs,
                                (to - from) * 1000L / AudioDecoder.TARGET_RATE, cancel,
                                onPartial = { text -> synchronized(lock) { if (emitted == i) onPartial(text) } },
                            )
                            synchronized(lock) { done[i] = segs }
                            flush()
                            segs
                        }
                    }
                }
                val all = jobs.flatMap { it.await() }
                onPartial(null)
                val ms = SystemClock.elapsedRealtime() - t0
                base.copy(
                    transcribeMs = ms, text = all.joinToString(" ") { it.text }, segments = all,
                    rawSegmentCount = all.size, firstSegmentMs = firstSegmentMs, windows = parts.size,
                )
            }
        } catch (t: Throwable) {
            if (t is kotlinx.coroutines.CancellationException) throw t
            onPartial(null)
            base.copy(
                transcribeMs = SystemClock.elapsedRealtime() - t0,
                error = if (cancel.get()) WhisperEngine.CANCELLED else "Wit: ${t.message ?: t.javaClass.simpleName}",
            )
        }
    }

    private fun pcm16(samples: FloatArray, from: Int, to: Int): ByteArray {
        val buf = ByteBuffer.allocate((to - from) * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (k in from until to) buf.putShort((samples[k].coerceIn(-1f, 1f) * 32767f).toInt().toShort())
        return buf.array()
    }

    /**
     * Tek parçayı gönderir, akış halindeki yanıtı okur. Yanıt art arda JSON
     * nesnelerinden oluşur; her biri {text, is_final, speech:{tokens:[{start,end}]}}.
     */
    private suspend fun postChunk(
        body: ByteArray, offsetMs: Long, durMs: Long, cancel: AtomicBoolean, onPartial: (String) -> Unit,
    ): List<Segment> {
        val conn = (URL(API).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 10_000
            readTimeout = 30_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", CONTENT_TYPE)
            setRequestProperty("Accept", "application/json")
            setFixedLengthStreamingMode(body.size)
        }
        val job = currentCoroutineContext()[Job]
        val closer = job?.invokeOnCompletion { runCatching { conn.disconnect() } }
        try {
            conn.outputStream.use { out ->
                var off = 0
                while (off < body.size) {
                    if (cancel.get()) throw IOException(WhisperEngine.CANCELLED)
                    val n = minOf(32 * 1024, body.size - off)
                    out.write(body, off, n)
                    off += n
                }
            }
            val code = conn.responseCode
            if (code != 200) {
                val msg = runCatching { conn.errorStream?.bufferedReader()?.readText() }.getOrNull()?.take(200)
                throw IOException(
                    when (code) {
                        401, 403 -> "anahtar geçersiz ($code)"
                        429 -> "kullanım sınırı aşıldı (429)"
                        else -> "HTTP $code ${msg ?: ""}".trim()
                    },
                )
            }
            val finals = mutableListOf<Segment>()
            val parser = JsonStreamSplitter()
            // Karakter akışı olarak oku: çok baytlı Türkçe/Arapça harfler paket sınırında bölünmesin
            conn.inputStream.reader(Charsets.UTF_8).use { input ->
                val buf = CharArray(4 * 1024)
                while (true) {
                    currentCoroutineContext().ensureActive()
                    if (cancel.get()) throw IOException(WhisperEngine.CANCELLED)
                    val n = input.read(buf)
                    if (n < 0) break
                    for (json in parser.feed(String(buf, 0, n))) {
                        handle(json, offsetMs, durMs, finals, onPartial)
                    }
                }
            }
            return finals
        } finally {
            closer?.dispose()
            runCatching { conn.disconnect() }
        }
    }

    private fun handle(json: String, offsetMs: Long, durMs: Long, finals: MutableList<Segment>, onPartial: (String) -> Unit) {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return
        o.optString("error").takeIf { it.isNotEmpty() }?.let { throw IOException(it) }
        val text = o.optString("text").trim()
        if (text.isEmpty()) return
        val isFinal = o.optBoolean("is_final", false) ||
            o.optString("type").equals("FINAL_TRANSCRIPTION", ignoreCase = true)
        if (!isFinal) { onPartial(text); return }
        if (finals.lastOrNull()?.text == text) return // aynı nihai metin tekrar gelirse
        // Zaman: Wit belirteç zamanları (ms) varsa onlar; yoksa parça içinde sırayla
        var start = -1L
        var end = -1L
        o.optJSONObject("speech")?.optJSONArray("tokens")?.let { toks ->
            if (toks.length() > 0) {
                start = toks.getJSONObject(0).optLong("start", -1)
                end = toks.getJSONObject(toks.length() - 1).optLong("end", -1)
            }
        }
        if (start < 0 || end < start) {
            start = finals.lastOrNull()?.let { it.endMs - offsetMs } ?: 0L
            end = durMs
        }
        finals += Segment(offsetMs + start, offsetMs + end.coerceAtMost(durMs), text)
    }
}

/**
 * Art arda gelen JSON nesnelerini (araya \r\n girebilir, bir nesne birden çok
 * pakete bölünebilir) süslü parantez derinliğiyle ayırır; dizgi içindeki
 * parantez ve kaçış karakterlerini dikkate alır.
 */
internal class JsonStreamSplitter {
    private val buf = StringBuilder()
    private var depth = 0
    private var inString = false
    private var escape = false
    private var start = -1

    fun feed(chunk: String): List<String> {
        val out = mutableListOf<String>()
        for (c in chunk) {
            if (depth == 0 && c != '{') continue // nesneler arası boşluk/satır sonu
            buf.append(c)
            if (inString) {
                when {
                    escape -> escape = false
                    c == '\\' -> escape = true
                    c == '"' -> inString = false
                }
                continue
            }
            when (c) {
                '"' -> inString = true
                '{' -> { if (depth == 0) start = buf.length - 1; depth++ }
                '}' -> {
                    depth--
                    if (depth == 0) {
                        out += buf.substring(start)
                        buf.setLength(0)
                    }
                }
            }
        }
        return out
    }
}
