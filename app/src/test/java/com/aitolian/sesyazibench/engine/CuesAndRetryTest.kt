package com.aitolian.sesyazibench.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CuesAndRetryTest {

    @Test fun translatedSentencesFromOneSegmentBecomeOneCue() {
        val sentences = Sentences.split(listOf(Segment(0, 10_000, "Birinci cümle. İkinci cümle.")))
        assertEquals(2, sentences.size)
        val cues = Sentences.mergeOverlappingCues(sentences)
        assertEquals(1, cues.size)
        assertEquals(0L, cues[0].startMs)
        assertEquals(10_000L, cues[0].endMs)
        assertEquals("Birinci cümle. İkinci cümle.", cues[0].text)
    }

    @Test fun adjacentCuesStaySeparate() {
        val cues = Sentences.mergeOverlappingCues(listOf(Segment(0, 2000, "A."), Segment(2000, 4000, "B.")))
        assertEquals(2, cues.size)
    }

    @Test fun retryAfterSecondsAndHttpDate() {
        assertEquals(120_000L, parseRetryAfterMs("120", 0))
        assertEquals(0L, parseRetryAfterMs("0", 0))
        assertNull(parseRetryAfterMs(null, 0))
        assertNull(parseRetryAfterMs("yarın", 0))
        // 1 Ocak 2026 00:00:30 GMT, şimdi 00:00:00 → 30 sn
        val now = 1_767_225_600_000L
        assertEquals(30_000L, parseRetryAfterMs("Thu, 01 Jan 2026 00:00:30 GMT", now))
        // Geçmiş tarih → 0
        assertTrue(parseRetryAfterMs("Thu, 01 Jan 2026 00:00:00 GMT", now + 5_000)!! == 0L)
    }
}
