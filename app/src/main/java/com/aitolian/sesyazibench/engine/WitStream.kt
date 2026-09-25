package com.aitolian.sesyazibench.engine

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/*
 * Wit.ai /dictation yanıt işleme — saf Kotlin (Android bağımlılığı yok), birim
 * testlerle doğrulanır. Ağ ve zamanlama WitEngine'dedir.
 */

internal const val WIT_ERR_AUTH = "WIT_AUTH"
internal const val WIT_ERR_RATE = "WIT_RATE_LIMIT"
internal const val WIT_ERR_SERVER = "WIT_SERVER"
internal const val WIT_ERR_NETWORK = "WIT_NETWORK"
internal const val WIT_ERR_TIMEOUT = "WIT_TIMEOUT"
internal const val WIT_ERR_TRUNCATED = "WIT_TRUNCATED"
internal const val WIT_ERR_INVALID = "WIT_INVALID_JSON"
internal const val WIT_ERR_SERVICE = "WIT_ERROR"
/** HTTP 200 ama gövdede hiç protokol olayı yok (boş / yalnız boşluk). */
internal const val WIT_ERR_EMPTY = "WIT_EMPTY_BODY"
/** Gövdede JSON var ama hiçbiri tanınan bir transkripsiyon olayı değil (ör. yalnız `{}`). */
internal const val WIT_ERR_SCHEMA = "WIT_UNKNOWN_EVENTS"
/** Transkripsiyon olayları geldi ama hiçbiri final değil (akış sonlandırılmadı). */
internal const val WIT_ERR_NO_FINAL = "WIT_NO_FINAL"

/**
 * Tek akış olayı. start/end: belirteç zamanları (ms varsayılır, parçaya göre); yoksa -1.
 * [recognized] = tanınan transkripsiyon olayı ("text", "is_final" ya da
 * PARTIAL/FINAL_TRANSCRIPTION türü taşıyor). Tanınmayan nesneler (ör. `{}`,
 * bilinmeyen üst veri) sayılır ama metin/final kanıtı sayılmaz.
 */
internal class WitEvent(
    val text: String,
    val isFinal: Boolean,
    val start: Long,
    val end: Long,
    val recognized: Boolean = true,
) {
    /** Zaman aralığı kullanılabilir mi (bilinmeyen zaman "aynı olay" kanıtı sayılmaz). */
    val hasTimes: Boolean get() = start >= 0 && end > start
}

internal fun parseWitEvent(json: String): WitEvent {
    val o = try { JSONObject(json) } catch (_: JSONException) { throw IOException(WIT_ERR_INVALID) }
    if (o.has("error")) throw IOException(WIT_ERR_SERVICE)
    val type = o.optString("type")
    val recognized = o.has("text") || o.has("is_final") ||
        type.equals("FINAL_TRANSCRIPTION", ignoreCase = true) || type.equals("PARTIAL_TRANSCRIPTION", ignoreCase = true)
    if (!recognized) return WitEvent("", false, -1, -1, recognized = false)
    val text = o.optString("text").trim()
    val isFinal = o.optBoolean("is_final", false) || type.equals("FINAL_TRANSCRIPTION", ignoreCase = true)
    var start = -1L
    var end = -1L
    o.optJSONObject("speech")?.optJSONArray("tokens")?.let { toks ->
        if (toks.length() > 0) {
            start = toks.optJSONObject(0)?.optLong("start", -1) ?: -1
            end = toks.optJSONObject(toks.length() - 1)?.optLong("end", -1) ?: -1
        }
    }
    return WitEvent(text, isFinal, start, end)
}

