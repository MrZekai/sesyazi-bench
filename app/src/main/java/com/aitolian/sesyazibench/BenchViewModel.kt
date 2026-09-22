package com.aitolian.sesyazibench

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aitolian.sesyazibench.audio.AudioDecoder
import com.aitolian.sesyazibench.audio.DecodedAudio
import com.aitolian.sesyazibench.engine.EngineResult
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.MlKitEngine
import com.aitolian.sesyazibench.engine.ModelStore
import com.aitolian.sesyazibench.engine.TranscriptionEngine
import com.aitolian.sesyazibench.engine.WhisperEngine
import com.aitolian.sesyazibench.engine.WhisperModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val fileLabel: String? = null,
    val audio: DecodedAudio? = null,
    val decodeMs: Long = 0,
    val lang: Lang = Lang.TR,
    val model: WhisperModel = WhisperModel.BASE,
    val modelsReady: Set<WhisperModel> = emptySet(),
    val downloadProgress: Map<WhisperModel, Float> = emptyMap(),
    val busy: String? = null,
    val results: List<EngineResult> = emptyList(),
    val message: String? = null,
)

class BenchViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx get() = getApplication<Application>()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init { refreshModels() }

    private fun refreshModels() = _state.update { s ->
        s.copy(modelsReady = WhisperModel.entries.filter { ModelStore.isReady(ctx, it) }.toSet())
    }

    fun setLang(l: Lang) = _state.update { it.copy(lang = l) }
    fun setModel(m: WhisperModel) = _state.update { it.copy(model = m) }
    fun dismissMessage() = _state.update { it.copy(message = null) }

    fun load(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(busy = "Ses çözülüyor…", results = emptyList()) }
            try {
                val label = withContext(Dispatchers.IO) { displayName(uri) }
                val t0 = System.currentTimeMillis()
                val audio = withContext(Dispatchers.IO) { AudioDecoder.decode(ctx, uri) }
                _state.update {
                    it.copy(fileLabel = label, audio = audio, decodeMs = System.currentTimeMillis() - t0, busy = null)
                }
            } catch (t: Throwable) {
                _state.update { it.copy(busy = null, message = "Ses açılamadı: ${t.message}") }
            }
        }
    }

    fun download(m: WhisperModel) {
        viewModelScope.launch {
            _state.update { it.copy(downloadProgress = it.downloadProgress + (m to 0f)) }
            try {
                ModelStore.download(ctx, m) { p ->
                    _state.update { it.copy(downloadProgress = it.downloadProgress + (m to p)) }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(message = "İndirme hatası: ${t.message}") }
            }
            _state.update { it.copy(downloadProgress = it.downloadProgress - m) }
            refreshModels()
        }
    }

    fun runWhisper() = launchEngine(WhisperEngine(ctx, state.value.model))
    fun runMlKit(advanced: Boolean) = launchEngine(MlKitEngine(ctx, advanced))

    fun runAll() {
        viewModelScope.launch {
            val s = state.value
            val engines = buildList<TranscriptionEngine> {
                if (s.model in s.modelsReady) add(WhisperEngine(ctx, s.model))
                add(MlKitEngine(ctx, advanced = false))
                add(MlKitEngine(ctx, advanced = true))
            }
            for (e in engines) runNow(e)
        }
    }

    private fun launchEngine(engine: TranscriptionEngine) {
        viewModelScope.launch { runNow(engine) }
    }

    private suspend fun runNow(engine: TranscriptionEngine) {
        val s = state.value
        val audio = s.audio
        if (audio == null) {
            _state.update { it.copy(message = "Önce bir ses dosyası seç ya da paylaş") }
            return
        }
        _state.update { it.copy(busy = "${engine.name} çalışıyor…") }
        val result = try {
            engine.transcribe(audio, s.lang)
        } catch (t: Throwable) {
            EngineResult(engine.name, "", s.lang.code, null, audio.durationMs, 0, 0, "", error = t.toString())
        }
        withContext(Dispatchers.IO) { ResultLog.append(ctx, s.fileLabel ?: "?", result) }
        _state.update { it.copy(busy = null, results = listOf(result) + it.results) }
    }

    private fun displayName(uri: Uri): String =
        ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        } ?: uri.lastPathSegment ?: "ses"
}
