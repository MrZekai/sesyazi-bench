package com.aitolian.sesyazibench.audio

import java.io.File
import java.io.InputStream
import java.util.concurrent.CancellationException

/**
 * İçe aktarılan sesin önbelleğe kopyalanması — saf Kotlin, birim testli.
 *
 * Sahiplik kuralı: her iş YALNIZ kendi [out] dosyasına yazar ve hata/iptalde yalnız
 * onu siler. Başka dosyaya dokunmaz. [alive] engelleyici açılıştan önce, sonra ve
 * her okumada kontrol edilir: sağlayıcı geç dönse bile iptal edilmiş iş yazmaz.
 *
 * Dönüş: kopyalanan dosya; [limitBytes] aşılırsa null (dosya silinir).
 * Boş dosyada [EmptyImportException].
 */
object ImportCopy {
    class EmptyImportException : Exception("Dosya boş")

    fun copy(open: () -> InputStream?, out: File, limitBytes: Long, alive: () -> Boolean): File? {
        fun check() { if (!alive()) throw CancellationException("iptal") }
        try {
            check()
            open().use { input ->
                requireNotNull(input) { "Dosya okunamadı" }
                check() // engelleyici açılıştan SONRA tekrar
                out.outputStream().use { o ->
                    val buf = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        check()
                        val n = input.read(buf)
                        if (n < 0) break
                        total += n
                        if (total > limitBytes) { o.close(); out.delete(); return null }
                        o.write(buf, 0, n)
                    }
                }
            }
            check()
            if (out.length() == 0L) throw EmptyImportException()
            return out
        } catch (t: Throwable) {
            out.delete()
            throw t
        }
    }
}
