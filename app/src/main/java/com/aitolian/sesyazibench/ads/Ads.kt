package com.aitolian.sesyazibench.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * AdMob — ŞU AN YALNIZCA GOOGLE TEST ID'LERİ.
 * Gerçek ID'ler sadece production derlemesinde, ayrı bir adımda eklenecek.
 */
object Ads {
    const val BANNER_ID = "ca-app-pub-3940256099942544/9214589741"        // test: adaptive banner
    const val INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"  // test: interstitial

    // Geçiş reklamı sınırları
    private const val MIN_DOCS_BETWEEN = 2          // en az 2 dökümde bir
    private const val MIN_GAP_MS = 60_000L          // iki reklam arası en az 60 sn
    private const val MAX_PER_HOUR = 3              // saatte en fazla 3

    private val started = AtomicBoolean(false)
    private val _ready = MutableStateFlow(false)
    /** Rıza alındı ve SDK hazır — banner ancak o zaman istenir. */
    val ready: StateFlow<Boolean> = _ready
    private var interstitial: InterstitialAd? = null
    private var docsSinceLast = 0
    private var lastShownAt = 0L
    private val shownTimes = ArrayDeque<Long>()

    /** UMP rıza akışı (AB/UK) → ardından MobileAds başlatılır. */
    fun start(activity: Activity) {
        val consent: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { err ->
                    if (err != null) Log.w("Ads", "consent form: ${err.message}")
                    if (consent.canRequestAds()) initSdk(activity)
                }
            },
            { err -> Log.w("Ads", "consent update: ${err.message}") },
        )
        if (consent.canRequestAds()) initSdk(activity)
    }

    private fun initSdk(activity: Activity) {
        if (!started.compareAndSet(false, true)) return
        MobileAds.initialize(activity.applicationContext) {
            _ready.value = true
            loadInterstitial(activity.applicationContext)
        }
    }

    private fun loadInterstitial(context: Context) {
        InterstitialAd.load(
            context, INTERSTITIAL_ID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    Log.w("Ads", "interstitial load: ${error.message}")
                }
            },
        )
    }

    /** AB/UK'de kullanıcı reklam rızasını sonradan değiştirebilmeli (Ayarlar > Gizlilik). */
    fun privacyOptionsRequired(activity: Activity): Boolean =
        UserMessagingPlatform.getConsentInformation(activity).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { err ->
            if (err != null) Log.w("Ads", "privacy options: ${err.message}")
        }
    }

    /** Her tamamlanan dökümde çağrılır. */
    fun onTranscriptionDone() { docsSinceLast++ }

    /**
     * "Yeni ses"e basıldığında: sınırlar uygunsa geçiş reklamı gösterir,
     * ardından (ya da hemen) [then] çalışır. İşlem sırasında asla çağrılmaz.
     */
    fun maybeShowInterstitial(activity: Activity, then: () -> Unit) {
        val now = SystemClock.elapsedRealtime()
        while (shownTimes.isNotEmpty() && now - shownTimes.first() > 3_600_000L) shownTimes.removeFirst()
        val ad = interstitial
        val allowed = ad != null &&
            docsSinceLast >= MIN_DOCS_BETWEEN &&
            (lastShownAt == 0L || now - lastShownAt >= MIN_GAP_MS) &&
            shownTimes.size < MAX_PER_HOUR
        if (!allowed) { then(); return }

        ad!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial(activity.applicationContext)
                then()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                loadInterstitial(activity.applicationContext)
                then()
            }
        }
        docsSinceLast = 0
        lastShownAt = now
        shownTimes.addLast(now)
        ad.show(activity)
    }
}
