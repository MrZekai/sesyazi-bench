package com.aitolian.sesyazibench.engine

import android.os.SystemClock
import com.aitolian.sesyazibench.BuildConfig
import com.aitolian.sesyazibench.audio.AudioDecoder
import com.aitolian.sesyazibench.audio.DecodedAudio
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Hızlı mod: Meta Wit.ai /dictation (ücretsiz, ticari kullanım dahil).
 *
 * Akış:
 *  - Ses, sessiz noktalardan en fazla [CHUNK_MAX_MS] uzunluğunda parçalara bölünür;
 *    parçalar en fazla [PARALLEL] eşzamanlı istekle gönderilir, sırayla birleşir.
 *  - Sıradaki (en baştaki bitmemiş) parçanın o ana kadar kesinleşen cümleleri +
 *    ara metni canlı gösterilir; parça bitince cümleler kalıcı segment olur.
 *    (Parça yeniden denenirse ekranda çift metin oluşmaz.)
 *  - Her parçanın sonucu ayrı tutulur: biri başarısız olursa diğerleri kaybolmaz;
 *    geçici hatalar sınırlı sayıda yeniden denenir, 429'da beklenir.
 *  - Yanıt katı doğrulanır: yarım JSON / final'siz bitiş = hata (eksik metin
 *    başarı sayılmaz).
 *  - İptal ve süre aşımında bağlantı ayrı bir bekçiyle hemen kapatılır.
 *  - Hata metinleri sabit kodlardır (sunucu yanıt gövdesi saklanmaz/loglanmaz).
 */
class WitEngine(private val token: String) {

    /** Tek parçanın sonucu. */
    sealed interface ChunkOutcome {
        data class Success(val segments: List<Segment>) : ChunkOutcome
        data class Failure(val code: String) : ChunkOutcome
    }

    /** Başarısız kalan parçanın örnek aralığı (yerel tamamlama için) ve hata kodu. */
    data class FailedChunk(val from: Int, val to: Int, val code: String)

    data class Outcome(val result: EngineResult, val failed: List<FailedChunk>) {
        val authFailed: Boolean get() = failed.any { it.code == ERR_AUTH }
    }

