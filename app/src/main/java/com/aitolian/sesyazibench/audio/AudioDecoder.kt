package com.aitolian.sesyazibench.audio

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteOrder

/** Kullanıcıya gösterilecek anlaşılır çözme hatası. */
class DecodeException(message: String) : Exception(message)

object AudioDecoder {
    const val TARGET_RATE = AUDIO_RATE
    /** Telefon belleği için üst sınır (30 dk ≈ 115 MB float). */
    const val MAX_DURATION_MS = 30 * 60 * 1000L
    private const val TIMEOUT_US = 10_000L

    /**
     * Android'in çözebildiği her formatı (WhatsApp opus/ogg, m4a, mp3, wav,
     * videonun ses izi…) 16 kHz mono'ya çevirir. Dönüştürme akış halinde
     * yapılır; uzun videolarda bile bellek kaynak hızına göre değil 16 kHz'e
     * göre büyür.
     */
    /**
     * @param channelPick ≥ 0 ise çok kanallı seste yalnız o kanal alınır. Normalde -1
     *   (ortalama). Çağıran, teşhisteki faz oranı zıt fazı gösterirse bununla yeniden çözer.
     */
    fun decode(context: Context, uri: Uri, cancelled: () -> Boolean = { false }, channelPick: Int = -1): DecodedAudio {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            context.contentResolver.openFileDescriptor(uri, "r").use { pfd ->
                if (pfd == null) throw DecodeException("Dosya açılamadı")
                try {
                    extractor.setDataSource(pfd.fileDescriptor)
                } catch (t: Throwable) {
                    throw DecodeException("Bu dosya okunamadı (desteklenmeyen biçim)")
                }
            }
            // İlk ses izi seçilir (davranış değişmedi); birden çok iz varsa teşhiste görünür
            val audioTracks = (0 until extractor.trackCount).filter {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            }
            val track = audioTracks.firstOrNull() ?: throw DecodeException("Bu dosyada ses bulunamadı")
            extractor.selectTrack(track)
            val format = extractor.getTrackFormat(track)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: "audio/?"
            if (format.containsKey(MediaFormat.KEY_DURATION) &&
                format.getLong(MediaFormat.KEY_DURATION) / 1000 > MAX_DURATION_MS
            ) throw DecodeException("Şimdilik en fazla 30 dakikalık ses destekleniyor")

            codec = try {
                MediaCodec.createDecoderByType(mime).also {
                    it.configure(format, null, null, 0)
                    it.start()
                }
            } catch (t: Throwable) {
                throw DecodeException("Bu ses biçimi ($mime) telefonunda desteklenmiyor")
            }

            var inRate = format.intOr(MediaFormat.KEY_SAMPLE_RATE, 44_100)
            var channels = format.intOr(MediaFormat.KEY_CHANNEL_COUNT, 1)
            var pcmFloat = false
            // Süre biliniyorsa çıktı dizisini baştan doğru boyutta ayır (30 dk seste
            // ikiye katlanarak büyümek ~135 MB + kopya demekti)
            val expectedOut = if (format.containsKey(MediaFormat.KEY_DURATION))
                (format.getLong(MediaFormat.KEY_DURATION) / 1_000_000.0 * TARGET_RATE).toInt() + TARGET_RATE else 0
            var resampler = StreamResampler(inRate, TARGET_RATE, expectedOut)
            val downmix = Downmix(channelPick)
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var idleAfterEos = 0
            var lastProgressAt = android.os.SystemClock.elapsedRealtime()

            while (!outputDone) {
                if (cancelled()) throw DecodeException("İptal edildi")
                // Girdi/çıktı vermeyen (takılan) çözücü: 20 sn ilerleme yoksa bırak
                if (android.os.SystemClock.elapsedRealtime() - lastProgressAt > 20_000) {
                    throw DecodeException("Ses çözülemedi (çözücü yanıt vermiyor)")
                }
                if (!inputDone) {
                    val inIdx = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inIdx >= 0) {
                        val inBuf = codec.getInputBuffer(inIdx)!!
                        val size = extractor.readSampleData(inBuf, 0)
                        lastProgressAt = android.os.SystemClock.elapsedRealtime()
                        if (size < 0) {
                            codec.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inIdx, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val outIdx = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                when {
                    outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val f = codec.outputFormat
                        val newRate = f.intOr(MediaFormat.KEY_SAMPLE_RATE, inRate)
                        channels = f.intOr(MediaFormat.KEY_CHANNEL_COUNT, channels)
                        pcmFloat = f.intOr(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT) ==
                            AudioFormat.ENCODING_PCM_FLOAT
                        if (newRate != inRate) {
                            inRate = newRate
                            resampler = resampler.withNewRate(inRate)
                        }
                    }
                    outIdx >= 0 -> {
                        idleAfterEos = 0
                        lastProgressAt = android.os.SystemClock.elapsedRealtime()
                        if (info.size > 0) {
                            val out = codec.getOutputBuffer(outIdx)!!.order(ByteOrder.nativeOrder())
                            out.position(info.offset); out.limit(info.offset + info.size)
                            resampler.push(downmix.toMono(out, channels, pcmFloat))
                            if (resampler.outputSize.toLong() * 1000 / TARGET_RATE > MAX_DURATION_MS) {
                                throw DecodeException("Şimdilik en fazla 30 dakikalık ses destekleniyor")
                            }
                        }
                        codec.releaseOutputBuffer(outIdx, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    }
                    inputDone -> if (++idleAfterEos > 300) outputDone = true // bazı çözücüler EOS bayrağı vermez
                }
            }
            val pcm = resampler.finish()
            val audioInfo = AudioInfo(
                trackCount = extractor.trackCount,
                audioTrackCount = audioTracks.size,
                selectedTrack = track,
                mime = mime,
                channels = channels,
                sourceRate = inRate,
                pcmFloat = pcmFloat,
                sourceDurationMs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) / 1000 else -1,
                decodedSamples = pcm.size,
                monoPhaseRatio = downmix.phaseRatio,
                channelPicked = if (channels >= 2) channelPick else -1,
            )
            return DecodedAudio(pcm, mime, inRate, channels, audioInfo)
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { extractor.release() }
        }
    }

    private fun MediaFormat.intOr(key: String, default: Int): Int =
        if (containsKey(key)) runCatching { getInteger(key) }.getOrDefault(default) else default
}
