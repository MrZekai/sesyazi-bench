package com.aitolian.sesyazibench.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

/** Wit akış ayrıştırıcısı: sentetik JSON yanıtlarıyla (gerçek ses/anahtar yok). */
class WitStreamTest {

    private fun final(text: String, start: Long = -1, end: Long = -1) = WitEvent(text, true, start, end)
    private fun partial(text: String, start: Long = -1, end: Long = -1) = WitEvent(text, false, start, end)

    private fun expectCode(code: String, block: () -> Unit) {
        try {
            block()
            fail("$code bekleniyordu")
        } catch (e: IOException) {
            assertEquals(code, e.message)
        }
    }

    @Test fun timelessIdenticalFinalsAreBothKept() {
        val st = WitStreamState()
        st.onEvent(final("seni seviyorum"))
        st.onEvent(final("seni seviyorum"))
        st.finish()
        assertEquals(2, st.finals.size)
        assertEquals(0, st.dupDropped)
    }

    @Test fun sameTextAtDifferentTimesIsKept() {
        val st = WitStreamState()
        st.onEvent(final("nakarat", 0, 2000))
        st.onEvent(final("nakarat", 5000, 7000))
        st.finish()
        assertEquals(2, st.finals.size)
    }

    @Test fun reliableDuplicateIsDroppedOnce() {
        val st = WitStreamState()
        st.onEvent(final("merhaba", 100, 900))
        st.onEvent(final("merhaba", 100, 900))
        st.finish()
        assertEquals(1, st.finals.size)
        assertEquals(1, st.dupDropped)
        assertEquals(2, st.finalsIn)
    }

    @Test fun oneSideWithoutTimesIsNotProofOfDuplicate() {
        val st = WitStreamState()
        st.onEvent(final("merhaba", 100, 900))
        st.onEvent(final("merhaba"))
        st.finish()
        assertEquals(2, st.finals.size)
    }

    @Test fun repeatedLinePartialWithoutFinalIsCountedNotCompletedNorDropped() {
        val st = WitStreamState()
        st.onEvent(final("la la la"))
        st.onEvent(partial("la la la"))
        assertEquals("la la la", st.partialText) // yeni söyleyiş olabilir: canlı gösterilir
        st.finish()
        assertEquals(1, st.finals.size)           // kesinleşmemiş metin final sayılmadı
        assertEquals(1, st.unconfirmedRepeat)     // ve teşhiste görünür
    }

    @Test fun repeatedLinePartialThenFinalIsKept() {
        val st = WitStreamState()
        st.onEvent(final("la la la"))
        st.onEvent(partial("la la la"))
        st.onEvent(final("la la la"))
        st.finish()
        assertEquals(2, st.finals.size)
    }

    @Test fun echoPartialWithSameValidTimesIsIgnored() {
        val st = WitStreamState()
        st.onEvent(final("tamam", 0, 500))
        st.onEvent(partial("tamam", 0, 500))
        st.finish()
        assertEquals(1, st.finals.size)
        assertEquals(1, st.echoIgnored)
        assertNull(st.partialText)
    }

    @Test fun pendingPartialIsTruncated() {
        val st = WitStreamState()
        st.onEvent(final("bir"))
        st.onEvent(partial("iki üç"))
        expectCode(WIT_ERR_TRUNCATED) { st.finish() }
    }

    @Test fun noEventsIsEmptyBodyNotSilence() {
        expectCode(WIT_ERR_EMPTY) { WitStreamState().finish() }
    }

    @Test fun unknownObjectOnlyIsSchemaErrorNotSuccess() {
        val st = WitStreamState()
        st.onEvent(parseWitEvent("{}"))
        expectCode(WIT_ERR_SCHEMA) { st.finish() }
        assertEquals(1, st.unknownEvents)
    }

