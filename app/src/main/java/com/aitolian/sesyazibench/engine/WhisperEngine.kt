package com.aitolian.sesyazibench.engine

import android.content.Context
import android.os.SystemClock
import com.aitolian.sesyazibench.audio.DecodedAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * whisper.cpp motoru. Model bağlamı önbellekte tutulur (her dökümde yeniden
 * yüklenmez); aynı anda tek döküm çalışır.
 */
class WhisperEngine(
    private val context: Context,
    private val model: WhisperModel,
    private val beamSize: Int = 1,
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
            val base = EngineResult(name, model.label, lang.code, null, audio.durationMs, 0, 0, "")
            if (!ModelStore.isReady(context, model)) return@withLock base.copy(error = "Model indirilmedi")

            ensureBackends(context)
            val t0 = SystemClock.elapsedRealtime()
            val ctx = contextFor(context, model)
            val loadMs = SystemClock.elapsedRealtime() - t0
            if (ctx == 0L) return@withLock base.copy(loadMs = loadMs, error = "Model yüklenemedi (bellek yetersiz olabilir)")

            val report = onProgress
            val emit = onSegment
            val listener = object : ProgressListener {
                override fun onProgress(percent: Int) = report(percent)
                override fun isCancelled(): Boolean = cancel.get()
                override fun onSegment(startMs: Long, endMs: Long, text: ByteArray) {
                    val t = text.toString(Charsets.UTF_8)
                    if (t.isNotBlank() && !isHallucination(t)) emit(Segment(startMs, endMs, t.trim()))
                }
            }
            val t1 = SystemClock.elapsedRealtime()
            val raw = WhisperNative.nativeTranscribe(
                ctx, audio.samples, lang.code, threadCount(), beamSize,
                ModelStore.vadFile(context).takeIf { ModelStore.vadReady(context) }?.absolutePath,
                listener,
            ).toString(Charsets.UTF_8)
            val ms = SystemClock.elapsedRealtime() - t1
            if (cancel.get()) return@withLock base.copy(loadMs = loadMs, transcribeMs = ms, error = "İptal edildi")
            if (raw.startsWith("ERR")) return@withLock base.copy(loadMs = loadMs, transcribeMs = ms, error = raw)

            var detected: String? = null
            val segs = mutableListOf<Segment>()
            raw.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                val p = line.split('\t', limit = 3)
                if (p[0] == "LANG") detected = p.getOrNull(1)
                else if (p.size == 3 && p[2].isNotBlank() && !isHallucination(p[2])) {
                    segs += Segment(p[0].toLong(), p[1].toLong(), p[2].trim())
                }
            }
            base.copy(
                detectedLanguage = detected,
                loadMs = loadMs,
                transcribeMs = ms,
                text = segs.joinToString(" ") { it.text },
                segments = segs,
            )
        }
    }

    companion object {
        private val lock = Mutex()
        private var cachedModel: WhisperModel? = null
        private var cachedCtx: Long = 0L
        @Volatile var backendInfo: String = "-"
            private set

        fun ensureBackends(context: Context) {
            if (backendInfo == "-") {
                backendInfo = WhisperNative.nativeLoadBackends(context.applicationInfo.nativeLibraryDir)
            }
        }

        /**
         * Uygulama arka plana geçince modeli bellekten boşalt (~200-600 MB).
         * Döküm sürüyorsa kilidi alamaz ve bir şey yapmaz.
         */
        fun releaseIfIdle() {
            if (!lock.tryLock()) return
            try {
                if (cachedCtx != 0L) WhisperNative.nativeFree(cachedCtx)
                cachedCtx = 0L
                cachedModel = null
            } finally {
                lock.unlock()
            }
        }

        private fun contextFor(context: Context, model: WhisperModel): Long {
            if (cachedModel == model && cachedCtx != 0L) return cachedCtx
            if (cachedCtx != 0L) WhisperNative.nativeFree(cachedCtx)
            cachedCtx = WhisperNative.nativeInit(ModelStore.file(context, model).absolutePath)
            cachedModel = if (cachedCtx != 0L) model else null
            return cachedCtx
        }

        /**
         * Yalnızca performans çekirdekleri: küçük (verimlilik) çekirdeklere iş
         * verilirse whisper en yavaş çekirdeği bekler ve toplam süre uzar.
         * Çekirdeklerin en yüksek frekansı, en hızlı çekirdeğin %85.inden
         * yüksekse "büyük" sayılır.
         */
        fun threadCount(): Int = bigCores

        private val bigCores: Int by lazy {
            val n = Runtime.getRuntime().availableProcessors()
            val freqs = (0 until n).mapNotNull { i ->
                runCatching {
                    java.io.File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").readText().trim().toLong()
                }.getOrNull()
            }
            val big = if (freqs.size == n && freqs.isNotEmpty()) {
                val top = freqs.max()
                freqs.count { it >= top * 0.85 }
            } else n / 2
            big.coerceIn(2, 6)
        }

        /**
         * Whisper'ın sessizlik/müzik üzerinde uydurduğu bilinen kalıplar
         * (YouTube altyazılarından öğrenilmiş). Tam eşleşirse satır atılır.
         */
        private val HALLUCINATIONS = listOf(
            "altyazı m.k.", "altyazı m.k", "izlediğiniz için teşekkürler", "izlediğiniz için teşekkür ederim",
            "abone olmayı unutmayın", "videoyu beğenmeyi unutmayın", "thank you for watching",
            "thanks for watching", "subtitles by the amara.org community", "please subscribe",
            "untertitel im auftrag des zdf", "sous-titres réalisés par la communauté d'amara.org",
            // Türkçe başlangıç ipucunun (initial_prompt) metne sızması
            "merhaba, nasılsın? yarın saat onda görüşelim. çok teşekkür ederim, iyi günler",
            "yarın saat onda görüşelim. çok teşekkür ederim, iyi günler",
            "[müzik]", "[music]", "(müzik)", "(music)", "♪", "...",
        )

        fun isHallucination(text: String): Boolean {
            val t = text.trim().lowercase().trimEnd('.', '!', ' ')
            if (t.isEmpty()) return true
            return HALLUCINATIONS.any { h -> t == h.trimEnd('.', '!', ' ') }
        }
    }
}
