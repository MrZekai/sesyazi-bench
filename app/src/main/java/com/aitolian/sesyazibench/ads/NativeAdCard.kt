package com.aitolian.sesyazibench.ads

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aitolian.sesyazibench.ui.SY
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Ana sayfada içerik akışına yerleşen yerel (native) reklam kartı.
 * AdMob kuralları: "Reklam" etiketi ve AdChoices görünür; başlık/görsel/düğme
 * reklam öğelerine bağlıdır; uygulama düğmelerine bitişik değildir (kazara tıklama
 * olmasın diye kart, not listesi ile kılavuz arasında ayrı durur).
 * Yüklenemezse hiçbir şey çizilmez (boş alan bırakmaz).
 */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var ad by remember { mutableStateOf<NativeAd?>(null) }

    // Yükleme: ekran açıkken sınırlı, artan beklemeli yeniden deneme (ağ geri gelirse
    // toparlanır; istek fırtınası yok). Ekrandan çıkınca döngü ve reklam kapanır.
    LaunchedEffect(Unit) {
        for (attempt in 0 until NATIVE_TRIES) {
            val n = loadNative(context)
            if (n != null) { ad?.destroy(); ad = n; return@LaunchedEffect }
            if (attempt < NATIVE_TRIES - 1) delay(NATIVE_RETRY_MS * (attempt + 1))
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            ad?.destroy()
            ad = null
        }
    }

    val current = ad ?: return
    val colors = NativeColors(
        text = SY.Text.toArgb(), muted = SY.Muted.toArgb(), accent = SY.Accent.toArgb(),
        onAccent = SY.OnAccent.toArgb(), badge = SY.Chip.toArgb(),
    )
    AndroidView(
        factory = { ctx -> NativeViews(ctx).adView },
        update = { v -> (v.tag as NativeViews).bind(current, colors) },
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SY.Sheet)
            .border(1.dp, SY.Outline, RoundedCornerShape(18.dp)).padding(12.dp),
    )
}

private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

private const val NATIVE_TRIES = 3
private const val NATIVE_RETRY_MS = 20_000L

/** Tek yükleme denemesi; başarısızsa null. İptal edilirse gelen reklam yok edilir. */
private suspend fun loadNative(context: Context): NativeAd? = suspendCancellableCoroutine { cont ->
    val loader = AdLoader.Builder(context, Ads.NATIVE_ID)
        .forNativeAd { n ->
            if (cont.isActive) {
                // Teslim ile devam arasında iptal olursa reklam sahipsiz kalmasın: ana iş parçacığında yok et
                cont.resume(n) { _, abandoned, _ -> abandoned?.let { a -> mainHandler.post { a.destroy() } } }
            } else {
                n.destroy()
            }
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w("Ads", "native load: ${error.message}")
                if (cont.isActive) cont.resume(null)
            }
        })
        .withNativeAdOptions(
            NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                .build(),
        )
        .build()
    loader.loadAd(AdRequest.Builder().build())
}

private data class NativeColors(val text: Int, val muted: Int, val accent: Int, val onAccent: Int, val badge: Int)

/**
 * Klasik View'larla kurulu NativeAdView (Compose'da AndroidView ile gösterilir).
 * NativeAdView final olduğu için kalıtım yerine sarmalanır; tutucu, görünümün tag'inde.
 */
private class NativeViews(context: Context) {
    val adView = NativeAdView(context).also { it.tag = this }
    private val res = context.resources
    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), res.displayMetrics).toInt()

    private val badge = TextView(context).apply {
        text = "Reklam"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setPadding(dp(6), dp(1), dp(6), dp(1))
    }
    private val icon = ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
    private val headline = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        setTypeface(typeface, Typeface.BOLD)
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    private val advertiser = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    private val media = MediaView(context)
    private val body = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        maxLines = 3
        ellipsize = TextUtils.TruncateAt.END
    }
    private val cta = Button(context).apply {
        isAllCaps = false
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        minHeight = dp(48)
    }

    init {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(icon, LinearLayout.LayoutParams(dp(40), dp(40)).apply { marginEnd = dp(10) })
        val titles = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val badgeRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        badgeRow.addView(badge)
        badgeRow.addView(advertiser, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(6) })
        titles.addView(headline)
        titles.addView(badgeRow)
        // AdChoices simgesi sağ üstte: başlık onun altına girmesin
        header.addView(titles, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(20) })
        root.addView(header)
        root.addView(media, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(160)).apply { topMargin = dp(10) })
        root.addView(body, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
        root.addView(cta, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) })
        adView.addView(root)
        adView.headlineView = headline
        adView.iconView = icon
        adView.advertiserView = advertiser
        adView.mediaView = media
        adView.bodyView = body
        adView.callToActionView = cta
    }

    private var bound: NativeAd? = null

    fun bind(ad: NativeAd, c: NativeColors) {
        headline.setTextColor(c.text)
        advertiser.setTextColor(c.muted)
        body.setTextColor(c.muted)
        badge.setTextColor(c.text)
        badge.background = GradientDrawable().apply { cornerRadius = dp(4).toFloat(); setColor(c.badge) }
        cta.setTextColor(c.onAccent)
        cta.background = GradientDrawable().apply { cornerRadius = dp(24).toFloat(); setColor(c.accent) }
        if (bound === ad) return
        bound = ad
        headline.text = ad.headline
        advertiser.text = ad.advertiser ?: ""
        advertiser.visibility = if (ad.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
        body.text = ad.body ?: ""
        body.visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
        cta.text = ad.callToAction ?: ""
        cta.visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
        val img = ad.icon?.drawable
        icon.setImageDrawable(img)
        icon.visibility = if (img == null) View.GONE else View.VISIBLE
        val mc = ad.mediaContent
        if (mc != null) media.setMediaContent(mc)
        media.visibility = if (mc == null) View.GONE else View.VISIBLE
        adView.setNativeAd(ad) // en sonda: gösterim/tıklama takibi bağlanan görünümlere göre yapılır
    }
}
