package com.aitolian.sesyazibench.engine

/** Cümle bölme ve okuma vurgusu birimleri. Android bağımlılığı yok (birim testlerde kullanılır). */
object Sentences {

    /**
     * Altyazı için: zamanı örtüşen parçaları tek cue'da birleştirir (aynı segmentten
     * gelen iki cümle çevirisi aynı aralığı taşır). Yeni kesin zaman uydurulmaz.
     */
    fun mergeOverlappingCues(items: List<Segment>): List<Segment> {
        val out = mutableListOf<Segment>()
        for (s in items.sortedBy { it.startMs }) {
            val p = out.lastOrNull()
            if (p != null && s.startMs < p.endMs) {
                out[out.lastIndex] = p.copy(
                    endMs = maxOf(p.endMs, s.endMs), text = p.text + " " + s.text,
                    approx = p.approx || s.approx,
                )
            } else out += s
        }
        return out
    }

    private val SPLIT = Regex("(?<=[.!?…。؟])\\s+")
    private const val ENDERS = ".!?…。؟"

    /**
     * Parçaları cümlelere dönüştürür (çeviri birimleri). Her cümle, ilk karakterinin
     * parçasının başlangıcını ve son karakterinin parçasının bitişini alır. Bir
     * parçanın içindeki birden çok cümle AYNI zamanı paylaşır — bu zaman cümle
     * düzeyinde hassas değildir (bkz. [readingUnits]).
     */
    fun split(segments: List<Segment>): List<Segment> {
        val out = mutableListOf<Segment>()
        val buf = StringBuilder()
        var start = -1L
        var end = 0L
        var approx = false
        for (seg in segments) {
            val pieces = seg.text.split(SPLIT)
            pieces.forEachIndexed { i, piece ->
                if (piece.isBlank()) return@forEachIndexed
                if (start < 0) start = seg.startMs
                if (buf.isNotEmpty()) buf.append(' ')
                buf.append(piece.trim())
                end = seg.endMs
                approx = approx || seg.approx
                val closesSentence = piece.trimEnd().lastOrNull()?.let { it in ENDERS } == true
                val isLastPiece = i == pieces.lastIndex
                if (closesSentence || (!isLastPiece)) {
                    out += Segment(start, end, buf.toString(), approx)
                    buf.clear(); start = -1; approx = false
                }
            }
        }
        if (buf.isNotEmpty()) out += Segment(start.coerceAtLeast(0), end, buf.toString(), approx)
        return out
    }

    /**
     * Okuma vurgusu birimleri: cümleler; ama aynı BAŞLANGIÇ zamanını paylaşan ardışık
     * cümleler (aynı parçadan bölünmüş) TEK birim olur. Böylece kesin cümle zamanı
     * yokken ikinci cümle, parça başında yanlışlıkla tek başına vurgulanmaz.
     */
    fun readingUnits(segments: List<Segment>): List<Segment> {
        val out = mutableListOf<Segment>()
        for (s in split(segments)) {
            val last = out.lastOrNull()
            if (last != null && last.startMs == s.startMs) {
                out[out.lastIndex] = Segment(
                    last.startMs, maxOf(last.endMs, s.endMs), last.text + " " + s.text, last.approx || s.approx,
                )
            } else out += s
        }
        return out
    }

    /**
     * Konumdaki birim: başlangıcı konumu geçmemiş son öğe; sonundan 1,5 sn'den
     * fazla sonraysa (sessizlik) hiçbiri. [items] başlangıca göre sıralı olmalı.
     */
    fun activeIndex(items: List<Segment>, posMs: Long): Int? {
        var lo = 0
        var hi = items.lastIndex
        var found = -1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            if (items[mid].startMs <= posMs) { found = mid; lo = mid + 1 } else hi = mid - 1
        }
        if (found < 0) return null
        return found.takeIf { posMs <= items[found].endMs + 1_500 }
    }
}
