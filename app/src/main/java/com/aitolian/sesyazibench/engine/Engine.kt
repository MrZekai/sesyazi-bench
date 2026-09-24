package com.aitolian.sesyazibench.engine

import com.aitolian.sesyazibench.audio.DecodedAudio

data class Segment(val startMs: Long, val endMs: Long, val text: String)

data class EngineResult(
    val engine: String,
    val variant: String,
    val language: String,
    val detectedLanguage: String?,
    val audioMs: Long,
    val loadMs: Long,
    val transcribeMs: Long,
    val text: String,
    val segments: List<Segment> = emptyList(),
    val error: String? = null,
    /** Aşama süreleri (ms) — yalnızca ölçüm; metin içermez. */
    val detectMs: Long = 0,
    /** Encoder toplamı (pencere × ortalama). */
    val encodeMs: Long = 0,
    /** STT'nin encoder dışındaki kısmı: decoder + VAD + örnekleme. */
    val decodeMs: Long = 0,
    /** Native döküm başladıktan sonra ilk cümlenin geldiği an (ms); gelmediyse -1. */
    val firstSegmentMs: Long = -1,
    val threads: Int = 0,
    /** Encoder'ın çalıştığı 30 sn'lik pencere sayısı. */
    val windows: Int = 0,
    /**
     * Dil nasıl belirlendi: "secili" (kullanıcı seçti), "base" (ayrı küçük modelle,
     * süresi detectMs'te), "model_ici" (büyük modelin içinde; süresi STT'ye dahil).
     */
    val detectPath: String = "",
    /** Native çıktıdaki geçerli zaman damgalı parça sayısı; metin filtresinden ÖNCE. */
    val rawSegmentCount: Int = 0,
) {
    /** Gerçek zaman katsayısı: 0.25 => 60 sn ses 15 sn'de bitti. */
    val rtf: Double get() = if (audioMs > 0) transcribeMs.toDouble() / audioMs else 0.0

    /** Hedef: 60 sn ses ≤ 15 sn  =>  RTF ≤ 0.25 */
    val passesTarget: Boolean get() = error == null && rtf <= 0.25
}

/** Uygulamanın desteklediği dil seçenekleri (whisper kodu -> ML Kit locale). */
enum class Lang(val code: String, val label: String, val mlKitTag: String?) {
    TR("tr", "Türkçe", "tr-TR"),
    EN("en", "English", "en-US"),
    DE("de", "Deutsch", "de-DE"),
    FR("fr", "Français", "fr-FR"),
    ES("es", "Español", "es-ES"),
    IT("it", "Italiano", "it-IT"),
    PT("pt", "Português", "pt-BR"),
    RU("ru", "Русский", "ru-RU"),
    AR("ar", "العربية", null),
    HI("hi", "हिन्दी", "hi-IN"),
    ID("id", "Bahasa Indonesia", null),
    AUTO("auto", "Otomatik", null),
}

/** Çeviri hedefi olabilecek diller (otomatik hariç). */
val TRANSLATABLE: List<Lang> = Lang.entries.filter { it != Lang.AUTO }

fun langOf(code: String?): Lang? = Lang.entries.firstOrNull { it.code == code }

interface TranscriptionEngine {
    val name: String
    suspend fun transcribe(audio: DecodedAudio, lang: Lang): EngineResult
}
