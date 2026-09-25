package com.aitolian.sesyazibench.data

import android.content.Context
import android.util.AtomicFile
import com.aitolian.sesyazibench.engine.Segment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

data class Transcript(
    val id: Long,
    val fileName: String,
    val durationMs: Long,
    val language: String,
    /** Gösterilen metni üreten son STT turunun süresi (ms). */
    val processMs: Long,
    val segments: List<Segment>,
    /** Kullanıcının not ekranında düzelttiği metin (varsa ham dökümün yerine gösterilir). */
    val editedText: String? = null,
    /** Her kullanıcı düzenlemesinde artar; arka plan işleri eski sürümün üzerine yazmasın diye. */
    val revision: Int = 0,
    /** En iyi ile iyileştirildiyse önceki (ön izleme) turunun STT süresi (ms); yoksa 0. */
    val previewMs: Long = 0,
    /** Metni üreten kalite (Quality.name); eski kayıtlarda boş. */
    val quality: String? = null,
    /** Doğrulanamayan bölümler (ör. kesinleşmeyen tekrar); kullanıcıya gösterilir. */
    val warnings: List<RangeWarning> = emptyList(),
) {
    val rawText: String get() = segments.joinToString(" ") { it.text }
    val text: String get() = editedText ?: rawText
    val preview: String get() = text.take(40).let { if (text.length > 40) "$it…" else it }

    /** Zamanlı altyazı; her zaman orijinal (zaman damgalı) parçalardan üretilir. */
    fun toSrt(): String = buildString {
        segments.forEachIndexed { i, s ->
            append(i + 1).append('\n')
            append(srtTime(s.startMs)).append(" --> ").append(srtTime(s.endMs)).append('\n')
            append(s.text).append("\n\n")
        }
    }

    companion object {
        fun clock(ms: Long): String = "%d:%02d".format(Locale.US, ms / 60_000, (ms / 1000) % 60)
        private fun srtTime(ms: Long) = "%02d:%02d:%02d,%03d".format(
            Locale.US, ms / 3_600_000, (ms / 60_000) % 60, (ms / 1000) % 60, ms % 1000,
        )
    }
}

/** Notun bir bölümü tam doğrulanamadı. [reason]: sabit kod (ör. [UNCONFIRMED_REPEAT]). */
data class RangeWarning(val fromMs: Long, val toMs: Long, val reason: String) {
    companion object {
        /** Aynı satır yeniden başlamış görünüyor ama motor kesinleştirmedi. */
        const val UNCONFIRMED_REPEAT = "UNCONFIRMED_REPEAT"
    }
}

/**
 * Notlar — cihazda JSON dosyası. Bütün okuma-değiştirme-yazma işlemleri tek
 * kilit altında ve bellekteki kopya üzerinden yapılır; disk yazımı AtomicFile
 * ile atomiktir (yarıda kesilen yazma eski dosyayı bozmaz). Dosya bozuksa
 * sessizce silinmez, yedeklenir.
 */
object HistoryStore {
    const val MAX = 50
    private val mutex = Mutex()
    private var cache: List<Transcript>? = null

    private fun file(c: Context) = AtomicFile(File(c.filesDir, "history.json"))

    suspend fun load(c: Context): List<Transcript> = mutex.withLock { current(c) }

    /** Yeni kayıt ekler ya da aynı id'liyi değiştirir (en üste taşır). */
    suspend fun add(c: Context, t: Transcript): List<Transcript> = mutate(c) { list ->
        (listOf(t) + list.filter { it.id != t.id }).take(MAX)
    }

    /**
     * Var olan kaydı dönüştürür. Kayıt silinmişse hiçbir şey yapmaz ve null döner
     * (silinen not arka plan işiyle geri gelmez).
     */
    suspend fun update(c: Context, id: Long, change: (Transcript) -> Transcript): Pair<List<Transcript>, Transcript>? =
        mutex.withLock {
            val list = current(c)
            val old = list.firstOrNull { it.id == id } ?: return@withLock null
            val new = change(old)
            val next = list.map { if (it.id == id) new else it }
            write(c, next)
            next to new
        }

    suspend fun remove(c: Context, id: Long): List<Transcript> = mutate(c) { list -> list.filter { it.id != id } }

    /** "Geri al": silinen kaydı zaman sırasındaki yerine geri koyar. */
    suspend fun restore(c: Context, t: Transcript): List<Transcript> = mutate(c) { list ->
        (list.filter { it.id != t.id } + t).sortedByDescending { it.id }.take(MAX)
    }

