package com.aitolian.sesyazibench.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SentencesTest {

    @Test fun oneSegmentTwoSentencesSharesTimeForTranslationButIsOneReadingUnit() {
        val seg = listOf(Segment(0, 10_000, "Birinci cümle. İkinci cümle."))
        val sentences = Sentences.split(seg)
        assertEquals(2, sentences.size) // çeviri birimleri bozulmadı
        assertEquals(sentences[0].startMs, sentences[1].startMs)
        val units = Sentences.readingUnits(seg)
        assertEquals(1, units.size)     // vurgu: tek birim
        assertEquals("Birinci cümle. İkinci cümle.", units[0].text)
        // Konum 0'da vurgulanan birim ikinci cümle tek başına DEĞİL
        assertEquals(0, Sentences.activeIndex(units, 0))
    }

    @Test fun sentenceSpanningSegmentsMergesWithSameStartSibling() {
        val segs = listOf(Segment(0, 3000, "A. B"), Segment(3000, 6000, "C."))
        val units = Sentences.readingUnits(segs)
        assertEquals(1, units.size)
        assertEquals(6000L, units[0].endMs)
    }

    @Test fun distinctSegmentsStayDistinct() {
        val segs = listOf(Segment(0, 2000, "Bir."), Segment(2500, 4000, "İki."))
        val units = Sentences.readingUnits(segs)
        assertEquals(2, units.size)
        assertEquals(1, Sentences.activeIndex(units, 3000))
        assertNull(Sentences.activeIndex(units, 9000)) // uzun sessizlikte vurgu yok
    }

    @Test fun approxFlagSurvives() {
        val units = Sentences.readingUnits(listOf(Segment(0, 1000, "tahmini", approx = true)))
        assertEquals(true, units[0].approx)
    }
}
