package com.aitolian.sesyazibench

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

/**
 * WhatsApp ile entegrasyon: Android'in izin verdiği tek temiz yol olan
 * "Paylaş" menüsü. Klasör/dosya izni istenmez.
 */
object ShareIntegration {
    private const val SHORTCUT_ID = "transcribe"
    /** res/xml/shortcuts.xml içindeki share-target kategorisiyle aynı olmalı. */
    private const val CATEGORY = "com.aitolian.sesyazibench.category.TRANSCRIBE"
    private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

    /**
     * Paylaş listesinde uygulamanın "doğrudan paylaşım" hedefi olarak öne
     * çıkması için uzun ömürlü kısayol. Sıralamayı Android kullanım sıklığına
     * göre yapar; [reportUsed] her paylaşımda sinyal verir.
     */
    fun publishShareShortcut(context: Context) {
        runCatching {
            val label = context.getString(R.string.app_name)
            val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
                .setShortLabel(label)
                .setLongLabel("$label ile yazıya dök")
                .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(Intent(context, MainActivity::class.java).setAction(Intent.ACTION_MAIN))
                .setLongLived(true)
                .setCategories(setOf(CATEGORY))
                .build()
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        }
    }

    fun reportUsed(context: Context) {
        runCatching { ShortcutManagerCompat.reportShortcutUsed(context, SHORTCUT_ID) }
    }

    /** WhatsApp'ı (yoksa WhatsApp Business'ı) açar; hiçbiri yoksa false. */
    fun openWhatsApp(context: Context): Boolean {
        for (pkg in WHATSAPP_PACKAGES) {
            val launch = context.packageManager.getLaunchIntentForPackage(pkg) ?: continue
            return runCatching { context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true }
                .getOrDefault(false)
        }
        return false
    }
}
