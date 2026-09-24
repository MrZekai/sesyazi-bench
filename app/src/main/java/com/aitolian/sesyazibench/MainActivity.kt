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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.aitolian.sesyazibench.ads.Ads
import com.aitolian.sesyazibench.ui.MainScreen
import com.aitolian.sesyazibench.ui.SesYaziTheme
import com.aitolian.sesyazibench.ui.isDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import android.graphics.drawable.ColorDrawable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
        ShareIntegration.publishShareShortcut(this)
        if (savedInstanceState == null) handleShare(intent)
        setContent {
            // Yalnızca tema değişince yenilenir (oynatma adımlarında kök yeniden çizilmesin)
            val themeFlow = remember { vm.state.map { it.themeMode }.distinctUntilChanged() }
            val themeMode by themeFlow.collectAsStateWithLifecycle(vm.state.value.themeMode)
            val dark = isDarkTheme(themeMode)
            // Durum/gezinme çubuğu simgeleri ve pencere zemini temaya uysun
            LaunchedEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                window.setBackgroundDrawable(ColorDrawable(if (dark) 0xFF0E0B1A.toInt() else 0xFFF6F4FB.toInt()))
            }
            SesYaziTheme(themeMode) {
                MainScreen(
                    vm,
                    onNewAudio = { then -> then() }, // reklam artık döküm başında
                    // Metin ekrana gelmeden önce; metin geldiyse / iş iptal edildiyse gösterilmez
                    onProcessingAd = {
                        lifecycleScope.launch { Ads.showWhenReady(this@MainActivity, stillWanted = vm::adStillWanted) }
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Notifier.appVisible = true
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Arka planda büyük modeli tutmak sistemin uygulamayı öldürmesine yol açar
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            com.aitolian.sesyazibench.engine.WhisperEngine.releaseIfIdle()
        }
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
        if (uri != null) {
            ShareIntegration.reportUsed(this) // paylaş listesinde öne çıkma sinyali
            vm.onAudio(uri)
        }
    }
}
