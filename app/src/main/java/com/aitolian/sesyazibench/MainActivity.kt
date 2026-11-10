package com.aitolian.sesyazibench

import android.content.Intent
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
        // Bildirim izni açılışta SORULMAZ (WhatsApp'tan gelen kullanıcıyı bekletmesin);
        // Ayarlar'da "İşlem bitince haber ver" açılınca istenir.
        ShareIntegration.publishShareShortcut(this)
        if (savedInstanceState == null) handleShare(intent)
        // Uygulama içi puanlama: ViewModel isteyince (not kapatılırken) Play penceresi açılır
        lifecycleScope.launch {
            vm.state.map { it.reviewRequest }.distinctUntilChanged().collect { n -> ReviewPrompt.ask(this@MainActivity, n) }
        }
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
                window.setBackgroundDrawable(ColorDrawable(if (dark) 0xFF0F1115.toInt() else 0xFFF5F6F8.toInt()))
            }
            SesYaziTheme(themeMode) {
                MainScreen(
                    vm,
                    onNewAudio = { then -> then() }, // reklam artık döküm başında
                    // Metin ekrana gelmeden önce; metin geldiyse / iş iptal edildiyse gösterilmez
                    onProcessingAd = {
                        lifecycleScope.launch {
                            Ads.showWhenReady(this@MainActivity, stillWanted = vm::adStillWanted, atAppOpen = vm.adAtAppOpen)
                        }
                    },
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShare(intent)
    }

    private fun handleShare(intent: Intent?) {
        val noteId = intent?.getLongExtra(Notifier.EXTRA_NOTE_ID, -1L) ?: -1L
        if (noteId > 0) {
            intent?.removeExtra(Notifier.EXTRA_NOTE_ID)
            vm.openNoteById(noteId)
            return
        }
        val action = intent?.action
        if (intent == null || (action != Intent.ACTION_SEND && action != Intent.ACTION_VIEW)) return
        val uri: Uri? = if (action == Intent.ACTION_VIEW) {
            intent.data // "Birlikte aç"
        } else if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
        if (uri != null) {
            if (action == Intent.ACTION_SEND) ShareIntegration.reportUsed(this) // paylaş listesinde öne çıkma sinyali
            vm.onSharedAudio(uri)
        }
    }
}
