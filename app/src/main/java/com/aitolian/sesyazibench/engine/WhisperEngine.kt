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
class WhisperEngine(private val context: Context, private val model: WhisperModel) : TranscriptionEngine {
    override val name = "whisper.cpp"

    override suspend fun transcribe(audio: DecodedAudio, lang: Lang): EngineResult =
        transcribe(audio, lang, onProgress = {}, cancel = AtomicBoolean(false))

    suspend fun transcribe(
        audio: DecodedAudio,
        lang: Lang,
        onProgress: (Int) -> Unit,
        cancel: AtomicBoolean,
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
            val listener = object : ProgressListener {
                override fun onProgress(percent: Int) = report(percent)
                override fun isCancelled(): Boolean = cancel.get()
            }
            val t1 = SystemClock.elapsedRealtime()
            val raw = WhisperNative.nativeTranscribe(
                ctx, audio.samples, lang.code, threadCount(), audioCtxFor(audio.durationMs), listener,
            ).toString(Charsets.UTF_8)
            val ms = SystemClock.elapsedRealtime() - t1
            if (cancel.get()) return@withLock base.copy(loadMs = loadMs, transcribeMs = ms, error = "İptal edildi")
            if (raw.startsWith("ERR")) return@withLock base.copy(loadMs = loadMs, transcribeMs = ms, error = raw)

            var detected: String? = null
            val segs = mutableListOf<Segment>()
            raw.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                val p = line.split('\t', limit = 3)
                if (p[0] == "LANG") detected = p.getOrNull(1)
                else if (p.size == 3 && p[2].isNotBlank()) segs += Segment(p[0].toLong(), p[1].toLong(), p[2].trim())
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

        private fun contextFor(context: Context, model: WhisperModel): Long {
            if (cachedModel == model && cachedCtx != 0L) return cachedCtx
            if (cachedCtx != 0L) WhisperNative.nativeFree(cachedCtx)
            cachedCtx = WhisperNative.nativeInit(ModelStore.file(context, model).absolutePath)
            cachedModel = if (cachedCtx != 0L) model else null
            return cachedCtx
        }

        /** Büyük çekirdek sayısı genelde 4'ü geçmez; fazlası küçük çekirdeklerde yavaşlatır. */
        fun threadCount(): Int = Runtime.getRuntime().availableProcessors().coerceIn(1, 4)

        /**
         * Whisper her zaman 30 sn'lik pencere işler. Kısa seslerde pencereyi sesin
         * boyuna indirmek (1 sn = 50 kare) kodlayıcıyı 2-4 kat hızlandırır.
         */
        fun audioCtxFor(durationMs: Long): Int {
            if (durationMs >= 29_000) return 0
            val frames = (durationMs / 20).toInt() + 64
            return ((frames + 63) / 64 * 64).coerceIn(256, 1500)
        }
    }
}
