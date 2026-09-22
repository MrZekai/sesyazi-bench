package com.aitolian.sesyazibench.engine

import android.content.Context
import android.os.SystemClock
import com.aitolian.sesyazibench.audio.DecodedAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhisperEngine(private val context: Context, private val model: WhisperModel) : TranscriptionEngine {
    override val name = "whisper.cpp"

    override suspend fun transcribe(audio: DecodedAudio, lang: Lang): EngineResult = withContext(Dispatchers.Default) {
        val base = EngineResult(name, model.label, lang.code, null, audio.durationMs, 0, 0, "")
        if (!ModelStore.isReady(context, model)) return@withContext base.copy(error = "Model indirilmedi")

        val t0 = SystemClock.elapsedRealtime()
        val ctx = WhisperNative.nativeInit(ModelStore.file(context, model).absolutePath)
        val loadMs = SystemClock.elapsedRealtime() - t0
        if (ctx == 0L) return@withContext base.copy(loadMs = loadMs, error = "Model yüklenemedi (bellek?)")

        try {
            val t1 = SystemClock.elapsedRealtime()
            val raw = WhisperNative.nativeTranscribe(ctx, audio.samples, lang.code, threadCount())
                .toString(Charsets.UTF_8)
            val ms = SystemClock.elapsedRealtime() - t1
            if (raw.startsWith("ERR")) return@withContext base.copy(loadMs = loadMs, transcribeMs = ms, error = raw)

            var detected: String? = null
            val segs = mutableListOf<Segment>()
            raw.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                val p = line.split('\t', limit = 3)
                if (p[0] == "LANG") detected = p.getOrNull(1)
                else if (p.size == 3) segs += Segment(p[0].toLong(), p[1].toLong(), p[2].trim())
            }
            base.copy(
                detectedLanguage = detected,
                loadMs = loadMs,
                transcribeMs = ms,
                text = segs.joinToString(" ") { it.text },
                segments = segs,
            )
        } finally {
            WhisperNative.nativeFree(ctx)
        }
    }

    companion object {
        /** Büyük çekirdek sayısı genelde 4'ü geçmez; fazlası küçük çekirdeklerde yavaşlatır. */
        fun threadCount(): Int = Runtime.getRuntime().availableProcessors().coerceIn(1, 4)
    }
}
