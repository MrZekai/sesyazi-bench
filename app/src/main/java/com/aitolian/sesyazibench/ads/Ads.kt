package com.aitolian.sesyazibench.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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
    val APP_OPEN_ID: String = com.aitolian.sesyazibench.BuildConfig.ADMOB_APP_OPEN
    val REWARDED_ID: String = com.aitolian.sesyazibench.BuildConfig.ADMOB_REWARDED

    // Geçiş reklamı sınırları (her döküm başında denenir; bu sınırlar aşırılığı önler)
    private const val MIN_GAP_MS = 60_000L          // iki reklam arası en az 60 sn
    private const val MAX_PER_HOUR = 4              // saatte en fazla 4

    private val started = AtomicBoolean(false)
    private val _ready = MutableStateFlow(false)
    /** Rıza alındı ve SDK hazır — banner ancak o zaman istenir. */
    val ready: StateFlow<Boolean> = _ready
    private var interstitial: InterstitialAd? = null
    private var loading = false
    // Uygulama açılış (app open) reklamı: başka uygulamadan paylaşımla açılışta/öne gelişte
    // gösterilir. AdMob, uygulama yüklenirken geçiş reklamını yasaklar ve bu biçimi önerir.
    private var appOpen: AppOpenAd? = null
    private var appOpenLoadedAt = 0L
    private var appOpenLoading = false
    private const val APP_OPEN_TTL_MS = 4 * 3_600_000L // Google: 4 saatten eski reklam gösterilmez
    private var appContext: Context? = null
    private val main = Handler(Looper.getMainLooper())
    private const val PREFS = "ads"
    private const val KEY_TIMES = "shown_times"
    private const val KEY_FREE_UNTIL = "ad_free_until"
    private const val AD_FREE_MS = 24 * 3_600_000L

    private val _adFree = MutableStateFlow(false)
    /** Ödüllü reklamla kazanılan reklamsız süre içinde mi (banner / native / tam ekran gösterilmez). */
    val adFree: StateFlow<Boolean> = _adFree

    private fun adFreeUntil(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_FREE_UNTIL, 0L)

    /** Saat geri alınırsa süre uzamasın: bitiş en fazla 24 saat ileride sayılır. */
    private fun isAdFree(context: Context): Boolean {
        val left = adFreeUntil(context) - System.currentTimeMillis()
        return left in 1..AD_FREE_MS
    }

    fun adFreeHoursLeft(context: Context): Int {
        val left = (adFreeUntil(context) - System.currentTimeMillis()).coerceIn(0L, AD_FREE_MS)
        return ((left + 3_599_999L) / 3_600_000L).toInt()
    }

    /**
     * Ödüllü reklam: sonuna kadar izlenirse 24 saat reklam gösterilmez.
     * [onResult] ana iş parçacığında: true = ödül kazanıldı, false = erken kapatıldı, null = reklam yüklenemedi.
     */
    fun showRewarded(activity: Activity, onResult: (Boolean?) -> Unit) {
        if (!_ready.value) { onResult(null); return }
        RewardedAd.load(
            activity, REWARDED_ID, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    if (activity.isFinishing || activity.isDestroyed) { onResult(null); return }
                    var earned = false
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() { onResult(earned) }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) { onResult(null) }
                    }
                    ad.show(activity) { _ ->
                        earned = true
                        val appCtx = activity.applicationContext
                        appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putLong(KEY_FREE_UNTIL, System.currentTimeMillis() + AD_FREE_MS).apply()
                        _adFree.value = true
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w("Ads", "rewarded load: ${error.message}")
                    onResult(null)
                }
            },
        )
    }

    /** UMP rıza akışı (AB/UK) → ardından MobileAds başlatılır. */
    fun start(activity: Activity) {
        _adFree.value = isAdFree(activity)
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
                loadAppOpen()
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

    /** Süresi dolmamış açılış reklamı (yoksa null). Ana iş parçacığında çağrılır. */
    private fun freshAppOpen(): AppOpenAd? {
        val ad = appOpen ?: return null
        if (SystemClock.elapsedRealtime() - appOpenLoadedAt > APP_OPEN_TTL_MS) { appOpen = null; return null }
        return ad
    }

    /** Ana iş parçacığında çağrılır. */
    private fun loadAppOpen() {
        val context = appContext ?: return
        if (appOpenLoading || freshAppOpen() != null) return
        appOpenLoading = true
        AppOpenAd.load(
            context, APP_OPEN_ID, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenLoading = false
                    appOpen = ad
                    appOpenLoadedAt = SystemClock.elapsedRealtime()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenLoading = false
                    appOpen = null
                    Log.w("Ads", "app open load: ${error.message}")
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
     * Döküm başında çağrılır. SDK başlatması ve yükleme soğuk açılışta sürebilir;
     * toplam en fazla [timeoutMs] beklenir. [stillWanted] false olur olmaz (metin
     * ekrana geldi, iptal, yeni ses) bırakılır: okunan metnin üstüne sonradan tam
     * ekran reklam binmez (AdMob geçiş reklamı politikası). Döküm reklamı beklemez.
     */
    suspend fun showWhenReady(
        activity: ComponentActivity,
        stillWanted: () -> Boolean,
        /** true: uygulama paylaşımla açıldı/öne geldi → açılış reklamı; false: uygulama içi geçiş → geçiş reklamı. */
        atAppOpen: Boolean = false,
        timeoutMs: Long = 5_000,
    ) {
        fun loaded() = if (atAppOpen) freshAppOpen() != null else interstitial != null
        _adFree.value = isAdFree(activity) // süre dolduysa reklamlar geri gelir
        if (_adFree.value) return
        var waited = 0L
        while (!_ready.value && waited < timeoutMs) {
            if (!stillWanted()) return
            kotlinx.coroutines.delay(150); waited += 150
        }
        if (!_ready.value) return
        if (!loaded()) main.post { if (atAppOpen) loadAppOpen() else loadInterstitial() }
        while (!loaded() && waited < timeoutMs) {
            if (!stillWanted()) return
            kotlinx.coroutines.delay(150); waited += 150
        }
        if (!stillWanted()) return
        if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        showIfAllowed(activity, atAppOpen)
    }

    /** Tam ekran reklam şu an ekranda mı (aynı anda ikinci gösterim olmasın). */
    @Volatile private var showing = false
    @Volatile private var showingSince = 0L

    /**
     * Kota yalnız GERÇEK gösterimde (onAdShowedFullScreenContent) bir kez artar;
     * gösterilemeyen reklam 60 sn / saatlik kotayı tüketmez.
     */
    private fun showIfAllowed(activity: Activity, atAppOpen: Boolean) {
        val now = System.currentTimeMillis()
        // Kapanış geri çağrısı hiç gelmezse (etkinlik yok edildi vb.) kilit 5 dk sonra düşer
        if (showing && now - showingSince < 300_000L) return
        val times = recentShows(activity, now)
        val last = times.maxOrNull()
        val allowed = (last == null || now - last >= MIN_GAP_MS) && times.size < MAX_PER_HOUR
        if (!allowed) return
        val open = if (atAppOpen) freshAppOpen() else null
        val inter = if (atAppOpen) null else interstitial
        if (open == null && inter == null) return
        // tek kullanımlık nesne: gösterimden önce tüket
        if (atAppOpen) appOpen = null else interstitial = null
        fun reload() { if (atAppOpen) loadAppOpen() else loadInterstitial() }
        val appCtx = activity.applicationContext
        var counted = false
        val callback = object : FullScreenContentCallback() {
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
                reload()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                showing = false
                Log.w("Ads", "full screen show: ${error.message}")
                reload()
            }
        }
        showing = true
        showingSince = now
        if (open != null) {
            open.fullScreenContentCallback = callback
            open.show(activity)
        } else if (inter != null) {
            inter.fullScreenContentCallback = callback
            inter.show(activity)
        }
    }
}
