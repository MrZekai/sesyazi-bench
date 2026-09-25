package com.aitolian.sesyazibench.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
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
 * AdMob. Kimlikler derlemeden gelir (app/build.gradle.kts): debug her zaman Google
 * test kimlikleri; release, GitHub secret'ları varsa gerçek kimlikler.
 */
object Ads {
    val BANNER_ID: String = com.aitolian.sesyazibench.BuildConfig.ADMOB_BANNER
    val INTERSTITIAL_ID: String = com.aitolian.sesyazibench.BuildConfig.ADMOB_INTERSTITIAL
    val NATIVE_ID: String = com.aitolian.sesyazibench.BuildConfig.ADMOB_NATIVE

    // Geçiş reklamı sınırları (her döküm başında denenir; bu sınırlar aşırılığı önler)
    private const val MIN_GAP_MS = 60_000L          // iki reklam arası en az 60 sn
    private const val MAX_PER_HOUR = 4              // saatte en fazla 4

    private val started = AtomicBoolean(false)
    private val _ready = MutableStateFlow(false)
    /** Rıza alındı ve SDK hazır — banner ancak o zaman istenir. */
    val ready: StateFlow<Boolean> = _ready
    private var interstitial: InterstitialAd? = null
    private var loading = false
    private var appContext: Context? = null
    private val main = Handler(Looper.getMainLooper())
    private const val PREFS = "ads"
    private const val KEY_TIMES = "shown_times"

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
        appContext = activity.applicationContext
        // Geliştiricinin kendi telefonları: gerçek kimlikle derlense bile test reklamı alır
        val testDevices = com.aitolian.sesyazibench.BuildConfig.ADMOB_TEST_DEVICES
            .split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (testDevices.isNotEmpty()) {
            MobileAds.setRequestConfiguration(
                com.google.android.gms.ads.RequestConfiguration.Builder().setTestDeviceIds(testDevices).build(),
            )
        }
        MobileAds.initialize(activity.applicationContext) {
            main.post {
                _ready.value = true
                loadInterstitial()
            }
        }
    }

    /** Ana iş parçacığında çağrılır. Başarısız yüklemede 30 sn sonra bir kez daha dener. */
    private fun loadInterstitial(retry: Int = 0) {
        val context = appContext ?: return
        if (interstitial != null || loading) return
        loading = true
        InterstitialAd.load(
            context, INTERSTITIAL_ID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { loading = false; interstitial = ad }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    interstitial = null
                    Log.w("Ads", "interstitial load: ${error.message}")
                    if (retry < 3) main.postDelayed({ loadInterstitial(retry + 1) }, 30_000L * (retry + 1))
                }
            },
        )
    }

    /** Gösterim zamanları kalıcıdır (uygulama yeniden açılınca sınır sıfırlanmaz). */
    private fun recentShows(context: Context, now: Long): MutableList<Long> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TIMES, "") ?: ""
        return raw.split(',').mapNotNull { it.toLongOrNull() }
            .filter { now - it in 0..3_600_000L } // saat geri alınırsa da eski kayıtları at
            .toMutableList()
    }

    private fun saveShows(context: Context, times: List<Long>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TIMES, times.joinToString(",")).apply()
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

    /**
     * Döküm başında çağrılır (paylaş → reklam → metin). SDK başlatması ve reklam
     * yüklemesi soğuk açılışta birkaç saniye sürebilir; toplam en fazla [timeoutMs]
     * beklenir. [stillWanted]: aynı döküm oturumu sürüyor/yeni bitti ve kullanıcı
     * iptal etmedi. Metin ekrana gelmiş olsa da oturum aynıysa reklam gösterilir
     * (kullanıcı kararı: geçiş reklamı her dökümde çıkar); süre dolarsa atlanır.
     */
    suspend fun showWhenReady(activity: ComponentActivity, stillWanted: () -> Boolean, timeoutMs: Long = 5_000) {
        var waited = 0L
        while (!_ready.value && waited < timeoutMs) {
            if (!stillWanted()) return
            kotlinx.coroutines.delay(150); waited += 150
        }
        if (!_ready.value) return
        if (interstitial == null) main.post { loadInterstitial() }
        while (interstitial == null && waited < timeoutMs) {
            if (!stillWanted()) return
            kotlinx.coroutines.delay(150); waited += 150
        }
        if (!stillWanted()) return
        if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        showIfAllowed(activity)
    }

    /** Geriye dönük uyumluluk; sayaç artık gösterimde tutuluyor. */
    fun onTranscriptionDone() {}

    /** Tam ekran reklam şu an ekranda mı (aynı anda ikinci gösterim olmasın). */
    @Volatile private var showing = false
    @Volatile private var showingSince = 0L

    /**
     * Kota yalnız GERÇEK gösterimde (onAdShowedFullScreenContent) bir kez artar;
     * gösterilemeyen reklam 60 sn / saatlik kotayı tüketmez.
     */
    private fun showIfAllowed(activity: Activity) {
        val now = System.currentTimeMillis()
        // Kapanış geri çağrısı hiç gelmezse (etkinlik yok edildi vb.) kilit 5 dk sonra düşer
        if (showing && now - showingSince < 300_000L) return
        val times = recentShows(activity, now)
        val last = times.maxOrNull()
        val allowed = (last == null || now - last >= MIN_GAP_MS) && times.size < MAX_PER_HOUR
        val ad = interstitial
        if (!allowed || ad == null) return
        interstitial = null // tek kullanımlık nesne: gösterimden önce tüket
        val appCtx = activity.applicationContext
        var counted = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                if (counted) return
                counted = true
                val shownAt = System.currentTimeMillis()
                val actual = recentShows(appCtx, shownAt)
                actual += shownAt
                saveShows(appCtx, actual)
            }
            override fun onAdDismissedFullScreenContent() {
                showing = false
                loadInterstitial()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                showing = false
                Log.w("Ads", "interstitial show: ${error.message}")
                loadInterstitial()
            }
        }
        showing = true
        showingSince = now
        ad.show(activity)
    }
}
