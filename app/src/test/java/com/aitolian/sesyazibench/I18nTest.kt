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
            Locale.setDefault(Locale.GERMANY) // tablosu olmayan dil İngilizce görür
            assertEquals("Settings", tx("Ayarlar"))
            assertEquals("tabloda yok", tx("tabloda yok")) // bilinmeyen metin çökmeden kaynak döner
        } finally {
            Locale.setDefault(old)
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