    companion object {
        private const val API = "https://api.wit.ai/dictation?v=20240304"
        private const val CONTENT_TYPE = "audio/raw;encoding=signed-integer;bits=16;rate=16000;endian=little"
        const val CHUNK_MAX_MS = 50_000L
        private const val CHUNK_MIN_MS = 25_000L
        private const val PARALLEL = 3
        private const val MAX_TRIES = 3
        /** Aynı cihazdan art arda istek başlatma aralığı (dakikalık kota için yumuşatma). */
        private const val MIN_START_GAP_MS = 300L
        private const val MAX_FRAME_CHARS = 1_048_576
        const val ENGINE = "wit.ai"

        const val ERR_AUTH = "WIT_AUTH"
        const val ERR_RATE = "WIT_RATE_LIMIT"
        const val ERR_SERVER = "WIT_SERVER"
        const val ERR_NETWORK = "WIT_NETWORK"
        const val ERR_TIMEOUT = "WIT_TIMEOUT"
        const val ERR_TRUNCATED = "WIT_TRUNCATED"
        const val ERR_INVALID = "WIT_INVALID_JSON"
        const val ERR_SERVICE = "WIT_ERROR"

        /** Derlemede gelen dil → anahtar eşlemesi; geçersiz girdiler tek tek atlanır. */
        val tokens: Map<String, String> by lazy {
            val o = runCatching { JSONObject(BuildConfig.WIT_TOKENS.ifBlank { "{}" }) }.getOrNull() ?: return@lazy emptyMap()
            o.keys().asSequence().mapNotNull { k ->
                val v = o.optString(k).trim()
                if (v.isEmpty() || v.any { it.isWhitespace() }) null else k to v
            }.toMap()
        }

        fun supports(langCode: String): Boolean = tokens.containsKey(langCode)
        fun forLang(langCode: String): WitEngine? = tokens[langCode]?.let { WitEngine(it) }

        // Uygulama genelinde istek zamanlayıcısı: başlangıç aralığı + 429 sonrası bekleme
        private val startLock = Mutex()
        @Volatile private var lastStartAt = 0L
        @Volatile private var cooldownUntil = 0L

        private suspend fun awaitStartSlot() {
            startLock.withLock {
                val now = SystemClock.elapsedRealtime()
                val wait = maxOf(cooldownUntil - now, lastStartAt + MIN_START_GAP_MS - now)
                if (wait > 0) delay(wait)
                lastStartAt = SystemClock.elapsedRealtime()
            }
        }

        /**
         * Sesi sessiz noktalardan böler. Her parça CHUNK_MIN..CHUNK_MAX arasında;
         * kesim, o aralıktaki en sessiz 200 ms'lik pencerenin ortası (eşitlikte en
         * geç olan → gereksiz kısa parça yok). Boş ses → boş liste.
         */
        fun splitPoints(samples: FloatArray): List<Pair<Int, Int>> {
            if (samples.isEmpty()) return emptyList()
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
                    if (e <= bestEnergy) { bestEnergy = e; bestPos = p + win / 2 }
                    p += win / 2
                }
                out += start to bestPos
                start = bestPos
            }
            out += start to samples.size
            return out
        }
    }

    /** Bir parçanın canlı durumu (yalnız [lock] altında). */
    private class PartState {
        var finals: List<String> = emptyList()
        var partial: String? = null
        var outcome: ChunkOutcome? = null
    }

    /**
     * @param onPartial sıradaki parçanın canlı metni (o ana kadarki kesin cümleler +
     *   ara metin); null → gösterilecek canlı metin yok
     * @param onSegment sırası gelen parçanın kesinleşmiş cümlesi (sırayla)
     */
    suspend fun transcribe(
        audio: DecodedAudio,
        langCode: String,
        cancel: AtomicBoolean,
        onPartial: (String?) -> Unit = {},
        onSegment: (Segment) -> Unit = {},
    ): Outcome = withContext(Dispatchers.IO) {
        val base = EngineResult(ENGINE, "wit $langCode", langCode, langCode, audio.durationMs, 0, 0, "", detectPath = "secili")
        val t0 = SystemClock.elapsedRealtime()
        val parts = splitPoints(audio.samples)
        if (parts.isEmpty()) return@withContext Outcome(base, emptyList())
        val gate = Semaphore(PARALLEL)
        val states = List(parts.size) { PartState() }
        val lock = Any()
        var head = 0
        var firstSegmentMs = -1L
        val authDead = AtomicBoolean(false)

        /** Sıradaki parçadan itibaren bitmiş olanları yayınla, sıradakinin canlı metnini göster. */
        fun flush() = synchronized(lock) {
            while (head < parts.size) {
                val st = states[head]
                val oc = st.outcome
                if (oc == null) {
                    val live = (st.finals + listOfNotNull(st.partial)).joinToString(" ").trim()
                    if (live.isNotEmpty() && firstSegmentMs < 0) firstSegmentMs = SystemClock.elapsedRealtime() - t0
                    onPartial(live.ifEmpty { null })
                    return@synchronized
                }
                if (oc is ChunkOutcome.Success) oc.segments.forEach {
                    if (firstSegmentMs < 0) firstSegmentMs = SystemClock.elapsedRealtime() - t0
                    onSegment(it)
                }
                head++
            }
            onPartial(null)
        }

        val outcomes = coroutineScope {
            parts.mapIndexed { i, (from, to) ->
                async {
                    val oc = gate.withPermit {
                        runChunk(audio.samples, from, to, cancel, authDead) { finals, partial ->
                            synchronized(lock) {
                                states[i].finals = finals
                                states[i].partial = partial
                            }
                            flush()
                        }
                    }
                    synchronized(lock) {
                        states[i].outcome = oc
                        states[i].partial = null
                    }
                    flush()
                    oc
                }
            }.awaitAll()
        }

        val all = mutableListOf<Segment>()
        val failed = mutableListOf<FailedChunk>()
        outcomes.forEachIndexed { i, oc ->
            when (oc) {
                is ChunkOutcome.Success -> all += oc.segments
                is ChunkOutcome.Failure -> failed += FailedChunk(parts[i].first, parts[i].second, oc.code)
            }
        }
        val ms = SystemClock.elapsedRealtime() - t0
        val cancelled = cancel.get()
        val result = base.copy(
            transcribeMs = ms, text = all.joinToString(" ") { it.text }, segments = all,
            rawSegmentCount = all.size, firstSegmentMs = firstSegmentMs, windows = parts.size,
            error = when {
                cancelled -> WhisperEngine.CANCELLED
                failed.size == parts.size -> failed.first().code
                else -> null
            },
        )
        Outcome(result, if (cancelled) emptyList() else failed)
    }

    /**
     * Bir parçayı, gerekirse yeniden deneyerek işler. 401/403 tekrar denenmez
     * (ve diğer parçalar da denemeyi bırakır); 429 önerilen süre kadar bekler;
     * ağ/5xx/yarım yanıt en fazla [MAX_TRIES] kez, artan beklemeyle denenir.
     */
    private suspend fun runChunk(
        samples: FloatArray, from: Int, to: Int, cancel: AtomicBoolean, authDead: AtomicBoolean,
        onLive: (List<String>, String?) -> Unit,
    ): ChunkOutcome {
        val offsetMs = from * 1000L / AudioDecoder.TARGET_RATE
        val durMs = (to - from) * 1000L / AudioDecoder.TARGET_RATE
        val body = pcm16(samples, from, to)
        var lastCode = ERR_NETWORK
        repeat(MAX_TRIES) { attempt ->
            if (cancel.get()) return ChunkOutcome.Failure(WhisperEngine.CANCELLED)
            if (authDead.get()) return ChunkOutcome.Failure(ERR_AUTH)
            awaitStartSlot()
            try {
                return ChunkOutcome.Success(postChunk(body, offsetMs, durMs, cancel, onLive))
            } catch (e: CancellationException) {
                throw e
            } catch (e: WitHttpException) {
                lastCode = e.code
                onLive(emptyList(), null)
                when (e.code) {
                    ERR_AUTH -> { authDead.set(true); return ChunkOutcome.Failure(ERR_AUTH) }
                    ERR_RATE -> {
                        val wait = (e.retryAfterMs ?: (2_000L * (attempt + 1))).coerceIn(1_000L, 15_000L)
                        cooldownUntil = maxOf(cooldownUntil, SystemClock.elapsedRealtime() + wait)
                    }
                    else -> delay(700L * (attempt + 1))
                }
            } catch (e: IOException) {
                lastCode = if (cancel.get()) WhisperEngine.CANCELLED else classify(e)
                onLive(emptyList(), null)
                if (cancel.get()) return ChunkOutcome.Failure(WhisperEngine.CANCELLED)
                delay(700L * (attempt + 1))
            } catch (e: Exception) {
                // Beklenmeyen yanıt biçimi (ör. JSONException): parça başarısız, diğerleri sürer
                lastCode = ERR_INVALID
                onLive(emptyList(), null)
            }
        }
        return ChunkOutcome.Failure(lastCode)
    }

    private fun classify(e: IOException): String = when {
        e is SocketTimeoutException -> ERR_TIMEOUT
        e is UnknownHostException -> ERR_NETWORK
        e.message == ERR_TRUNCATED || e.message == ERR_INVALID || e.message == ERR_SERVICE ||
            e.message == ERR_TIMEOUT -> e.message!!
        else -> ERR_NETWORK
    }

    private class WitHttpException(val code: String, val retryAfterMs: Long? = null) : IOException(code)

    private fun pcm16(samples: FloatArray, from: Int, to: Int): ByteArray {
        val buf = ByteBuffer.allocate((to - from) * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (k in from until to) buf.putShort((samples[k].coerceIn(-1f, 1f) * 32767f).toInt().toShort())
        return buf.array()
    }

    /**
     * Tek parçayı gönderir, akış halindeki yanıtı okur ve doğrular.
     * Ayrı bir bekçi coroutine'i iptalde ya da toplam süre aşımında bağlantıyı
     * kapatır (engelleyen read/write'ı hemen keser).
     */
    private suspend fun postChunk(
        body: ByteArray, offsetMs: Long, durMs: Long, cancel: AtomicBoolean,
        onLive: (List<String>, String?) -> Unit,
    ): List<Segment> = coroutineScope {
        val conn = (URL(API).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 10_000
            readTimeout = 20_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", CONTENT_TYPE)
            setRequestProperty("Accept", "application/json")
            setFixedLengthStreamingMode(body.size)
        }
        val timedOut = AtomicBoolean(false)
        val deadline = SystemClock.elapsedRealtime() + 30_000L + durMs
        val watchdog = launch(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
            try {
                while (isActive) {
                    if (cancel.get()) break
                    if (SystemClock.elapsedRealtime() > deadline) { timedOut.set(true); break }
                    delay(100)
                }
            } finally {
                // Normal bitişte de çağrılır; kapanmış bağlantıda zararsız
                if (cancel.get() || timedOut.get() || !isActive) runCatching { conn.disconnect() }
            }
        }
        try {
            conn.outputStream.use { out ->
                var off = 0
                while (off < body.size) {
                    val n = minOf(32 * 1024, body.size - off)
                    out.write(body, off, n)
                    off += n
                }
            }
            val code = conn.responseCode
            if (code != 200) {
                runCatching { conn.errorStream?.close() } // gövde okunmaz/saklanmaz
                throw when {
                    code == 401 || code == 403 -> WitHttpException(ERR_AUTH)
                    code == 429 -> WitHttpException(
                        ERR_RATE,
                        conn.getHeaderField("Retry-After")?.trim()?.toLongOrNull()?.times(1000),
                    )
                    code in 500..599 -> WitHttpException(ERR_SERVER)
                    else -> WitHttpException("WIT_HTTP_$code")
                }
            }
            val finals = mutableListOf<Final>()
            val parser = JsonStreamSplitter(MAX_FRAME_CHARS)
            var seenAny = false
            var pendingPartial = false
            conn.inputStream.reader(Charsets.UTF_8).use { input ->
                val buf = CharArray(4 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    for (json in parser.feed(String(buf, 0, n))) {
                        val ev = parse(json)
                        seenAny = true
                        if (ev.isFinal) {
                            pendingPartial = false
                            val last = finals.lastOrNull()
                            // Aynı final'in protokol tekrarı (metin + zaman aynı) atlanır;
                            // farklı zamanda söylenen aynı cümle korunur
                            val dup = last != null && last.text == ev.text && last.start == ev.start && last.end == ev.end
                            if (ev.text.isNotEmpty() && !dup) finals += ev
                            onLive(finals.map { it.text }, null)
                        } else if (ev.text.isNotEmpty() && ev.text != finals.lastOrNull()?.text) {
                            pendingPartial = true
                            onLive(finals.map { it.text }, ev.text)
                        }
                    }
                }
            }
            // Bekçi bağlantıyı kapattıysa okuma sessizce -1 dönebilir: yarım sonucu başarı sayma
            if (timedOut.get()) throw IOException(ERR_TIMEOUT)
            if (cancel.get()) throw IOException(WhisperEngine.CANCELLED)
            parser.finish()
            // Ara metin geldi ama kesin metin gelmeden akış bitti → eksik yanıt
            if (pendingPartial) throw IOException(ERR_TRUNCATED)
            if (!seenAny) return@coroutineScope emptyList() // konuşma yok
            timed(finals, offsetMs, durMs)
        } catch (e: IOException) {
            if (timedOut.get()) throw IOException(ERR_TIMEOUT)
            throw e
        } finally {
            watchdog.cancel()
            runCatching { conn.disconnect() }
        }
    }

    private class Final(val text: String, val isFinal: Boolean, val start: Long, val end: Long)

    private fun parse(json: String): Final {
        val o = try { JSONObject(json) } catch (_: JSONException) { throw IOException(ERR_INVALID) }
        if (o.has("error")) throw IOException(ERR_SERVICE)
        val text = o.optString("text").trim()
        val isFinal = o.optBoolean("is_final", false) ||
            o.optString("type").equals("FINAL_TRANSCRIPTION", ignoreCase = true)
        var start = -1L
        var end = -1L
        o.optJSONObject("speech")?.optJSONArray("tokens")?.let { toks ->
            if (toks.length() > 0) {
                start = toks.optJSONObject(0)?.optLong("start", -1) ?: -1
                end = toks.optJSONObject(toks.length() - 1)?.optLong("end", -1) ?: -1
            }
        }
        return Final(text, isFinal, start, end)
    }

    /**
     * Zaman damgaları. Bütün cümlelerin belirteç zamanları geçerliyse (0 ≤ başlangıç
     * < bitiş ≤ parça süresi, sıralı) onlar kullanılır. Değilse parça süresi,
     * cümlelere metin uzunluğuyla orantılı ve boşluksuz dağıtılır (sıfır süreli ya
     * da ters aralık oluşmaz). Aynı metin farklı zamanda tekrar söylenebilir:
     * yalnızca metni VE zamanı aynı olan protokol tekrarı atılır.
     */
    private fun timed(finals: List<Final>, offsetMs: Long, durMs: Long): List<Segment> {
        if (finals.isEmpty()) return emptyList()
        val tol = 500L
        var prevEnd = 0L
        val valid = finals.all { f ->
            val ok = f.start >= 0 && f.start < f.end && f.start < durMs && f.end <= durMs + tol && f.start + tol >= prevEnd
            if (ok) prevEnd = f.end
            ok
        }
        val out = mutableListOf<Segment>()
        if (valid) {
            for (f in finals) {
                val s = Segment(offsetMs + f.start, offsetMs + f.end.coerceAtMost(durMs), f.text)
                val last = out.lastOrNull()
                if (last != null && last.text == s.text && last.startMs == s.startMs && last.endMs == s.endMs) continue
                out += s
            }
        } else {
            val total = finals.sumOf { it.text.length.coerceAtLeast(1) }.toDouble()
            var acc = 0.0
            for (f in finals) {
                val s = offsetMs + (durMs * acc / total).toLong()
                acc += f.text.length.coerceAtLeast(1)
                val e = offsetMs + (durMs * acc / total).toLong()
                out += Segment(s, maxOf(e, s + 1), f.text)
            }
        }
        return out
    }
}

