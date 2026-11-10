package com.aitolian.sesyazibench.data

import android.content.Context
import android.content.SharedPreferences

/** Kullanıcı tercihleri — cihazda, SharedPreferences. */
class Prefs(context: Context) {
    private val p: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var defaultLang: String?
        get() = p.getString("lang", null)
        set(v) = p.edit().putString("lang", v).apply()

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
        get() = p.getInt("theme", 1) // varsayılan: açık tema
        set(v) = p.edit().putInt("theme", v).apply()

    var devMode: Boolean
        get() = p.getBoolean("dev", false)
        set(v) = p.edit().putBoolean("dev", v).apply()
}
