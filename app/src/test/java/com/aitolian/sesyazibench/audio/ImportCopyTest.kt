package com.aitolian.sesyazibench.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ImportCopyTest {

    /**
     * QA-02 regresyonu: A'nın sağlayıcı açılışı bekler; A iptal edilir, B kopyalanır;
     * sonra A'nın sağlayıcısı döner. B'nin dosyası bozulmamalı, A'nın dosyası kalmamalı.
     */
    @Test fun cancelledSlowImportNeverTouchesNewFile() {
        val dir = java.nio.file.Files.createTempDirectory("imp").toFile()
        val aOut = File(dir, "current_audio_1.opus")
        val bOut = File(dir, "current_audio_2.opus")
        val aAlive = AtomicBoolean(true)
        val release = CountDownLatch(1)
        var aError: Throwable? = null
        val a = Thread {
            try {
                ImportCopy.copy({ release.await(5, TimeUnit.SECONDS); ByteArrayInputStream(ByteArray(64) { 7 }) }, aOut, 1_000_000) { aAlive.get() }
            } catch (t: Throwable) { aError = t }
        }
        a.start()
        Thread.sleep(50)
        aAlive.set(false) // yeni paylaşım A'yı iptal eder
        val bData = ByteArray(128) { it.toByte() }
        ImportCopy.copy({ ByteArrayInputStream(bData) }, bOut, 1_000_000) { true }
        release.countDown() // A'nın sağlayıcısı şimdi döner
        a.join(5_000)
        assertTrue(aError is CancellationException)
        assertArrayEquals(bData, bOut.readBytes())
        assertFalse(aOut.exists())
        dir.deleteRecursively()
    }

    @Test fun overLimitReturnsNullAndDeletesOwnFile() {
        val dir = java.nio.file.Files.createTempDirectory("imp").toFile()
        val out = File(dir, "x.bin")
        val r = ImportCopy.copy({ ByteArrayInputStream(ByteArray(2_000)) }, out, 1_000) { true }
        assertTrue(r == null)
        assertFalse(out.exists())
        dir.deleteRecursively()
    }

    @Test fun emptyFileIsRejected() {
        val dir = java.nio.file.Files.createTempDirectory("imp").toFile()
        val out = File(dir, "x.bin")
        var err: Throwable? = null
        try { ImportCopy.copy({ ByteArrayInputStream(ByteArray(0)) }, out, 1_000) { true } } catch (t: Throwable) { err = t }
        assertTrue(err is ImportCopy.EmptyImportException)
        assertFalse(out.exists())
        dir.deleteRecursively()
    }
}
