package com.aitolian.sesyazibench.audio

import java.nio.ByteBuffer

/**
 * Kanal birleştirme (mono). Davranış değiştirilmedi: kanalların ortalaması.
 * Yalnız ölçüm eklendi: mono enerjisinin kanal enerjisine oranı (zıt fazlı
 * içerik mono toplamda sönümlenir; bu oran bunu görünür kılar).
 * Android bağımlılığı yok (birim testlerde kullanılır).
 */
internal class Downmix(
    /** ≥ 0: yalnız bu kanal kullanılır (zıt fazlı stereo'da sönümlenmeyi önlemek için); -1: ortalama. */
    private val pick: Int = -1,
) {
    private var monoEnergy = 0.0
    private var channelEnergy = 0.0
    private var maxChannels = 1

    fun toMono(buf: ByteBuffer, channels: Int, isFloat: Boolean): FloatArray {
        val ch = channels.coerceAtLeast(1)
        if (ch > maxChannels) maxChannels = ch
        return if (isFloat) {
            val fb = buf.asFloatBuffer()
            FloatArray(fb.remaining() / ch) { mix(ch) { fb.get() } }
        } else {
            val sb = buf.asShortBuffer()
            FloatArray(sb.remaining() / ch) { mix(ch) { sb.get() / 32768f } }
        }
    }

    private inline fun mix(ch: Int, next: () -> Float): Float {
        var sum = 0f
        var e = 0.0
        var picked = 0f
        repeat(ch) { k ->
            val v = next()
            if (k == pick) picked = v
            sum += v
            e += v.toDouble() * v
        }
        val m = if (pick in 0 until ch) picked else sum / ch
        monoEnergy += m.toDouble() * m
        channelEnergy += e / ch
        return m
    }

    /** Tek kanalda ya da sessizlikte -1. */
    val phaseRatio: Double
        get() = if (maxChannels < 2 || channelEnergy <= 1e-9) -1.0 else monoEnergy / channelEnergy
}

/**
 * Akış halinde örnekleme hızı dönüştürücü.
 * Aşağı örneklemede (48k→16k) pencere ortalaması ile alçak geçiren filtre
 * uygular — yoksa yüksek frekanslar konuşma bandına katlanır (aliasing).
 * Pencere ortalaması zayıf bir filtredir (bkz. AudioDspTest ölçümleri); gerçek
 * bir tanıma kaybı kanıtlanmadan değiştirilmedi. Yukarı örneklemede doğrusal
 * ara değerleme yapar.
 */
internal class StreamResampler(private val from: Int, private val to: Int, expectedOut: Int = 0) {
    private val ratio = from.toDouble() / to
    private val half = ratio / 2.0
    private var pending = FloatArray(0)
    private var pendingStart = 0L       // pending[0]'ın küresel indeksi
    private var nextPos = 0.0           // sıradaki çıktının küresel giriş konumu
    private val out = Growable(expectedOut)

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
        return StreamResampler(newFrom, to, out.capacity).also { it.out.addAll(out.toArray()) }
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
internal class Growable(initial: Int = 0) {
    private var data = FloatArray(maxOf(initial, 1 shl 16))
    var size = 0
        private set
    val capacity: Int get() = data.size

    fun add(v: Float) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    fun addAll(v: FloatArray) = v.forEach { add(it) }
    /** Dizi tam dolmuşsa kopyalamadan verir (uzun seste bellek zirvesini yarıya indirir). */
    fun toArray(): FloatArray = if (size == data.size) data else data.copyOf(size)
}
