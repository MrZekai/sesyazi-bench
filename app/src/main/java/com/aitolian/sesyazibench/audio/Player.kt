package com.aitolian.sesyazibench.audio

import android.media.MediaPlayer
import java.io.File

/** Seçilen sesin kopyasını çalar (paylaşılan URI izni kısa ömürlü olduğu için). */
class Player {
    private var mp: MediaPlayer? = null
    /** Oynatıcı henüz hazırlanmadan seçilen konum (satıra dokunma) — ilk Play'de uygulanır. */
    private var pendingSeekMs = 0L
    var file: File? = null
        private set

    fun setSource(f: File?) {
        release()
        file = f
        pendingSeekMs = 0L
    }

    val isPlaying: Boolean get() = runCatching { mp?.isPlaying == true }.getOrDefault(false)
    val positionMs: Long get() = runCatching { mp?.currentPosition?.toLong() }.getOrNull() ?: pendingSeekMs

    fun toggle(onEnd: () -> Unit) {
        val f = file ?: return
        val p = mp ?: MediaPlayer().also {
            try {
                it.setDataSource(f.absolutePath)
                it.setOnCompletionListener { onEnd() }
                it.prepare()
            } catch (t: Throwable) {
                it.release()
                throw t
            }
            if (pendingSeekMs > 0) it.seekTo(pendingSeekMs.toInt())
            mp = it
        }
        if (p.isPlaying) p.pause() else p.start()
    }

    fun seekTo(ms: Long) {
        val p = mp
        if (p == null) pendingSeekMs = ms else runCatching { p.seekTo(ms.toInt()) }
    }

    fun release() {
        runCatching { mp?.release() }
        mp = null
    }
}

/** Dalga formu için sesi n sütuna indirger (0..1). */
fun DecodedAudio.peaks(n: Int = 48): FloatArray {
    if (samples.isEmpty()) return FloatArray(n)
    val step = (samples.size / n).coerceAtLeast(1)
    val out = FloatArray(n) { i ->
        var m = 0f
        val from = i * step
        val to = minOf(samples.size, from + step)
        for (k in from until to) { val v = kotlin.math.abs(samples[k]); if (v > m) m = v }
        m
    }
    val max = out.maxOrNull()?.takeIf { it > 0f } ?: 1f
    return FloatArray(n) { (out[it] / max).coerceIn(0.08f, 1f) }
}
