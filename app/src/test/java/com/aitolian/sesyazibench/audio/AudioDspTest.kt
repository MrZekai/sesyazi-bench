package com.aitolian.sesyazibench.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Ses çıkarma ölçümleri (sentetik sinyal). Davranışı DEĞİŞTİRMEZ; mevcut
 * dönüştürmenin süre/genlik/katlanma değerlerini sabitler ve raporlar.
 * Gerçek kullanıcı şarkısındaki kaybın nedeni bu testlerle kanıtlanmış olmaz.
 */
class AudioDspTest {

    private fun tone(rate: Int, hz: Double, sec: Double, amp: Double = 0.5) =
        FloatArray((rate * sec).toInt()) { (amp * sin(2 * PI * hz * it / rate)).toFloat() }

    private fun rms(a: FloatArray, from: Int = 0, to: Int = a.size): Double {
        var e = 0.0
        for (i in from until to) e += a[i].toDouble() * a[i]
        return sqrt(e / (to - from))
    }

    /** Kodek çıkışını taklit: parça parça (4096 örnek) itilir. */
    private fun resample(input: FloatArray, from: Int): FloatArray {
        val r = StreamResampler(from, AUDIO_RATE)
        var i = 0
        while (i < input.size) {
            val n = minOf(4096, input.size - i)
            r.push(input.copyOfRange(i, i + n))
            i += n
        }
        return r.finish()
    }

    @Test fun durationIsPreservedFor44100And48000And8000() {
        for (rate in listOf(44_100, 48_000, 22_050, 8_000)) {
            val out = resample(tone(rate, 440.0, 10.0), rate)
            val expected = 10 * AUDIO_RATE
            assertTrue("$rate Hz: ${out.size} örnek", abs(out.size - expected) <= 2)
        }
    }

    @Test fun speechBandToneKeepsAmplitude() {
        for (rate in listOf(44_100, 48_000)) {
            val out = resample(tone(rate, 1000.0, 2.0), rate)
            val ratio = rms(out, 1000, out.size - 1000) / (0.5 / sqrt(2.0))
            println("1 kHz genlik oranı @${rate}: ${"%.3f".format(ratio)}")
            assertTrue("1 kHz @${rate} oranı $ratio", ratio in 0.9..1.02)
        }
    }

    /**
     * Ölçüm: 16 kHz'de temsil edilemeyen (> 8 kHz) tonların katlanan kalıntısı.
     * (12 kHz @48k, 4 örneklik pencerede tam sıfırlandığı için temsilî değil; 10 ve 13 kHz ölçülür.)
     */
    @Test fun aliasingIsMeasuredForBoxcarFilter() {
        for (rate in listOf(48_000, 44_100)) for (hz in listOf(10_000.0, 13_000.0)) {
            val out = resample(tone(rate, hz, 2.0), rate)
            val residual = rms(out, 1000, out.size - 1000) / (0.5 / sqrt(2.0))
            println("katlanma kalıntısı ${hz.toInt()} Hz @$rate: ${"%.3f".format(residual)} (1 = hiç bastırılmamış)")
            // Pencere ortalaması zayıf bir süzgeçtir; yalnız "tamamen geçirmiyor" olduğunu sabitler
            assertTrue(residual < 0.9)
        }
    }

    private fun stereoPcm16(l: FloatArray, r: FloatArray): ByteBuffer {
        val b = ByteBuffer.allocate(l.size * 4).order(ByteOrder.nativeOrder())
        for (i in l.indices) {
            b.putShort((l[i] * 32767).toInt().toShort())
            b.putShort((r[i] * 32767).toInt().toShort())
        }
        b.flip()
        return b
    }

    @Test fun monoOfIdenticalChannelsIsLossless() {
        val t = tone(AUDIO_RATE, 500.0, 1.0)
        val d = Downmix()
        val m = d.toMono(stereoPcm16(t, t), 2, false)
        assertEquals(rms(t), rms(m), 0.01)
        assertEquals(1.0, d.phaseRatio, 0.02)
    }

    @Test fun antiPhaseStereoCancelsAndIsFlagged() {
        val t = tone(AUDIO_RATE, 500.0, 1.0)
        val inv = FloatArray(t.size) { -t[it] }
        val d = Downmix()
        val m = d.toMono(stereoPcm16(t, inv), 2, false)
        assertTrue(rms(m) < 0.001)                         // mono toplamda ses kaybolur
        assertTrue(d.phaseRatio < 0.05)                    // teşhiste görünür
    }

    @Test fun speechOnOneChannelIsHalvedNotLost() {
        val t = tone(AUDIO_RATE, 500.0, 1.0)
        val d = Downmix()
        val m = d.toMono(stereoPcm16(t, FloatArray(t.size)), 2, false)
        assertEquals(rms(t) / 2, rms(m), 0.01)             // -6 dB, içerik korunur
        assertEquals(0.5, d.phaseRatio, 0.02)
    }

    @Test fun monoSourceHasNoPhaseRatio() {
        val t = tone(AUDIO_RATE, 500.0, 0.1)
        val b = ByteBuffer.allocate(t.size * 2).order(ByteOrder.nativeOrder())
        t.forEach { b.putShort((it * 32767).toInt().toShort()) }
        b.flip()
        val d = Downmix()
        d.toMono(b, 1, false)
        assertEquals(-1.0, d.phaseRatio, 0.0)
    }
}