    @Test fun metadataIsIgnoredButValidFinalStillCounts() {
        val st = WitStreamState()
        st.onEvent(parseWitEvent("""{"meta":{"id":"x"}}"""))
        st.onEvent(parseWitEvent("""{"text":"merhaba","is_final":true}"""))
        st.finish()
        assertEquals(1, st.finals.size)
        assertEquals(1, st.unknownEvents)
    }

    @Test fun transcriptionWithoutAnyFinalIsNoFinal() {
        val st = WitStreamState()
        st.onEvent(parseWitEvent("""{"text":"","is_final":false}"""))
        expectCode(WIT_ERR_NO_FINAL) { st.finish() }
    }

    @Test fun validNoTextResponseIsSuccessWithoutFinals() {
        val st = WitStreamState()
        st.onEvent(parseWitEvent("""{"text":"","is_final":true}"""))
        st.finish()
        assertTrue(st.finals.isEmpty())
    }

    @Test fun splitterHandlesChunkedObjectsAndSeparators() {
        val sp = JsonStreamSplitter()
        val a = sp.feed("""{"text":"a {b}","is_final":false}""" + "\r\n" + """{"te""")
        val b = sp.feed("""xt":"c\"d","is_final":true}""" + "\n")
        sp.finish()
        assertEquals(1, a.size)
        assertEquals(1, b.size)
        assertEquals("c\"d", parseWitEvent(b[0]).text)
        assertTrue(parseWitEvent(b[0]).isFinal)
    }

    @Test fun splitterRejectsGarbageAndHalfObject() {
        expectCode(WIT_ERR_INVALID) { JsonStreamSplitter().feed("<html>") }
        val sp = JsonStreamSplitter()
        sp.feed("""{"text":"yar""")
        expectCode(WIT_ERR_TRUNCATED) { sp.finish() }
    }

    @Test fun serviceErrorObjectIsError() {
        expectCode(WIT_ERR_SERVICE) { parseWitEvent("""{"error":"x","code":"y"}""") }
    }

    @Test fun tokenTimesAreParsed() {
        val ev = parseWitEvent(
            """{"text":"a b","is_final":true,"type":"FINAL_TRANSCRIPTION","speech":{"tokens":[{"start":120,"end":400,"token":"a"},{"start":450,"end":900,"token":"b"}]}}""",
        )
        assertEquals(120L, ev.start)
        assertEquals(900L, ev.end)
        assertTrue(ev.hasTimes)
    }

    @Test fun timedUsesTokensWhenValidOtherwiseMarksApprox() {
        val ok = witTimed(listOf(final("a", 0, 1000), final("b", 1200, 2000)), 10_000, 5000)
        assertTrue(ok.reliable)
        assertEquals(10_000L, ok.segments[0].startMs)
        assertFalse(ok.segments.any { it.approx })

        val approx = witTimed(listOf(final("aaaa"), final("bb")), 0, 6000)
        assertFalse(approx.reliable)
        assertTrue(approx.segments.all { it.approx })
        assertEquals(4000L, approx.segments[1].startMs)
        assertEquals(6000L, approx.segments[1].endMs)
    }

    @Test fun peakWindowFindsShortQuietSpeechInLongSilence() {
        val s = FloatArray(50 * 16000)
        for (i in 10 * 16000 until 11 * 16000) s[i] = (0.02 * kotlin.math.sin(2 * Math.PI * 200 * i / 16000)).toFloat()
        assertTrue(rms(s, 0, s.size) < 0.003)          // ortalama: "sessiz" görünürdü
        assertTrue(peakWindowRms(s, 0, s.size) > 0.01)  // tepe pencere: konuşma var
    }

    @Test fun rmsOfSilenceAndTone() {
        val z = FloatArray(1000)
        assertEquals(0.0, rms(z, 0, z.size), 1e-9)
        val t = FloatArray(16000) { (0.5 * kotlin.math.sin(2 * Math.PI * 440 * it / 16000)).toFloat() }
        assertEquals(0.5 / kotlin.math.sqrt(2.0), rms(t, 0, t.size), 0.01)
    }
}
