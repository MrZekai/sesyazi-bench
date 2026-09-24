package com.aitolian.sesyazibench.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * TXT / SRT dışa aktarma. Dosyalar önbellekteki "exports" klasörüne yazılır;
 * her yeni dışa aktarmada eskiler silinir (paylaşılmış kopyalar diskte birikmez).
 * Dosya adı nottan türetilir, güvenli karakterlere indirgenir ve nota özgüdür.
 */
object Exports {
    fun dir(c: Context) = File(c.cacheDir, "exports")

    fun clear(c: Context) { dir(c).deleteRecursively() }

    /** Düz metin (.txt) — ekranda görünen metin: düzenlenmiş ya da çeviri. */
    fun shareTxt(context: Context, t: Transcript, text: String, suffix: String) =
        share(context, fileFor(context, t, suffix, "txt"), text, "text/plain", "Metin dosyasını paylaş")

    /**
     * Altyazı (.srt) — zaman damgalı parçalardan. Serbest düzenlenmiş metne
     * uydurma zaman damgası eklenmez; düzenlenmiş notta SRT orijinal dökümdür
     * ve dosya adında "_orijinal" yazar.
     */
    fun shareSrt(context: Context, t: Transcript, srt: String, suffix: String) =
        share(context, fileFor(context, t, suffix, "srt"), srt, "application/x-subrip", "Altyazı dosyasını paylaş")

    private fun fileFor(c: Context, t: Transcript, suffix: String, ext: String): File {
        val d = dir(c)
        d.deleteRecursively()
        d.mkdirs()
        val safe = t.fileName.substringBeforeLast('.')
            .replace(Regex("[^\\p{L}\\p{N}._ -]"), "_").trim().trim('.').take(48)
            .ifBlank { "sesyazi" }
        return File(d, "${safe}_${t.id % 100_000}$suffix.$ext")
    }

    private fun share(context: Context, f: File, content: String, mime: String, title: String) {
        f.writeText(content)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", f)
        val i = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, f.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(i, title))
    }
}
