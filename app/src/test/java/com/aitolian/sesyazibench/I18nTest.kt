package com.aitolian.sesyazibench

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class I18nTest {
    private val placeholder = Regex("\\{\\d\\}")

    @Test
    fun turkishPhoneSeesSourceOthersSeeEnglish() {
        val old = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("Silindi: x", tx("Silindi: {0}", "x"))
            Locale.setDefault(Locale.ENGLISH)
            assertEquals("Deleted: x", tx("Silindi: {0}", "x"))
            Locale.setDefault(Locale.GERMANY)
            assertEquals("Einstellungen", tx("Ayarlar"))
            Locale.setDefault(Locale.forLanguageTag("ja-JP")) // tablosu olmayan dil İngilizce görür
            assertEquals("Settings", tx("Ayarlar"))
            assertEquals("tabloda yok", tx("tabloda yok")) // bilinmeyen metin çökmeden kaynak döner
        } finally {
            Locale.setDefault(old)
        }
    }

    @Test
    fun everyLanguageCoversEveryText() {
        I18n.LANGS.forEach { lang ->
            val table = I18n.table(lang)
            assertEquals("eksik/fazla metin: $lang", I18n.EN.keys, table.keys)
            // Boş değer = o dilde bir önceki parçayla birleştirilmiş paragraf; yer tutucu taşımaz
            table.forEach { (source, text) ->
                if (text.isNotEmpty() && placeholder.containsMatchIn(source)) {
                    assertEquals(
                        "yer tutucu uyuşmuyor ($lang): $source",
                        placeholder.findAll(source).map { it.value }.toSet(),
                        placeholder.findAll(text).map { it.value }.toSet(),
                    )
                }
            }
        }
    }

    @Test
    fun translationsKeepPlaceholders() {
        I18n.EN.forEach { (source, en) ->
            assertTrue("boş çeviri: $source", en.isNotBlank())
            assertEquals(
                "yer tutucu uyuşmuyor: $source",
                placeholder.findAll(source).map { it.value }.toSet(),
                placeholder.findAll(en).map { it.value }.toSet(),
            )
        }
    }
}
