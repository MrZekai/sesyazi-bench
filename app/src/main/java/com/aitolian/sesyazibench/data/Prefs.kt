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

    /** Geliştirici A/B ölçümü: whisper thread sayısı (0 = otomatik). */
    var threadOverride: Int
        get() = p.getInt("threads", 0)
        set(v) = p.edit().putInt("threads", v).apply()

    var devMode: Boolean
        get() = p.getBoolean("dev", false)
        set(v) = p.edit().putBoolean("dev", v).apply()
}
