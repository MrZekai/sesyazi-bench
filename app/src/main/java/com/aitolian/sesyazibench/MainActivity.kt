package com.aitolian.sesyazibench

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.aitolian.sesyazibench.ads.Ads
import com.aitolian.sesyazibench.ui.MainScreen
import com.aitolian.sesyazibench.ui.SesYaziTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        Ads.start(this)
        askNotificationPermissionOnce()
        if (savedInstanceState == null) handleShare(intent)
        setContent {
            SesYaziTheme {
                MainScreen(
                    vm,
                    onNewAudio = { then -> Ads.maybeShowInterstitial(this, then) },
                    onProcessingAd = { Ads.maybeShowInterstitial(this) {} },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Notifier.appVisible = true
    }

    override fun onStop() {
        Notifier.appVisible = false
        super.onStop()
    }

    /** Uzun dökümler arka planda bittiğinde haber verebilmek için (Android 13+), bir kez sorulur. */
    private fun askNotificationPermissionOnce() {
        if (Build.VERSION.SDK_INT < 33) return
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        if (prefs.getBoolean("asked_notif", false)) return
        prefs.edit().putBoolean("asked_notif", true).apply()
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShare(intent)
    }

    private fun handleShare(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val uri: Uri? = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
        uri?.let(vm::onAudio)
    }
}
