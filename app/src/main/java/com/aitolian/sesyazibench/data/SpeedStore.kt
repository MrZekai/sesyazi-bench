package com.aitolian.sesyazibench.data

import android.content.Context
import com.aitolian.sesyazibench.engine.WhisperModel

/**
 * Bu telefonda her modelin ölçülen hızı (işlem süresi / ses süresi).
 * Süre tahmini ve "işlem uzun sürecek mi?" kararı için kullanılır.
 */
object SpeedStore {
    private const val PREFS = "speed"

    /** Hiç ölçülmemişse orta segment bir telefon için temkinli varsayımlar. */
    private fun defaultRtf(m: WhisperModel) = when (m) {
        WhisperModel.TINY -> 0.3f
        WhisperModel.BASE -> 0.4f
        WhisperModel.SMALL -> 1.0f
        WhisperModel.TURBO -> 5.0f
        WhisperModel.TURBO_Q8 -> 4.0f
    }

    fun rtf(c: Context, m: WhisperModel): Float =
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(m.fileName, defaultRtf(m))

    /** Yeni ölçümü eskisiyle yumuşatarak kaydeder. */
    fun record(c: Context, m: WhisperModel, processMs: Long, audioMs: Long) {
        if (audioMs < 3_000 || processMs <= 0) return
        val measured = processMs.toFloat() / audioMs
        val prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val old = prefs.getFloat(m.fileName, -1f)
        val v = if (old < 0) measured else old * 0.5f + measured * 0.5f
        prefs.edit().putFloat(m.fileName, v).apply()
    }

    fun estimateMs(c: Context, m: WhisperModel, audioMs: Long): Long = (rtf(c, m) * audioMs).toLong() + 1_500
}
