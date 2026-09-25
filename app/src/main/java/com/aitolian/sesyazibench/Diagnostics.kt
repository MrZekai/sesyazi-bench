package com.aitolian.sesyazibench

import com.aitolian.sesyazibench.audio.DecodedAudio
import com.aitolian.sesyazibench.data.Transcript
import com.aitolian.sesyazibench.engine.WitEngine
import java.util.Locale

/**
 * Geliştirici teşhisi: eksik metnin kaynağını (ses çıkarma mı, parçalama mı,
 * motor yanıtı mı, uygulamanın işlemesi mi) ayırmak için sayılar. Metin, ses
 * ya da anahtar İÇERMEZ; yalnız geliştirici modunda üretilir ve kullanıcı
 * isterse paylaşılır (otomatik kayıt yok).
 */
object Diagnostics {
    private fun f2(v: Double) = "%.2f".format(Locale.US, v)
    private fun clock(ms: Long) = Transcript.clock(ms)

    fun audio(a: DecodedAudio): String = buildString {
        val i = a.info
        append("SES\n")
        if (i == null) {
            append("  bilgi yok (${a.sourceMime}, ${a.sourceChannels} kanal, ${a.sourceRate} Hz)\n")
            append("  PCM: ${a.samples.size} örnek · ${clock(a.durationMs)}\n")
            return@buildString
        }
        append("  izler: ${i.trackCount} (ses: ${i.audioTrackCount}) · seçilen: #${i.selectedTrack}")
        if (i.audioTrackCount > 1) append(" · UYARI: birden çok ses izi, ilki kullanıldı")
        append('\n')
        append("  ${i.mime} · ${i.channels} kanal · ${i.sourceRate} Hz · ${if (i.pcmFloat) "float" else "16-bit"}\n")
        append("  PCM: ${i.decodedSamples} örnek · ${i.decodedMs} ms")
        if (i.sourceDurationMs > 0) {
            val diff = i.decodedMs - i.sourceDurationMs
            append(" · kaynak ${i.sourceDurationMs} ms · fark ${if (diff >= 0) "+" else ""}$diff ms")
        }
        append('\n')
        if (i.monoPhaseRatio >= 0) {
            append("  mono/kanal enerji oranı: ${f2(i.monoPhaseRatio)}")
            if (i.monoPhaseRatio < 0.3) append(" · UYARI: kanallar büyük ölçüde zıt fazlı, mono toplamda ses sönümlenebilir")
            append('\n')
        }
    }

    fun wit(lang: String, oc: WitEngine.Outcome, fills: Map<Int, String>): String = buildString {
        val d = oc.diag
        append("WIT ($lang) · ${d.size} parça · ${oc.result.transcribeMs} ms\n")
        var inTotal = 0
        var kept = 0
        var dups = 0
        var echo = 0
        var unconf = 0
        d.forEach { c ->
            inTotal += c.finalsIn; kept += c.finalsKept; dups += c.dupDropped; echo += c.echoIgnored; unconf += c.unconfirmedRepeat
            append("  #${c.index + 1} ${clock(c.fromMs)}–${clock(c.toMs)} · ${c.samples} örnek/${c.bytes} B · rms ${f2(c.rms * 100)}% (tepe ${f2(c.peakRms * 100)}%)\n")
            append("     denemeler: ${c.codes.joinToString(" → ").ifEmpty { "-" }}")
            append(" · olay ${c.events} · final gelen ${c.finalsIn} / saklanan ${c.finalsKept}")
            if (c.dupDropped > 0) append(" · tekrar atılan ${c.dupDropped} (aynı metin+aynı geçerli zaman)")
            if (c.echoIgnored > 0) append(" · yankı sayılan ara metin ${c.echoIgnored}")
            if (c.unconfirmedRepeat > 0) append(" · UYARI: finali gelmeyen tekrar ara metni ${c.unconfirmedRepeat} (doğrulanamadı)")
            append(
                " · zaman: " + when (c.reliableTimes) {
                    true -> "Wit belirteçleri"
                    false -> "YAKLAŞIK (orantılı)"
                    null -> "-"
                },
            )
            append('\n')
        }
        oc.failed.forEach { f ->
            val from = f.from * 1000L / com.aitolian.sesyazibench.audio.AUDIO_RATE
            val to = f.to * 1000L / com.aitolian.sesyazibench.audio.AUDIO_RATE
            append("  BAŞARISIZ ${clock(from)}–${clock(to)} · ${f.code} · ${fills[f.from] ?: "-"}\n")
        }
        append("  TOPLAM final gelen $inTotal / saklanan $kept · tekrar atılan $dups · yankı $echo · doğrulanamayan tekrar $unconf\n")
    }
}
