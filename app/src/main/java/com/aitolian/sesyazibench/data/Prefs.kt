package com.aitolian.sesyazibench.data

import android.content.Context
import android.content.SharedPreferences

/** Kullanıcı tercihleri — cihazda, SharedPreferences. */
class Prefs(context: Context) {
    private val p: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var defaultLang: String?
        get() = p.getString("lang", null)
        set(v) = p.edit().putString("lang", v).apply()

    var defaultQuality: String?
        get() = p.getString("quality", null)
        set(v) = p.edit().putString("quality", v).apply()

    var translateTarget: String?
        get() = p.getString("translate_target", null)
        set(v) = p.edit().putString("translate_target", v).apply()

    var notifyWhenDone: Boolean
        get() = p.getBoolean("notify", true)
        set(v) = p.edit().putBoolean("notify", v).apply()

    var readerFont: Int
        get() = p.getInt("reader_font", 19)
        set(v) = p.edit().putInt("reader_font", v).apply()

    /** Satır aralığı: 0 sıkı, 1 normal, 2 geniş. */
    var readerLine: Int
        get() = p.getInt("reader_line", 1)
        set(v) = p.edit().putInt("reader_line", v).apply()

    /** Ses çalarken okunan cümleyi takip et (ekran kendiliğinden kayar). */
    var followAudio: Boolean
        get() = p.getBoolean("follow_audio", true)
        set(v) = p.edit().putBoolean("follow_audio", v).apply()

    /** Tema: 0 sistem, 1 açık, 2 koyu. */
    var themeMode: Int
        get() = p.getInt("theme", 0)
        set(v) = p.edit().putInt("theme", v).apply()

    /** Geliştirici A/B ölçümü: whisper thread sayısı (0 = otomatik). */
    var threadOverride: Int
        get() = p.getInt("threads", 0)
        set(v) = p.edit().putInt("threads", v).apply()

    /** Deney: tekrar deneme (temperature fallback) 0 = otomatik, 1 = hep açık, 2 = hep kapalı. */
    var fallbackMode: Int
        get() = p.getInt("fallback", 0)
        set(v) = p.edit().putInt("fallback", v).apply()

    /** Deney: En iyi'de önce ön izleme göster (true) ya da büyük modeli doğrudan çalıştır. */
    var bestPreview: Boolean
        get() = p.getBoolean("best_preview", true)
        set(v) = p.edit().putBoolean("best_preview", v).apply()

    /** Deney: En iyi için large-v3-turbo q8_0 (834 MB) kullan. */
    var turboQ8: Boolean
        get() = p.getBoolean("turbo_q8", false)
        set(v) = p.edit().putBoolean("turbo_q8", v).apply()

    /** Deney tercihlerinin görünmeden normal kullanımı etkilemesini engeller. */
    fun disableDevModeAndResetExperiments() {
        p.edit().putBoolean("dev", false)
            .remove("fallback").remove("best_preview").remove("turbo_q8").remove("threads")
            .apply()
    }

    /**
     * Yazıya dökme motoru: 0 = henüz sorulmadı, 1 = Hızlı (internet, Wit.ai;
     * olmazsa telefonda), 2 = Gizli (yalnızca telefonda).
     */
    var engineMode: Int
        get() = p.getInt("engine_mode", 0)
        set(v) = p.edit().putInt("engine_mode", v).apply()

    /**
     * İlk bulut aktarımı onayı: 0 = sorulmadı, 1 = "İnternetle devam et" (ya da Ayarlar'da
     * açıklamayı görerek Hızlı seçildi). Reddetme/kapatma kabul sayılmaz.
     */
    var cloudConsent: Int
        get() = p.getInt("cloud_consent", 0)
        set(v) = p.edit().putInt("cloud_consent", v).apply()

    var devMode: Boolean
        get() = p.getBoolean("dev", false)
        set(v) = p.edit().putBoolean("dev", v).apply()
}
