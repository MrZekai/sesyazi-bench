package com.aitolian.sesyazibench.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

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

    var whatsappTree: Uri?
        get() = p.getString("wa_tree", null)?.let(Uri::parse)
        set(v) = p.edit().putString("wa_tree", v?.toString()).apply()

    var devMode: Boolean
        get() = p.getBoolean("dev", false)
        set(v) = p.edit().putBoolean("dev", v).apply()
}
