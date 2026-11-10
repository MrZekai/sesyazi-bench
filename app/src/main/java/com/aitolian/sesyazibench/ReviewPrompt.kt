package com.aitolian.sesyazibench

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Uygulama içi puanlama (Play In-App Review). Pencereyi gösterip göstermemeye Play
 * karar verir (kota var); sonuç bilinmez ve akış her durumda sessizce biter.
 * İstek sayacı ViewModel'dedir; aynı istek (ör. ekran döndürme) iki kez işlenmez.
 */
object ReviewPrompt {
    private var handled = 0

    fun ask(activity: Activity, request: Int) {
        if (request <= handled) return
        handled = request
        runCatching {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful && !activity.isFinishing && !activity.isDestroyed) {
                    runCatching { manager.launchReviewFlow(activity, task.result) }
                }
            }
        }
    }
}
