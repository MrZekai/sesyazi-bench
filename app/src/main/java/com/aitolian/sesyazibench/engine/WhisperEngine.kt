package com.aitolian.sesyazibench.engine

import android.content.Context
import android.os.SystemClock
import com.aitolian.sesyazibench.audio.DecodedAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * whisper.cpp motoru. Model bağlamı önbellekte tutulur (her dökümde yeniden
 * yüklenmez); aynı anda tek native çağrı çalışır (Mutex). İptal edilen bir
 * oturumun native çağrısı abort geri çağrısıyla en kısa sürede durur; yeni
 * oturum kilidi bekler, bu yüzden iki native çağrı asla çakışmaz.
 *
 * @param allowFallback false => sıcaklık yedeklemesi kapalı (tek greedy tur).
 */
class WhisperEngine(
    private val context: Context,
    private val model: WhisperModel,
    private val beamSize: Int = 1,
    private val allowFallback: Boolean = false,
) : TranscriptionEngine {
    override val name = "whisper.cpp"

    override suspend fun transcribe(audio: DecodedAudio, lang: Lang): EngineResult =
        transcribe(audio, lang, onProgress = {}, cancel = AtomicBoolean(false))

    /** @param onSegment cümleler çözüldükçe anında çağrılır (canlı metin akışı) */
    suspend fun transcribe(
        audio: DecodedAudio,
        lang: Lang,
        onProgress: (Int) -> Unit,
        cancel: AtomicBoolean,
        onSegment: (Segment) -> Unit = {},
    ): EngineResult = withContext(Dispatchers.Default) {
        lock.withLock {
            val threads = threadCount()
            val base = EngineResult(name, model.label, lang.code, null, audio.durationMs, 0, 0, "", threads = threads)
            if (cancel.get()) return@withLock base.copy(error = CANCELLED)
            if (!ModelStore.isReady(context, model)) return@withLock base.copy(error = "Model indirilmedi")

            ensureBackends(context)
            val t0 = SystemClock.elapsedRealtime()
            val ctx = contextFor(context, model)
            val loadMs = SystemClock.elapsedRealtime() - t0
            if (ctx == 0L) return@withLock base.copy(loadMs = loadMs, error = "Model yüklenemedi (bellek yetersiz olabilir)")
            if (cancel.get()) return@withLock base.copy(loadMs = loadMs, error = CANCELLED)

            // Otomatik dil: büyük modeli iki kez çalıştırmamak için dili küçük (base) modelle bul
            var langCode = lang.code
            var detectMs = 0L
            var detectPath = if (lang == Lang.AUTO) "model_ici" else "secili"
            if (lang == Lang.AUTO && model != WhisperModel.BASE && ModelStore.isReady(context, WhisperModel.BASE)) {
                val td = SystemClock.elapsedRealtime()
                val d = detectorCtx(context)
                if (d != 0L) {
                    langCode = WhisperNative.nativeDetectLanguage(d, audio.samples, threads)
                    if (langCode != "auto") detectPath = "base"
                }
                detectMs = SystemClock.elapsedRealtime() - td
            }
            if (cancel.get()) return@withLock base.copy(loadMs = loadMs, detectMs = detectMs, error = CANCELLED)

            val report = onProgress
            val emit = onSegment
            val t1 = SystemClock.elapsedRealtime()
            var firstSegmentMs = -1L
            val listener = object : ProgressListener {
                override fun onProgress(percent: Int) { if (!cancel.get()) report(percent) }
                override fun isCancelled(): Boolean = cancel.get()
                override fun onSegment(startMs: Long, endMs: Long, text: ByteArray) {
                    if (cancel.get()) return
                    val t = text.toString(Charsets.UTF_8)
                    if (t.isNotBlank() && !Postprocess.isNoise(t)) {
                        if (firstSegmentMs < 0) firstSegmentMs = SystemClock.elapsedRealtime() - t1
                        emit(Segment(startMs, endMs, t.trim()))
                    }
                }
            }
            val bytes = WhisperNative.nativeTranscribe(
                ctx, audio.samples, langCode, threads, beamSize, allowFallback,
                ModelStore.vadFile(context).takeIf { ModelStore.vadReady(context) }?.absolutePath,
                listener,
            )
            val ms = SystemClock.elapsedRealtime() - t1
            val timed = base.copy(
                loadMs = loadMs, transcribeMs = ms, detectMs = detectMs, firstSegmentMs = firstSegmentMs, detectPath = detectPath,
            )
            if (cancel.get()) return@withLock timed.copy(error = CANCELLED)
            val raw = bytes?.toString(Charsets.UTF_8) ?: return@withLock timed.copy(error = "Bellek yetersiz")
            if (raw.startsWith("ERR")) return@withLock timed.copy(error = raw)

            var detected: String? = null
            var encodeMs = 0L
            var decodeMs = 0L
            var windows = 0
            var rawSegmentCount = 0
            val segs = mutableListOf<Segment>()
            raw.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                val p = line.split('\t')
                when (p[0]) {
                    "LANG" -> detected = p.getOrNull(1)?.takeIf { it != "?" } ?: langCode.takeIf { it != "auto" }
                    "TIME" -> {
                        // TIME <encoder_ort_ms> <pencere> <decoder_ort> <toplu_decoder_ort> <prompt_ort>
                        val encAvg = p.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                        windows = p.getOrNull(2)?.toIntOrNull() ?: 0
                        encodeMs = (encAvg * windows).toLong()
                        decodeMs = (ms - encodeMs).coerceAtLeast(0)
                    }
                    else -> {
                        val q = line.split('\t', limit = 3)
                        if (q.size == 3) {
                            val s = q[0].toLongOrNull()
                            val e = q[1].toLongOrNull()
                            if (s != null && e != null) {
                                rawSegmentCount++
                                if (q[2].isNotBlank() && !Postprocess.isNoise(q[2])) {
                                    segs += Segment(s, e, q[2].trim())
                                }
                            }
                        }
                    }
                }
            }
            timed.copy(
                detectedLanguage = detected,
                text = segs.joinToString(" ") { it.text },
                segments = segs,
                encodeMs = encodeMs,
                decodeMs = decodeMs,
                windows = windows,
                rawSegmentCount = rawSegmentCount,
            )
        }
    }

    companion object {
        const val CANCELLED = "İptal edildi"

        private val lock = Mutex()
        private var cachedModel: WhisperModel? = null
        private var cachedCtx: Long = 0L
        /** Dil algılama için ayrı, küçük base bağlamı; ana model base değilse kullanılır. */
        private var detectCtx: Long = 0L

        @Volatile var backendInfo: String = "-"
            private set

        /** Geliştirici araçlarından A/B ölçümü için elle thread sayısı (0 = otomatik). */
        @Volatile var threadOverride: Int = 0

        fun ensureBackends(context: Context) {
            if (backendInfo == "-") {
                backendInfo = WhisperNative.nativeLoadBackends(context.applicationInfo.nativeLibraryDir)
            }
        }

        fun systemInfo(): String = runCatching { WhisperNative.nativeSystemInfo() }.getOrDefault("-")

        /**
         * Yalnızca dil algılama (küçük base modeliyle, izin verilen diller arasından).
         * Hızlı modda sesin hangi dilin Wit uygulamasına gideceğini seçmek için.
         * Base modeli yoksa null döner.
         */
        suspend fun detectLanguage(context: Context, audio: DecodedAudio): String? =
            withContext(Dispatchers.Default) {
                if (!ModelStore.isReady(context, WhisperModel.BASE)) return@withContext null
                lock.withLock {
                    ensureBackends(context)
                    val d = detectorCtx(context)
                    if (d == 0L) null
                    else WhisperNative.nativeDetectLanguage(d, audio.samples, threadCount()).takeIf { it != "auto" }
                }
            }

        /**
         * Uygulama arka plana geçince modelleri bellekten boşalt (~200-900 MB).
         * Döküm sürüyorsa kilidi alamaz ve bir şey yapmaz.
         */
        fun releaseIfIdle() {
            if (!lock.tryLock()) return
            try {
                freeAll()
            } finally {
                lock.unlock()
            }
        }

        private fun freeAll() {
            if (cachedCtx != 0L) WhisperNative.nativeFree(cachedCtx)
            cachedCtx = 0L
            cachedModel = null
            if (detectCtx != 0L) WhisperNative.nativeFree(detectCtx)
            detectCtx = 0L
        }

        private fun detectorCtx(context: Context): Long {
            if (cachedModel == WhisperModel.BASE && cachedCtx != 0L) return cachedCtx
            if (detectCtx == 0L) detectCtx = WhisperNative.nativeInit(ModelStore.file(context, WhisperModel.BASE).absolutePath)
            return detectCtx
        }

        private fun contextFor(context: Context, model: WhisperModel): Long {
            if (cachedModel == model && cachedCtx != 0L) return cachedCtx
            if (cachedCtx != 0L) WhisperNative.nativeFree(cachedCtx)
            cachedCtx = 0L
            cachedModel = null
            // Büyük model (turbo) yüklenirken ikinci bağlamı bellekte tutma: düşük RAM'de süreç ölür
            if (model.isLarge && detectCtx != 0L) {
                WhisperNative.nativeFree(detectCtx)
                detectCtx = 0L
            }
            cachedCtx = WhisperNative.nativeInit(ModelStore.file(context, model).absolutePath)
            cachedModel = if (cachedCtx != 0L) model else null
            return cachedCtx
        }

        fun threadCount(): Int = threadOverride.takeIf { it > 0 } ?: autoThreads

        /**
         * Yalnızca performans çekirdekleri: küçük (verimlilik) çekirdeklere iş
         * verilirse whisper en yavaş çekirdeği bekler. Önce çekirdeklerin
         * çekirdek-göreli kapasitesi (cpu_capacity, çoğu Android çekirdeğinde var),
         * yoksa en yüksek frekans kullanılır. Ölçüm için geliştirici araçlarında
         * elle 2/3/4/6 seçilebilir.
         */
        val autoThreads: Int by lazy {
            val n = Runtime.getRuntime().availableProcessors()
            fun readAll(name: String) = (0 until n).mapNotNull { i ->
                runCatching { File("/sys/devices/system/cpu/cpu$i/$name").readText().trim().toLong() }.getOrNull()
            }
            val capacity = readAll("cpu_capacity")
            val values = if (capacity.size == n && capacity.distinct().size > 1) capacity
            else readAll("cpufreq/cpuinfo_max_freq")
            val big = if (values.size == n && values.isNotEmpty()) {
                val top = values.max()
                values.count { it >= top * 0.75 }
            } else n / 2
            big.coerceIn(2, 6)
        }

        /** Geliştirici araçları için: çekirdek kapasite/frekans dökümü. */
        fun cpuReport(): String {
            val n = Runtime.getRuntime().availableProcessors()
            return (0 until n).joinToString(" ") { i ->
                val cap = runCatching { File("/sys/devices/system/cpu/cpu$i/cpu_capacity").readText().trim() }.getOrDefault("?")
                val f = runCatching {
                    File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").readText().trim().toLong() / 1000
                }.getOrNull()?.toString() ?: "?"
                "c$i:$cap/${f}MHz"
            }
        }
    }
}

