package com.aitolian.sesyazibench.audio

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Çözülmüş sesin örnekleme hızı (mono, 16 kHz). Android bağımlılığı yok (birim testlerde kullanılır). */
const val AUDIO_RATE = 16_000

/**
 * Ses çıkarma teşhisi (yalnız sayılar; içerik yok). Geliştirici modunda
 * "Son döküm teşhisi" olarak gösterilir.
 *
 * @param monoPhaseRatio mono enerji / kanalların ortalama enerjisi. Aynı kanallar ≈ 1,
 *   ilişkisiz stereo ≈ 0,5, zıt fazlı içerik → 0'a yakın (mono toplamda sönümlenir).
 *   Tek kanalda -1.
 */
data class AudioInfo(
    val trackCount: Int,
    val audioTrackCount: Int,
    val selectedTrack: Int,
    val mime: String,
    val channels: Int,
    val sourceRate: Int,
    val pcmFloat: Boolean,
    val sourceDurationMs: Long,
    val decodedSamples: Int,
    val monoPhaseRatio: Double,
) {
    val decodedMs: Long get() = decodedSamples * 1000L / AUDIO_RATE
}

/** 16 kHz mono ses: whisper için float, ML Kit / platform STT için 16-bit PCM. */
class DecodedAudio(
    val samples: FloatArray,
    val sourceMime: String,
    val sourceRate: Int,
    val sourceChannels: Int,
    val info: AudioInfo? = null,
) {
    val durationMs: Long get() = samples.size * 1000L / AUDIO_RATE

    /** Başlıksız 16-bit little-endian PCM dosyası yazar (ML Kit ve platform STT bunu ister). */
    fun writePcm16(file: File): File {
        val buf = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (s in samples) buf.putShort((s.coerceIn(-1f, 1f) * 32767f).toInt().toShort())
        FileOutputStream(file).use { it.write(buf.array()) }
        return file
    }

    /** Aralığın kopyası (başarısız bölümü telefonda tamamlamak için). */
    fun slice(from: Int, to: Int) = DecodedAudio(samples.copyOfRange(from, to), sourceMime, sourceRate, sourceChannels)
}