/**
 * Bir parçanın akış durumu ve sayaçları (teşhis için; metin saklanmaz).
 *
 * Tekrar kuralları:
 *  - Final yalnızca GÜVENİLİR kanıtla atılır: önceki finalle aynı metin VE ikisinin
 *    de geçerli ve aynı zaman aralığı. Zamanı bilinmeyen aynı metinli iki final
 *    korunur (şarkı nakaratı gibi gerçek tekrarlar kaybolmasın). Sınırlama: zaman
 *    bilgisi taşımayan bir protokol tekrarı olursa metin çift görünebilir.
 *  - Son finalle aynı metinli ara metin: zamanları geçerli ve aynıysa yankı sayılır.
 *    Değilse yeni söyleyiş olabilir → bekleyen ara metin (canlı gösterilir); final
 *    gelirse ikinci söyleyiş korunur. Akış bu ara metin kesinleşmeden biterse
 *    ara metin kesin metin olarak EKLENMEZ, kesinleşmiş metin korunur ve parça
 *    [unconfirmedRepeat] ile "belirsiz" işaretlenir → notta ve ekranda uyarı olur,
 *    kullanıcı isterse o bölümü yeniden döker. (Wit'in finalden sonra aynı metinli
 *    yankı ara metni gönderip göndermediği belgede doğrulanamadı; bu yüzden ne kör
 *    yeniden deneme yapılır ne de "söz kayboldu" denir.)
 *
 * Akış sonu doğrulaması ([finish]): hiç olay yok → [WIT_ERR_EMPTY]; olay var ama
 * tanınan transkripsiyon yok → [WIT_ERR_SCHEMA]; transkripsiyon var ama final yok →
 * [WIT_ERR_NO_FINAL]. Metni boş bir FINAL ise geçerli "konuşma yok" yanıtıdır.
 */
internal class WitStreamState {
    val finals = mutableListOf<WitEvent>()
    private var pending: WitEvent? = null
    private var pendingRepeat = false

    var events = 0; private set
    /** Tanınmayan JSON nesneleri (üst veri vb.). */
    var unknownEvents = 0; private set
    var transcriptionEvents = 0; private set
    var finalsIn = 0; private set
    var dupDropped = 0; private set
    var echoIgnored = 0; private set
    var unconfirmedRepeat = 0; private set

    /** Canlı gösterim için bekleyen ara metin. */
    val partialText: String? get() = pending?.text

    fun onEvent(ev: WitEvent) {
        events++
        if (!ev.recognized) { unknownEvents++; return }
        transcriptionEvents++
        val last = finals.lastOrNull()
        if (ev.isFinal) {
            finalsIn++
            pending = null
            pendingRepeat = false
            if (ev.text.isEmpty()) return
            val dup = last != null && last.text == ev.text && last.hasTimes && ev.hasTimes &&
                last.start == ev.start && last.end == ev.end
            if (dup) dupDropped++ else finals += ev
            return
        }
        if (ev.text.isEmpty()) return
        if (last != null && ev.text == last.text) {
            val echo = last.hasTimes && ev.hasTimes && last.start == ev.start && last.end == ev.end
            if (echo) { echoIgnored++; return }
            pending = ev
            pendingRepeat = true
            return
        }
        pending = ev
        pendingRepeat = false
    }

    /** Akış sonu doğrulaması (bkz. sınıf açıklaması). */
    fun finish() {
        if (events == 0) throw IOException(WIT_ERR_EMPTY)
        if (transcriptionEvents == 0) throw IOException(WIT_ERR_SCHEMA)
        if (pending != null && !pendingRepeat) throw IOException(WIT_ERR_TRUNCATED)
        if (finalsIn == 0 && pending == null) throw IOException(WIT_ERR_NO_FINAL)
        if (pending != null) {
            if (!pendingRepeat) throw IOException(WIT_ERR_TRUNCATED)
            unconfirmedRepeat++
            pending = null
        }
    }
}

/** Zamanlanmış çıktı: [reliable] = Wit belirteç zamanları kullanıldı (tahmin değil). */
internal class TimedFinals(val segments: List<Segment>, val reliable: Boolean)