/**
 * Döküm sonrası temizlik. Gerçek konuşmayı silmemek için muhafazakâr:
 * bilinen "altyazı" uydurmaları yalnızca metnin başında/sonunda atılır,
 * aynı cümle ancak 3+ kez art arda gelirse (model döngüsü) teke indirilir.
 */
object Postprocess {
    /** Hiç anlam taşımayan parçalar — her konumda atılır. */
    fun isNoise(text: String): Boolean {
        val t = norm(text)
        return t.isEmpty() || t in NOISE || t == norm(TR_PROMPT) || t == norm(TR_PROMPT_TAIL)
    }

    fun clean(segs: List<Segment>): List<Segment> {
        var list = segs.filterNot { isNoise(it.text) }
        // Bilinen uydurmalar: yalnızca baştaki ve sondaki parçada (gerçek konuşmayı koru)
        while (list.isNotEmpty() && norm(list.last().text) in OUTROS) list = list.dropLast(1)
        while (list.isNotEmpty() && norm(list.first().text) in OUTROS) list = list.drop(1)
        // 3+ art arda aynı cümle => model döngüsü
        val out = mutableListOf<Segment>()
        var i = 0
        while (i < list.size) {
            var j = i
            while (j + 1 < list.size && norm(list[j + 1].text) == norm(list[i].text)) j++
            val run = j - i + 1
            if (run >= 3) out += list[i].copy(endMs = list[j].endMs) else for (k in i..j) out += list[k]
            i = j + 1
        }
        return out
    }

    private fun norm(s: String) = s.trim().lowercase().trimEnd('.', '!', '?', ' ', '…')

    /** sesyazi_jni.c içindeki Türkçe initial_prompt ile aynı olmalı. */
    private const val TR_PROMPT = "Merhaba, nasılsın? Yarın saat onda görüşelim. Çok teşekkür ederim, iyi günler."
    private const val TR_PROMPT_TAIL = "Yarın saat onda görüşelim. Çok teşekkür ederim, iyi günler."

    private val NOISE = setOf("", "...", "♪", "♪♪", "[müzik]", "[music]", "(müzik)", "(music)", "[sessizlik]", "[blank_audio]")

    private val OUTROS = setOf(
        "altyazı m.k", "izlediğiniz için teşekkürler", "izlediğiniz için teşekkür ederim",
        "abone olmayı unutmayın", "videoyu beğenmeyi unutmayın", "thank you for watching",
        "thanks for watching", "subtitles by the amara.org community", "please subscribe",
        "untertitel im auftrag des zdf", "sous-titres réalisés par la communauté d'amara.org",
    )
}
