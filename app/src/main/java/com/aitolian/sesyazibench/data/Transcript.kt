package com.aitolian.sesyazibench.data

import android.content.Context
import com.aitolian.sesyazibench.engine.Segment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

data class Transcript(
    val id: Long,
    val fileName: String,
    val durationMs: Long,
    val language: String,
    val processMs: Long,
    val segments: List<Segment>,
) {
    val text: String get() = segments.joinToString(" ") { it.text }
    val preview: String get() = text.take(40).let { if (text.length > 40) "$it…" else it }

    fun toSrt(): String = buildString {
        segments.forEachIndexed { i, s ->
            append(i + 1).append('\n')
            append(srtTime(s.startMs)).append(" --> ").append(srtTime(s.endMs)).append('\n')
            append(s.text).append("\n\n")
        }
    }

    fun toTxt(): String = segments.joinToString("\n") { "[${clock(it.startMs)}] ${it.text}" }

    companion object {
        fun clock(ms: Long): String = "%d:%02d".format(Locale.US, ms / 60_000, (ms / 1000) % 60)
        private fun srtTime(ms: Long) = "%02d:%02d:%02d,%03d".format(
            Locale.US, ms / 3_600_000, (ms / 60_000) % 60, (ms / 1000) % 60, ms % 1000,
        )
    }
}

/** Son dökümler — cihazda JSON dosyası, en fazla 20 kayıt. */
object HistoryStore {
    private const val MAX = 20
    private fun file(c: Context) = File(c.filesDir, "history.json")

    fun load(c: Context): List<Transcript> = runCatching {
        val arr = JSONArray(file(c).readText())
        (0 until arr.length()).map { i ->
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
                    Segment(s.getLong("s"), s.getLong("e"), s.getString("t"))
                },
            )
        }
    }.getOrDefault(emptyList())

    fun remove(c: Context, id: Long): List<Transcript> = save(c, load(c).filter { it.id != id })

    fun clear(c: Context) { file(c).delete() }

    /** "Geri al": silinen kaydı zaman sırasındaki yerine geri koyar. */
    fun restore(c: Context, t: Transcript): List<Transcript> =
        save(c, (load(c).filter { it.id != t.id } + t).sortedByDescending { it.id }.take(MAX))

    fun add(c: Context, t: Transcript): List<Transcript> =
        save(c, (listOf(t) + load(c).filter { it.id != t.id }).take(MAX))

    private fun save(c: Context, list: List<Transcript>): List<Transcript> {
        val arr = JSONArray()
        list.forEach { tr ->
            val segs = JSONArray()
            tr.segments.forEach { s -> segs.put(JSONObject().put("s", s.startMs).put("e", s.endMs).put("t", s.text)) }
            arr.put(
                JSONObject().put("id", tr.id).put("fileName", tr.fileName).put("durationMs", tr.durationMs)
                    .put("language", tr.language).put("processMs", tr.processMs).put("segments", segs),
            )
        }
        // Atomik yazım: yarıda kesilen yazma geçmişi bozmasın
        val tmp = File(c.filesDir, "history.json.tmp")
        tmp.writeText(arr.toString())
        if (!tmp.renameTo(file(c))) { file(c).writeText(arr.toString()); tmp.delete() }
        return list
    }
}