/**
 * Art arda gelen JSON nesnelerini (araya \r\n girebilir, bir nesne birden çok
 * pakete bölünebilir) süslü parantez derinliğiyle ayırır; dizgi içindeki
 * parantez ve kaçış karakterlerini dikkate alır. Tek nesne [maxChars] sınırını
 * aşarsa ya da akış yarım nesneyle biterse hata verir.
 */
internal class JsonStreamSplitter(private val maxChars: Int = 1_048_576) {
    private val buf = StringBuilder()
    private var depth = 0
    private var inString = false
    private var escape = false

    fun feed(chunk: String): List<String> {
        val out = mutableListOf<String>()
        for (c in chunk) {
            if (depth == 0 && c != '{') {
                if (!c.isWhitespace() && c != ',' && c != '[' && c != ']' && c != '\uFEFF') throw IOException("WIT_INVALID_JSON")
                continue // nesneler arası boşluk/satır sonu
            }
            if (buf.length >= maxChars) throw IOException("WIT_INVALID_JSON")
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
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        out += buf.toString()
                        buf.setLength(0)
                    }
                }
            }
        }
        return out
    }

    /** Akış sonu: yarım kalmış nesne varsa hata. */
    fun finish() {
        if (depth != 0 || inString || buf.isNotEmpty()) throw IOException("WIT_TRUNCATED")
    }
}
