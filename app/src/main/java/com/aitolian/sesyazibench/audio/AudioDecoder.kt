package com.aitolian.sesyazibench.audio

import android.content.Context
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

object AudioDecoder {
    const val TARGET_RATE = 16_000
    private const val TIMEOUT_US = 10_000L

    /** Her Android'in çözebildiği formatları (opus/ogg, m4a, mp3, mp4 videonun sesi…) 16 kHz mono'ya çevirir. */
    fun decode(context: Context, uri: Uri): DecodedAudio {
        val extractor = MediaExtractor()
        context.contentResolver.openFileDescriptor(uri, "r").use { pfd ->
            requireNotNull(pfd) { "Dosya açılamadı" }
            extractor.setDataSource(pfd.fileDescriptor)
        }
        try {
            val track = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: error("Dosyada ses izi yok")
            extractor.selectTrack(track)
            val format = extractor.getTrackFormat(track)
            val mime = format.getString(MediaFormat.KEY_MIME)!!

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            var outRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var outChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            var pcmFloat = false
            val mono = FloatList()
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false

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
                        outRate = f.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        outChannels = f.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        pcmFloat = f.containsKey(MediaFormat.KEY_PCM_ENCODING) &&
                            f.getInteger(MediaFormat.KEY_PCM_ENCODING) == android.media.AudioFormat.ENCODING_PCM_FLOAT
                    }
                    outIdx >= 0 -> {
                        val out = codec.getOutputBuffer(outIdx)!!.order(ByteOrder.nativeOrder())
                        out.position(info.offset); out.limit(info.offset + info.size)
                        appendMono(out, outChannels, pcmFloat, mono)
                        codec.releaseOutputBuffer(outIdx, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    }
                }
            }
            codec.stop(); codec.release()
            val resampled = resample(mono.toArray(), outRate, TARGET_RATE)
            return DecodedAudio(resampled, mime, outRate, outChannels)
        } finally {
            extractor.release()
        }
    }

    private fun appendMono(buf: ByteBuffer, channels: Int, isFloat: Boolean, dst: FloatList) {
        val ch = channels.coerceAtLeast(1)
        if (isFloat) {
            val fb = buf.asFloatBuffer()
            while (fb.remaining() >= ch) {
                var sum = 0f
                repeat(ch) { sum += fb.get() }
                dst.add(sum / ch)
            }
        } else {
            val sb = buf.asShortBuffer()
            while (sb.remaining() >= ch) {
                var sum = 0f
                repeat(ch) { sum += sb.get() / 32768f }
                dst.add(sum / ch)
            }
        }
    }

    /** Doğrusal ara değerleme — konuşma tanıma için yeterli. */
    private fun resample(input: FloatArray, from: Int, to: Int): FloatArray {
        if (from == to || input.isEmpty()) return input
        val outLen = (input.size.toLong() * to / from).toInt()
        val out = FloatArray(outLen)
        val ratio = from.toDouble() / to
        for (i in 0 until outLen) {
            val pos = i * ratio
            val i0 = pos.toInt().coerceAtMost(input.size - 1)
            val i1 = (i0 + 1).coerceAtMost(input.size - 1)
            val frac = (pos - i0).toFloat()
            out[i] = input[i0] * (1 - frac) + input[i1] * frac
        }
        return out
    }
}

/** Kutulama yapmadan büyüyen float listesi. */
private class FloatList {
    private var data = FloatArray(1 shl 16)
    private var size = 0
    fun add(v: Float) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }
    fun toArray(): FloatArray = data.copyOf(size)
}
