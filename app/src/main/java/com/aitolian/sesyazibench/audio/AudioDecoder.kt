package com.aitolian.sesyazibench.audio

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** 16 kHz mono ses: whisper için float, ML Kit / platform STT için 16-bit PCM. */
class DecodedAudio(val samples: FloatArray, val sourceMime: String, val sourceRate: Int, val sourceChannels: Int) {
    val durationMs: Long get() = samples.size * 1000L / AudioDecoder.TARGET_RATE

    /** Başlıksız 16-bit little-endian PCM dosyası yazar (ML Kit ve platform STT bunu ister). */
    fun writePcm16(file: File): File {
        val buf = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (s in samples) buf.putShort((s.coerceIn(-1f, 1f) * 32767f).toInt().toShort())
        FileOutputStream(file).use { it.write(buf.array()) }
        return file
    }
}

/** Kullanıcıya gösterilecek anlaşılır çözme hatası. */
class DecodeException(message: String) : Exception(message)

object AudioDecoder {
    const val TARGET_RATE = 16_000
    /** Telefon belleği için üst sınır (30 dk ≈ 115 MB float). */
    const val MAX_DURATION_MS = 30 * 60 * 1000L
    private const val TIMEOUT_US = 10_000L

    /**
     * Android'in çözebildiği her formatı (WhatsApp opus/ogg, m4a, mp3, wav,
     * videonun ses izi…) 16 kHz mono'ya çevirir. Dönüştürme akış halinde
     * yapılır; uzun videolarda bile bellek kaynak hızına göre değil 16 kHz'e
     * göre büyür.
     */
    fun decode(context: Context, uri: Uri): DecodedAudio {
        val extractor = MediaExtractor()
        context.contentResolver.openFileDescriptor(uri, "r").use { pfd ->
            if (pfd == null) throw DecodeException("Dosya açılamadı")
            try {
                extractor.setDataSource(pfd.fileDescriptor)
            } catch (t: Throwable) {
                throw DecodeException("Bu dosya okunamadı (desteklenmeyen biçim)")
            }
        }
        var codec: MediaCodec? = null
        try {
            val track = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: throw DecodeException("Bu dosyada ses bulunamadı")
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
            var resampler = StreamResampler(inRate, TARGET_RATE)
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var idleAfterEos = 0

            while (!outputDone) {
                if (!inputDone) {
                    val inIdx = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inIdx >= 0) {
                        val inBuf = codec.getInputBuffer(inIdx)!!
                        val size = extractor.readSampleData(inBuf, 0)
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
                        if (info.size > 0) {
                            val out = codec.getOutputBuffer(outIdx)!!.order(ByteOrder.nativeOrder())
                            out.position(info.offset); out.limit(info.offset + info.size)
                            resampler.push(toMono(out, channels, pcmFloat))
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
            return DecodedAudio(resampler.finish(), mime, inRate, channels)
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            extractor.release()
        }
    }

    private fun toMono(buf: ByteBuffer, channels: Int, isFloat: Boolean): FloatArray {
        val ch = channels.coerceAtLeast(1)
        return if (isFloat) {
            val fb = buf.asFloatBuffer()
            FloatArray(fb.remaining() / ch) {
                var sum = 0f
                repeat(ch) { sum += fb.get() }
                sum / ch
            }
        } else {
            val sb = buf.asShortBuffer()
            FloatArray(sb.remaining() / ch) {
                var sum = 0f
                repeat(ch) { sum += sb.get() / 32768f }
                sum / ch
            }
        }
    }

    private fun MediaFormat.intOr(key: String, default: Int): Int =
        if (containsKey(key)) runCatching { getInteger(key) }.getOrDefault(default) else default
}

/**
 * Akış halinde örnekleme hızı dönüştürücü.
 * Aşağı örneklemede (48k→16k) pencere ortalaması ile alçak geçiren filtre
 * uygular — yoksa yüksek frekanslar konuşma bandına katlanır (aliasing) ve
 * tanıma kalitesi düşer. Yukarı örneklemede doğrusal ara değerleme yapar.
 */
private class StreamResampler(private val from: Int, private val to: Int) {
    private val ratio = from.toDouble() / to
    private val half = ratio / 2.0
    private var pending = FloatArray(0)
    private var pendingStart = 0L       // pending[0]'ın küresel indeksi
    private var nextPos = 0.0           // sıradaki çıktının küresel giriş konumu
    private val out = Growable()

    val outputSize: Int get() = out.size

    fun push(chunk: FloatArray) {
        if (chunk.isEmpty()) return
        pending = pending + chunk
        drain(final = false)
    }

    fun finish(): FloatArray {
        drain(final = true)
        return out.toArray()
    }

    /** Kodek çıkış hızı değişirse (nadir) biriken sesi bitirip yeni hızla devam et. */
    fun withNewRate(newFrom: Int): StreamResampler {
        drain(final = true)
        return StreamResampler(newFrom, to).also { it.out.addAll(out.toArray()) }
    }

    private fun drain(final: Boolean) {
        val end = pendingStart + pending.size
        while (true) {
            val need = if (ratio > 1.0) nextPos + half else nextPos + 1
            if (!final && need >= end) break
            if (nextPos >= end) break
            val v = if (ratio > 1.0) {
                val a = (nextPos - half).toLong().coerceAtLeast(pendingStart)
                val b = (nextPos + half).toLong().coerceAtMost(end - 1)
                var sum = 0f
                for (k in a..b) sum += pending[(k - pendingStart).toInt()]
                sum / (b - a + 1)
            } else {
                val i0 = nextPos.toLong().coerceIn(pendingStart, end - 1)
                val i1 = (i0 + 1).coerceAtMost(end - 1)
                val frac = (nextPos - i0).toFloat()
                pending[(i0 - pendingStart).toInt()] * (1 - frac) + pending[(i1 - pendingStart).toInt()] * frac
            }
            out.add(v)
            nextPos += ratio
        }
        // Artık gerekmeyen örnekleri at
        val keepFrom = ((nextPos - half).toLong() - 1).coerceAtLeast(pendingStart)
        val drop = (keepFrom - pendingStart).toInt().coerceIn(0, pending.size)
        if (drop > 0) {
            pending = pending.copyOfRange(drop, pending.size)
            pendingStart += drop
        }
    }
}

/** Kutulama yapmadan büyüyen float listesi. */
private class Growable {
    private var data = FloatArray(1 shl 16)
    var size = 0
        private set

    fun add(v: Float) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    fun addAll(v: FloatArray) = v.forEach { add(it) }
    fun toArray(): FloatArray = data.copyOf(size)
}
