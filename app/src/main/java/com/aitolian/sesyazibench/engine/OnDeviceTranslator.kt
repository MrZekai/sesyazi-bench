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
 * indirilir (~30 MB); sonrası internetsiz çalışır. Zaman damgaları korunur:
 * her satır ayrı çevrilir.
 */
object OnDeviceTranslator {

    fun supports(lang: Lang): Boolean = TranslateLanguage.fromLanguageTag(lang.code) != null

    suspend fun isDownloaded(lang: Lang): Boolean {
        val code = TranslateLanguage.fromLanguageTag(lang.code) ?: return false
        val model = TranslateRemoteModel.Builder(code).build()
        return RemoteModelManager.getInstance().isModelDownloaded(model).await()
    }

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
            return segments.mapIndexed { i, s ->
                onStatus("Çevriliyor ${i + 1}/${segments.size}…")
                Segment(s.startMs, s.endMs, translator.translate(s.text).await())
            }
        } finally {
            translator.close()
        }
    }
}
