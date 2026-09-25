package com.aitolian.sesyazibench.engine

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/**
 * ML Kit çeviri — tamamen cihazda. Her dil paketi ilk kullanımda bir kez
 * indirilir (~30 MB); sonrası internetsiz çalışır.
 *
 * Çeviri TAM CÜMLELER üzerinden yapılır: whisper parçaları cümle ortasından
 * bölünebildiği için parça parça çeviri bağlamı kaybediyor ve anlamsız
 * sonuç veriyordu. Her cümlenin zamanı, başladığı parçanın zamanıdır.
 */
object OnDeviceTranslator {

    fun supports(lang: Lang): Boolean = TranslateLanguage.fromLanguageTag(lang.code) != null

    private val manager get() = RemoteModelManager.getInstance()

    suspend fun isDownloaded(lang: Lang): Boolean {
        val code = TranslateLanguage.fromLanguageTag(lang.code) ?: return false
        return manager.isModelDownloaded(TranslateRemoteModel.Builder(code).build()).await()
    }

    /** Cihazdaki dil paketleri (İngilizce her zaman yerleşik, listelenmez). */
    suspend fun downloadedLanguages(): List<Lang> {
        val models = manager.getDownloadedModels(TranslateRemoteModel::class.java).await()
        val codes = models.map { it.language }.toSet()
        return TRANSLATABLE.filter { it != Lang.EN && TranslateLanguage.fromLanguageTag(it.code) in codes }
    }

    suspend fun delete(lang: Lang) {
        val code = TranslateLanguage.fromLanguageTag(lang.code) ?: return
        manager.deleteDownloadedModel(TranslateRemoteModel.Builder(code).build()).await()
    }

    /** Parçaları cümlelere dönüştürür (bkz. [Sentences.split]). */
    fun toSentences(segments: List<Segment>): List<Segment> = Sentences.split(segments)

    /** @param onStatus kullanıcıya gösterilecek durum metni */
    suspend fun translate(
        segments: List<Segment>,
        source: Lang,
        target: Lang,
        onStatus: (String) -> Unit,
    ): List<Segment> {
        val src = TranslateLanguage.fromLanguageTag(source.code) ?: error("${source.label} çeviri için desteklenmiyor")
        val tgt = TranslateLanguage.fromLanguageTag(target.code) ?: error("${target.label} çeviri için desteklenmiyor")
        val translator = Translation.getClient(
            TranslatorOptions.Builder().setSourceLanguage(src).setTargetLanguage(tgt).build(),
        )
        try {
            if (!isDownloaded(source) || !isDownloaded(target)) {
                onStatus("Dil paketi indiriliyor (tek seferlik, ~30 MB)…")
            }
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            val sentences = toSentences(segments)
            return sentences.mapIndexed { i, s ->
                onStatus("Çevriliyor ${i + 1}/${sentences.size}…")
                Segment(s.startMs, s.endMs, translator.translate(s.text).await(), approx = s.approx)
            }
        } finally {
            translator.close()
        }
    }
}
