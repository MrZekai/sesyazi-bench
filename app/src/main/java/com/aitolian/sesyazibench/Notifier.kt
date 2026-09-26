package com.aitolian.sesyazibench

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Uygulama arka plandayken döküm biterse "Metnin hazır" bildirimi. */
object Notifier {
    private const val CHANNEL = "transcript_done"
    @Volatile var appVisible = true

    const val EXTRA_NOTE_ID = "note_id"

    /**
     * Kilit ekranında metin gösterilmez (genel başlık); dokununca ilgili not açılır.
     */
    fun notifyDone(c: Context, noteId: Long) {
        if (appVisible || !com.aitolian.sesyazibench.data.Prefs(c).notifyWhenDone) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = c.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "Döküm tamamlandı", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        val open = PendingIntent.getActivity(
            c, 0,
            Intent(c, MainActivity::class.java)
                .putExtra(EXTRA_NOTE_ID, noteId)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_fg)
            .setContentTitle("Metnin hazır")
            .setContentText("Açmak için dokun")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(c).notify(1, n) }
    }
}