    suspend fun clear(c: Context) = mutex.withLock {
        withContext(Dispatchers.IO) {
            file(c).delete()
            File(c.filesDir, "history.json.new").delete()
            File(c.filesDir, "history.json.bak").delete()
            File(c.filesDir, "history.json.tmp").delete() // v1.7-1.8 geçici dosyası
            // Bozuk dosya yedekleri de kullanıcının notlarıdır: "tümünü sil" bunları da siler
            c.filesDir.listFiles()
                ?.filter { it.name.startsWith("history.corrupt-") && it.name.endsWith(".json") }
                ?.forEach { it.delete() }
        }
        cache = emptyList()
    }

    private suspend fun mutate(c: Context, change: (List<Transcript>) -> List<Transcript>): List<Transcript> =
        mutex.withLock {
            val next = change(current(c))
            write(c, next)
            next
        }

    private suspend fun current(c: Context): List<Transcript> =
        cache ?: withContext(Dispatchers.IO) { read(c) }.also { cache = it }

    /**
     * Disk + bellek birlikte ve iptal edilemez şekilde güncellenir: yazım başladıysa
     * tamamlanır; dosya yeni / bellek eski kalıp sonraki yazımın kayıt düşürmesi olmaz.
     */
    private suspend fun write(c: Context, list: List<Transcript>) {
        withContext(NonCancellable + Dispatchers.IO) {
            val af = file(c)
            val out = af.startWrite()
            try {
                out.write(encode(list).toByteArray(Charsets.UTF_8))
                af.finishWrite(out)
                cache = list
            } catch (t: Throwable) {
                af.failWrite(out)
                throw t
            }
        }
    }

    private fun read(c: Context): List<Transcript> {
        val af = file(c)
        if (!af.baseFile.exists()) return emptyList()
        return try {
            decode(String(af.readFully(), Charsets.UTF_8))
        } catch (t: Throwable) {
            // Bozuk dosyayı silme: yedekle, boş listeyle devam et
            runCatching { af.baseFile.renameTo(File(c.filesDir, "history.corrupt-${System.currentTimeMillis()}.json")) }
            emptyList()
        }
    }

    private fun encode(list: List<Transcript>): String {
        val arr = JSONArray()
        list.forEach { tr ->
            val segs = JSONArray()
            tr.segments.forEach { s ->
                segs.put(JSONObject().put("s", s.startMs).put("e", s.endMs).put("t", s.text).apply { if (s.approx) put("a", true) })
            }
            arr.put(
                JSONObject().put("id", tr.id).put("fileName", tr.fileName).put("durationMs", tr.durationMs)
                    .put("language", tr.language).put("processMs", tr.processMs).put("segments", segs)
                    .put("rev", tr.revision).put("pms", tr.previewMs)
                    .apply { tr.quality?.let { put("q", it) } }
                    .apply {
                        if (tr.warnings.isNotEmpty()) put("w", JSONArray().apply {
                            tr.warnings.forEach { w -> put(JSONObject().put("s", w.fromMs).put("e", w.toMs).put("r", w.reason)) }
                        })
                    }
                    .apply { tr.editedText?.let { put("edited", it) } },
            )
        }
        return arr.toString()
    }

    private fun decode(json: String): List<Transcript> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val segs = o.getJSONArray("segments")
            Transcript(
                id = o.getLong("id"),
                fileName = o.getString("fileName"),
                durationMs = o.getLong("durationMs"),
                language = o.getString("language"),
                processMs = o.getLong("processMs"),
                segments = (0 until segs.length()).map { j ->
                    val s = segs.getJSONObject(j)
                    Segment(s.getLong("s"), s.getLong("e"), s.getString("t"), approx = s.optBoolean("a", false))
                },
                editedText = o.optString("edited").ifEmpty { null },
                revision = o.optInt("rev", 0),
                previewMs = o.optLong("pms", 0),
                quality = o.optString("q").ifEmpty { null },
                warnings = o.optJSONArray("w")?.let { arr ->
                    (0 until arr.length()).mapNotNull { j ->
                        arr.optJSONObject(j)?.let { w -> RangeWarning(w.optLong("s"), w.optLong("e"), w.optString("r")) }
                    }
                } ?: emptyList(),
            )
        }
    }
}