/**
 * Zaman damgaları. Bütün finallerin belirteç zamanları geçerliyse (0 ≤ başlangıç
 * < bitiş, başlangıç < parça süresi, bitiş ≤ süre + tolerans, sıralı) onlar
 * kullanılır. Değilse parça süresi metin uzunluğuyla orantılı dağıtılır ve
 * segmentler [Segment.approx] olarak işaretlenir (gerçek hizalama değildir).
 */
internal fun witTimed(finals: List<WitEvent>, offsetMs: Long, durMs: Long): TimedFinals {
    if (finals.isEmpty()) return TimedFinals(emptyList(), true)
    val tol = 500L
    var prevEnd = 0L
    val valid = finals.all { f ->
        val ok = f.start >= 0 && f.start < f.end && f.start < durMs && f.end <= durMs + tol && f.start + tol >= prevEnd
        if (ok) prevEnd = f.end
        ok
    }
    if (valid) {
        return TimedFinals(finals.map { Segment(offsetMs + it.start, offsetMs + it.end.coerceAtMost(durMs), it.text) }, true)
    }
    val out = mutableListOf<Segment>()
    val total = finals.sumOf { it.text.length.coerceAtLeast(1) }.toDouble()
    var acc = 0.0
    for (f in finals) {
        val s = offsetMs + (durMs * acc / total).toLong()
        acc += f.text.length.coerceAtLeast(1)
        val e = offsetMs + (durMs * acc / total).toLong()
        out += Segment(s, maxOf(e, s + 1), f.text, approx = true)
    }
    return TimedFinals(out, false)
}

/**
 * Aralıktaki en yüksek kısa pencere (varsayılan 200 ms) RMS düzeyi. Sessizlik kararı
 * bununla verilir: uzun parçada kısa, kısık konuşma ortalamada kaybolmasın.
 */
internal fun peakWindowRms(samples: FloatArray, from: Int, to: Int, window: Int = 3_200): Double {
    if (to <= from) return 0.0
    var peak = 0.0
    var p = from
    while (p < to) {
        val e = minOf(p + window, to)
        peak = maxOf(peak, rms(samples, p, e))
        p = e
    }
    return peak
}

/** Aralığın RMS düzeyi (0..1). */
internal fun rms(samples: FloatArray, from: Int, to: Int): Double {
    if (to <= from) return 0.0
    var e = 0.0
    for (k in from until to) e += samples[k].toDouble() * samples[k]
    return kotlin.math.sqrt(e / (to - from))
}

/**
 * Art arda gelen JSON nesnelerini (araya \r\n girebilir, bir nesne birden çok
 * pakete bölünebilir) süslü parantez derinliğiyle ayırır; dizgi içindeki
 * parantez ve kaçış karakterlerini dikkate alır. Tek nesne [maxChars] sınırını
 * aşarsa ya da akış yarım nesneyle biterse hata verir.
 */
internal class JsonStreamSplitter(private val maxChars: Int = 1_048_576) {
    private val buf = StringBuilder()
    private var depth = 0
    private var inString = false
    private var escape = false

    fun feed(chunk: String): List<String> {
        val out = mutableListOf<String>()
        for (c in chunk) {
            if (depth == 0 && c != '{') {
                if (!c.isWhitespace() && c != ',' && c != '[' && c != ']' && c != '﻿') throw IOException(WIT_ERR_INVALID)
                continue // nesneler arası boşluk/satır sonu
            }
            if (buf.length >= maxChars) throw IOException(WIT_ERR_INVALID)
            buf.append(c)
            if (inString) {
                when {
                    escape -> escape = false
                    c == '\\' -> escape = true
                    c == '"' -> inString = false
                }
                continue
            }
            when (c) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        out += buf.toString()
                        buf.setLength(0)
                    }
                }
            }
        }
        return out
    }

    /** Akış sonu: yarım kalmış nesne varsa hata. */
    fun finish() {
        if (depth != 0 || inString || buf.isNotEmpty()) throw IOException(WIT_ERR_TRUNCATED)
    }
}
