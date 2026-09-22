package com.aitolian.sesyazibench

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aitolian.sesyazibench.ads.Ads
import com.aitolian.sesyazibench.audio.AudioDecoder
import com.aitolian.sesyazibench.audio.DecodedAudio
import com.aitolian.sesyazibench.audio.Player
import com.aitolian.sesyazibench.audio.peaks
import com.aitolian.sesyazibench.data.HistoryStore
import com.aitolian.sesyazibench.data.Transcript
import com.aitolian.sesyazibench.engine.EngineResult
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.MlKitEngine
import com.aitolian.sesyazibench.engine.ModelStore
import com.aitolian.sesyazibench.engine.WhisperEngine
import com.aitolian.sesyazibench.engine.WhisperModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

enum class Quality(val label: String, val model: WhisperModel) {
    FAST("Hızlı", WhisperModel.BASE),
    BALANCED("Dengeli", WhisperModel.SMALL),
}

sealed interface Phase {
    data object Idle : Phase
    data class Preparing(val message: String) : Phase
    data class Downloading(val progress: Float) : Phase
    data class Transcribing(val percent: Int) : Phase
    data class Failed(val message: String) : Phase
}

enum class Tab { TEXT, TRANSLATION, SUMMARY }

data class MainState(
    val phase: Phase = Phase.Idle,
    val lang: Lang = Lang.TR,
    val quality: Quality = Quality.FAST,
    val fileName: String? = null,
    val audioMs: Long = 0,
    val waveform: FloatArray = FloatArray(0),
    val hasAudio: Boolean = false,
    val playing: Boolean = false,
    val positionMs: Long = 0,
    val result: Transcript? = null,
    val history: List<Transcript> = emptyList(),
    val tab: Tab = Tab.TEXT,
    val toast: String? = null,
    val testLog: List<String> = emptyList(),
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx get() = getApplication<Application>()
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    private val player = Player()
    private var audio: DecodedAudio? = null
    private var work: Job? = null
    private var ticker: Job? = null
    private val cancel = AtomicBoolean(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val h = HistoryStore.load(ctx)
            _state.update { it.copy(history = h) }
        }
    }

    fun setLang(l: Lang) = _state.update { it.copy(lang = l) }
    fun setQuality(q: Quality) = _state.update { it.copy(quality = q) }
    fun setTab(t: Tab) = _state.update { it.copy(tab = t) }
    fun toastShown() = _state.update { it.copy(toast = null) }
    fun toast(msg: String) = _state.update { it.copy(toast = msg) }

    /** Ses seçildi / paylaşıldı → kopyala, çöz, otomatik yazıya dök. */
    fun onAudio(uri: Uri) {
        if (work?.isActive == true) { toast("Önce mevcut işlem bitsin"); return }
        stopPlayback()
        work = viewModelScope.launch {
            _state.update { it.copy(phase = Phase.Preparing("Ses hazırlanıyor…"), result = null, tab = Tab.TEXT) }
            try {
                val name = withContext(Dispatchers.IO) { displayName(uri) }
                val copy = withContext(Dispatchers.IO) { copyToCache(uri, name) }
                val decoded = withContext(Dispatchers.IO) { AudioDecoder.decode(ctx, Uri.fromFile(copy)) }
                if (decoded.samples.isEmpty()) error("Seste okunabilir içerik yok")
                audio = decoded
                player.setSource(copy)
                _state.update {
                    it.copy(
                        fileName = name, audioMs = decoded.durationMs, waveform = decoded.peaks(),
                        hasAudio = true, positionMs = 0,
                    )
                }
                transcribeCurrent()
            } catch (t: Throwable) {
                _state.update { it.copy(phase = Phase.Failed("Ses açılamadı: ${t.message ?: t.javaClass.simpleName}")) }
            }
        }
    }

    /** Dil veya kalite değişince aynı sesi yeniden dökmek için. */
    fun retranscribe() {
        if (audio == null || work?.isActive == true) return
        work = viewModelScope.launch { transcribeCurrent() }
    }

    private suspend fun transcribeCurrent() {
        val a = audio ?: return
        val s = state.value
        val model = s.quality.model
        if (!ModelStore.isReady(ctx, model)) {
            _state.update { it.copy(phase = Phase.Downloading(0f)) }
            try {
                ModelStore.download(ctx, model) { p -> _state.update { it.copy(phase = Phase.Downloading(p)) } }
            } catch (t: Throwable) {
                _state.update { it.copy(phase = Phase.Failed("Model indirilemedi (${t.message}). İnterneti kontrol et.")) }
                return
            }
        }
        cancel.set(false)
        _state.update { it.copy(phase = Phase.Transcribing(0)) }
        val r = WhisperEngine(ctx, model).transcribe(
            a, s.lang,
            onProgress = { p -> _state.update { it.copy(phase = Phase.Transcribing(p.coerceIn(0, 100))) } },
            cancel = cancel,
        )
        withContext(Dispatchers.IO) { ResultLog.append(ctx, s.fileName ?: "?", r) }
        if (r.error != null) {
            _state.update { it.copy(phase = if (cancel.get()) Phase.Idle else Phase.Failed(r.error)) }
            return
        }
        if (r.segments.isEmpty()) {
            _state.update { it.copy(phase = Phase.Failed("Konuşma algılanamadı. Dil seçimini kontrol et.")) }
            return
        }
        val t = Transcript(
            id = System.currentTimeMillis(),
            fileName = s.fileName ?: "ses",
            durationMs = a.durationMs,
            language = r.detectedLanguage ?: s.lang.code,
            processMs = r.transcribeMs,
            segments = r.segments,
        )
        val h = withContext(Dispatchers.IO) { HistoryStore.add(ctx, t) }
        Ads.onTranscriptionDone()
        _state.update { it.copy(phase = Phase.Idle, result = t, history = h) }
    }

    fun cancelWork() { cancel.set(true) }

    fun openHistory(t: Transcript) {
        if (work?.isActive == true) return
        stopPlayback()
        audio = null
        player.setSource(null)
        _state.update {
            it.copy(
                result = t, fileName = t.fileName, audioMs = t.durationMs, hasAudio = false,
                waveform = FloatArray(0), tab = Tab.TEXT, phase = Phase.Idle, positionMs = 0,
            )
        }
    }

    fun clearForNew() {
        stopPlayback()
        audio = null
        player.setSource(null)
        _state.update {
            it.copy(result = null, fileName = null, hasAudio = false, waveform = FloatArray(0), phase = Phase.Idle, positionMs = 0)
        }
    }

    // --- Oynatıcı ---
    fun togglePlay() {
        if (!state.value.hasAudio) return
        runCatching {
            player.toggle { _state.update { it.copy(playing = false, positionMs = 0) } }
        }.onFailure { toast("Ses çalınamadı"); return }
        val playing = player.isPlaying
        _state.update { it.copy(playing = playing) }
        ticker?.cancel()
        if (playing) ticker = viewModelScope.launch {
            while (isActive && player.isPlaying) {
                _state.update { it.copy(positionMs = player.positionMs) }
                delay(120)
            }
            _state.update { it.copy(playing = player.isPlaying) }
        }
    }

    fun seekTo(ms: Long) {
        player.seekTo(ms)
        _state.update { it.copy(positionMs = ms) }
    }

    private fun stopPlayback() {
        ticker?.cancel()
        player.release()
        _state.update { it.copy(playing = false) }
    }

    // --- Test araçları (Ayarlar içinde) ---
    fun runMlKitTest(advanced: Boolean) {
        val a = audio ?: run { toast("Önce bir ses seç"); return }
        viewModelScope.launch {
            log("ML Kit ${if (advanced) "ADVANCED" else "BASIC"} çalışıyor…")
            val r = MlKitEngine(ctx, advanced).transcribe(a, state.value.lang)
            withContext(Dispatchers.IO) { ResultLog.append(ctx, state.value.fileName ?: "?", r) }
            log(describe(r))
        }
    }

    fun deviceInfo(): String {
        WhisperEngine.ensureBackends(ctx)
        return DeviceInfo.summary(ctx) + "\nCPU backend: " + WhisperEngine.backendInfo +
            "\nwhisper thread: " + WhisperEngine.threadCount()
    }

    fun modelReady(m: WhisperModel) = ModelStore.isReady(ctx, m)

    fun deleteModel(m: WhisperModel) {
        ModelStore.file(ctx, m).delete()
        toast("${m.label} silindi")
    }

    private fun log(line: String) = _state.update { it.copy(testLog = (listOf(line) + it.testLog).take(12)) }

    private fun describe(r: EngineResult): String =
        if (r.error != null) "${r.engine} ${r.variant}: HATA — ${r.error}"
        else "${r.engine} ${r.variant}: ${"%.1f".format(r.transcribeMs / 1000.0)} sn (RTF ${"%.2f".format(r.rtf)}) — ${r.text.take(80)}"

    private fun copyToCache(uri: Uri, name: String): File {
        val ext = name.substringAfterLast('.', "bin").take(5)
        val out = File(ctx.cacheDir, "current_audio.$ext")
        ctx.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Dosya okunamadı" }
            out.outputStream().use { input.copyTo(it) }
        }
        return out
    }

    private fun displayName(uri: Uri): String =
        ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        } ?: uri.lastPathSegment ?: "ses"

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
