package com.aitolian.sesyazibench.engine

import com.aitolian.sesyazibench.audio.AUDIO_RATE
import com.aitolian.sesyazibench.audio.DecodedAudio
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * WitEngine uçtan uca (ağ yerine sahte HTTP yanıtları). Gerçek Wit/anahtar kullanılmaz.
 * Ses: 70 sn; 40. sn'de sessiz boşluk → 2 parça (≈40 sn + ≈30 sn), parça gövde boyutuyla ayırt edilir.
 */
class WitEngineTest {

    private class Resp(val code: Int, val body: String = "", val retryAfter: String? = null)

    private class FakeConn(private val pick: (ByteArray) -> Resp) : HttpURLConnection(URL("https://example.invalid/")) {
        private val out = ByteArrayOutputStream()
        private var resp: Resp? = null
        private fun r() = resp ?: pick(out.toByteArray()).also { resp = it }
        override fun getOutputStream(): OutputStream = out
        override fun getResponseCode(): Int = r().code
        override fun getInputStream(): InputStream = ByteArrayInputStream(r().body.toByteArray(Charsets.UTF_8))
        override fun getErrorStream(): InputStream? = null
        override fun getHeaderField(name: String?): String? = if (name == "Retry-After") r().retryAfter else null
        override fun disconnect() {}
        override fun usingProxy() = false
        override fun connect() {}
    }

    private val loud = 0.3f

    private fun audio(silentSecondPart: Boolean = false, quietWordInSecondPart: Boolean = false): DecodedAudio {
        val n = 70 * AUDIO_RATE
        val gapFrom = 40 * AUDIO_RATE
        val gapTo = gapFrom + AUDIO_RATE / 4
        val s = FloatArray(n) { i ->
            when {
                i in gapFrom until gapTo -> 0f
                quietWordInSecondPart && i >= gapTo -> if (i in 60 * AUDIO_RATE until 61 * AUDIO_RATE)
                    (0.02 * kotlin.math.sin(2 * Math.PI * 200 * i / AUDIO_RATE)).toFloat() else 0f
                silentSecondPart && i >= gapTo -> 0f
                else -> (loud * kotlin.math.sin(2 * Math.PI * 300 * i / AUDIO_RATE)).toFloat()
            }
        }
        return DecodedAudio(s, "audio/test", AUDIO_RATE, 1)
    }

    /** İlk parça (büyük gövde) için [first], ikinci için [second]; her parçanın deneme sayısı sayılır. */
    private fun engine(first: (Int) -> Resp, second: (Int) -> Resp, tries: ConcurrentHashMap<Int, AtomicInteger>): WitEngine {
        val threshold = 35 * AUDIO_RATE * 2
        return WitEngine("test-token") {
            FakeConn { body ->
                val idx = if (body.size > threshold) 0 else 1
                val k = tries.getOrPut(idx) { AtomicInteger() }.incrementAndGet()
                if (idx == 0) first(k) else second(k)
            }
        }
    }

    private val okText = """{"text":"merhaba","is_final":false}
{"text":"merhaba dünya","is_final":true,"speech":{"tokens":[{"start":100,"end":900}]}}"""

    private fun run(e: WitEngine, a: DecodedAudio, cancel: AtomicBoolean = AtomicBoolean(false)): WitEngine.Outcome {
        WitEngine.resetSchedulerForTests()
        return runBlocking { e.transcribe(a, "tr", cancel) }
    }

    @Test fun emptySecondBodyIsNotSilentSuccess() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(200, okText) }, { Resp(200, "  \r\n ") }, tries), audio())
        assertNull(oc.result.error)                       // ilk parça başarılı: kayıt tamamen başarısız değil
        assertEquals(1, oc.failed.size)                   // ama tam başarı da değil
        assertEquals(WitEngine.ERR_EMPTY, oc.failed[0].code)
        assertEquals(3, tries[1]!!.get())                 // sınırlı yeniden deneme
        assertEquals(1, tries[0]!!.get())                 // başarılı parça yeniden çalıştırılmadı
        assertEquals("merhaba dünya", oc.result.segments.single().text)
        assertEquals(listOf(WIT_ERR_EMPTY, WIT_ERR_EMPTY, WIT_ERR_EMPTY), oc.diag[1].codes)
    }

    @Test fun emptyBodyOnReallySilentChunkIsSilence() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(200, okText) }, { Resp(200, "") }, tries), audio(silentSecondPart = true))
        assertTrue(oc.failed.isEmpty())
        assertEquals(1, tries[1]!!.get())                 // sessizlikte boşuna tekrar yok
        assertEquals(listOf("OK_SILENT"), oc.diag[1].codes)
    }

    @Test fun emptyBodyOnChunkWithShortQuietSpeechIsNotSilence() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(200, okText) }, { Resp(200, "") }, tries), audio(quietWordInSecondPart = true))
        assertEquals(WitEngine.ERR_EMPTY, oc.failed.single().code)
        assertEquals(3, tries[1]!!.get())
    }

    @Test fun validNoTextResponseIsNotRetried() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(200, okText) }, { Resp(200, """{"text":"","is_final":true}""") }, tries), audio())
        assertTrue(oc.failed.isEmpty())
        assertEquals(1, tries[1]!!.get())
    }

    @Test fun brokenJsonFailsChunkAfterRetries() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(200, okText) }, { Resp(200, """{"text":"yar""") }, tries), audio())
        assertEquals(WitEngine.ERR_TRUNCATED, oc.failed.single().code)
        assertEquals(3, tries[1]!!.get())
    }

    @Test fun partialThenDisconnectIsNotSuccess() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(200, okText) }, { Resp(200, """{"text":"yarım kaldı","is_final":false}""") }, tries), audio())
        assertEquals(WitEngine.ERR_TRUNCATED, oc.failed.single().code)
    }

    @Test fun unauthorizedStopsWithoutRetry() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(401) }, { Resp(401) }, tries), audio())
        assertTrue(oc.authFailed)
        assertEquals(WitEngine.ERR_AUTH, oc.result.error)
        assertTrue(tries.values.all { it.get() == 1 })
    }

    @Test fun rateLimitWaitsThenSucceeds() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(
            engine({ Resp(200, okText) }, { k -> if (k == 1) Resp(429, retryAfter = "1") else Resp(200, okText) }, tries),
            audio(),
        )
        assertTrue(oc.failed.isEmpty())
        assertEquals(listOf(WIT_ERR_RATE, "OK"), oc.diag[1].codes)
        assertEquals(2, oc.result.segments.size)
    }

    @Test fun cancelledRunReportsCancelAndNoFailedChunks() {
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(200, okText) }, { Resp(200, okText) }, tries), audio(), AtomicBoolean(true))
        assertEquals(ERR_CANCELLED, oc.result.error)
        assertTrue(oc.failed.isEmpty())
        assertTrue(tries.isEmpty())                       // iptal edilmiş oturum istek göndermedi
    }

    @Test fun repeatedChorusAcrossFinalsIsKeptInResult() {
        val chorus = """{"text":"nakarat","is_final":true}
{"text":"nakarat","is_final":false}
{"text":"nakarat","is_final":true}"""
        val tries = ConcurrentHashMap<Int, AtomicInteger>()
        val oc = run(engine({ Resp(200, chorus) }, { Resp(200, """{"text":"","is_final":true}""") }, tries), audio())
        assertEquals(listOf("nakarat", "nakarat"), oc.result.segments.map { it.text })
        assertTrue(oc.result.segments.all { it.approx })  // zaman yok → yaklaşık olarak işaretli
        assertEquals(false, oc.diag[0].reliableTimes)
    }
}
