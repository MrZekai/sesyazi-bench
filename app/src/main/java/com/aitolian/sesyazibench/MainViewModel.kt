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
import com.aitolian.sesyazibench.data.SpeedStore
import com.aitolian.sesyazibench.data.Prefs
import com.aitolian.sesyazibench.data.VoiceNote
import com.aitolian.sesyazibench.data.VoiceNotes
import com.aitolian.sesyazibench.data.Transcript
import com.aitolian.sesyazibench.engine.EngineResult
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.MlKitEngine
import com.aitolian.sesyazibench.engine.ModelStore
import com.aitolian.sesyazibench.engine.OnDeviceTranslator
import com.aitolian.sesyazibench.engine.Segment
import com.aitolian.sesyazibench.engine.langOf
import com.aitolian.sesyazibench.audio.DecodeException
import java.util.Locale
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

/**
 * Kalite seviyeleri. Hepsi greedy (beam=1): gerçek cihaz testinde beam search +
 * sıcaklık yedeklemesi small modeli 36 sn ses için ~6 dk'ya çıkardı; doğruluk
 * kazancı bu maliyeti karşılamıyor.
 */
enum class Quality(val label: String, val model: WhisperModel, val beam: Int) {
    FAST("Hızlı", WhisperModel.BASE, 1),
    BALANCED("Dengeli", WhisperModel.SMALL, 1),
    BEST("En iyi", WhisperModel.TURBO, 1),
}

sealed interface Phase {
    data object Idle : Phase
    data class Preparing(val message: String) : Phase
    data class Downloading(val progress: Float) : Phase
    data class Transcribing(val percent: Int) : Phase
    data class Failed(val message: String) : Phase
}

enum class Tab { TEXT, TRANSLATION }

data class MainState(
    val phase: Phase = Phase.Idle,
    val lang: Lang = Lang.AUTO,
    val quality: Quality = Quality.BALANCED,
    val fileName: String? = null,
    val audioMs: Long = 0,
    val waveform: FloatArray = FloatArray(0),
    val hasAudio: Boolean = false,
    val playing: Boolean = false,
    val positionMs: Long = 0,
    val result: Transcript? = null,
    val history: List<Transcript> = emptyList(),
    val tab: Tab = Tab.TEXT,
    val translation: List<Segment>? = null,
    val translationTarget: Lang = defaultTarget(null),
    val translating: String? = null,
    val toast: String? = null,
    val testLog: List<String> = emptyList(),
    /** Döküm sürerken çözülen cümleler (canlı akış). */
    val live: List<Segment> = emptyList(),
    /** Tahmini kalan süre (sn) — ölçülen cihaz hızına göre. */
    val etaSec: Int? = null,
    /** En iyi modda: önizleme gösterilirken arka planda iyileştirme durumu. */
    val refining: String? = null,
    /** Türkçe sonuç Dengeli/Hızlı ile çıktıysa "En iyi ile tekrar dene" önerisi. */
    val suggestBest: Boolean = false,
    /** Artınca UI uzun işlem için geçiş reklamı dener (tek seferlik olay sayacı). */
    val adRequest: Int = 0,
    /** WhatsApp sesli mesaj klasörüne izin verildi mi, son sesli mesajlar. */
    val waGranted: Boolean = false,
    val voiceNotes: List<VoiceNote> = emptyList(),
    val voiceNotesLoading: Boolean = false,
    /** Ayarlar ekranındaki model indirmeleri (0..1). */
    val modelDownloads: Map<WhisperModel, Float> = emptyMap(),
)

/** Çeviri hedefi: telefonun dili; kaynak zaten o dilse İngilizce (kaynak İngilizceyse Türkçe). */
fun defaultTarget(source: String?): Lang {
    val device = langOf(Locale.getDefault().language)?.takeIf { it != Lang.AUTO }
    return when {
        device != null && device.code != source -> device
        source != Lang.EN.code -> Lang.EN
        else -> Lang.TR
    }
}

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx get() = getApplication<Application>()
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    private val player = Player()
    private var audio: DecodedAudio? = null
    private var work: Job? = null
    private var ticker: Job? = null
    private val cancel = AtomicBoolean(false)
    private var vadTried = false

    val prefs = Prefs(app)

    init {
        // Kayıtlı tercihler; hiç seçilmemişse yavaş telefonda (≤2 güçlü çekirdek) varsayılan Hızlı
        val q = prefs.defaultQuality?.let { n -> Quality.entries.firstOrNull { it.name == n } }
            ?: if (WhisperEngine.threadCount() <= 2) Quality.FAST else Quality.BALANCED
        val l = prefs.defaultLang?.let { langOf(it) } ?: Lang.AUTO
        val target = prefs.translateTarget?.let { langOf(it) }
        _state.update { it.copy(quality = q, lang = l, translationTarget = target ?: it.translationTarget) }
        viewModelScope.launch(Dispatchers.IO) {
            val h = HistoryStore.load(ctx)
            _state.update { it.copy(history = h) }
        }
        refreshVoiceNotes()
    }

    fun setLang(l: Lang) = _state.update { it.copy(lang = l) }
    fun setQuality(q: Quality) = _state.update { it.copy(quality = q) }

    // --- Ayarlar (kalıcı varsayılanlar) ---
    fun setDefaultLang(l: Lang) { prefs.defaultLang = l.code; setLang(l) }
    fun setDefaultQuality(q: Quality) { prefs.defaultQuality = q.name; setQuality(q) }
    fun setDefaultTarget(l: Lang) {
        prefs.translateTarget = l.code
        _state.update { it.copy(translationTarget = l) }
    }

    fun downloadModel(m: WhisperModel) {
        if (m in state.value.modelDownloads) return
        viewModelScope.launch {
            _state.update { it.copy(modelDownloads = it.modelDownloads + (m to 0f)) }
            val ok = runCatching {
                ModelStore.download(ctx, m) { p -> _state.update { it.copy(modelDownloads = it.modelDownloads + (m to p)) } }
            }.isSuccess
            _state.update { it.copy(modelDownloads = it.modelDownloads - m, toast = if (ok) "${m.approxMb} MB model hazır" else "İndirilemedi, interneti kontrol et") }
        }
    }

    // --- WhatsApp sesli mesajları ---
    fun onWhatsAppFolderPicked(tree: Uri) {
        runCatching { VoiceNotes.persist(ctx, tree) }
        prefs.whatsappTree = tree
        refreshVoiceNotes(showEmptyHint = true)
    }

    fun revokeWhatsApp() {
        prefs.whatsappTree?.let { VoiceNotes.release(ctx, it) }
        prefs.whatsappTree = null
        _state.update { it.copy(waGranted = false, voiceNotes = emptyList()) }
    }

    fun refreshVoiceNotes(showEmptyHint: Boolean = false) {
        val tree = prefs.whatsappTree
        if (tree == null || !VoiceNotes.hasAccess(ctx, tree)) {
            _state.update { it.copy(waGranted = false, voiceNotes = emptyList()) }
            return
        }
        _state.update { it.copy(waGranted = true, voiceNotesLoading = true) }
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { VoiceNotes.list(ctx, tree) }
            _state.update {
                it.copy(
                    voiceNotes = list, voiceNotesLoading = false,
                    toast = if (showEmptyHint && list.isEmpty())
                        "Bu klasörde sesli mesaj bulunamadı. \"WhatsApp Voice Notes\" klasörünü seçtiğinden emin ol."
                    else it.toast,
                )
            }
        }
    }
    fun setTab(t: Tab) = _state.update { it.copy(tab = t) }
    fun toastShown() = _state.update { it.copy(toast = null) }
    fun toast(msg: String) = _state.update { it.copy(toast = msg) }

    /** Ses seçildi / paylaşıldı → kopyala, çöz, otomatik yazıya dök. */
    fun onAudio(uri: Uri) {
        val prev = work
        if (prev?.isActive == true) {
            // Arka plandaki "iyileştirme" turu yeni sesi engellemesin: iptal et, bitmesini bekle
            if (state.value.refining != null) cancel.set(true)
            else { toast("Önce mevcut işlem bitsin"); return }
        }
        stopPlayback()
        work = viewModelScope.launch {
            prev?.join()
            _state.update {
                it.copy(phase = Phase.Preparing("Ses hazırlanıyor…"), result = null, tab = Tab.TEXT, translation = null)
            }
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
            } catch (t: DecodeException) {
                _state.update { it.copy(phase = Phase.Failed(t.message ?: "Ses açılamadı")) }
            } catch (t: OutOfMemoryError) {
                _state.update { it.copy(phase = Phase.Failed("Dosya bu telefon için çok büyük")) }
            } catch (t: Throwable) {
                _state.update { it.copy(phase = Phase.Failed("Ses açılamadı: ${t.message ?: t.javaClass.simpleName}")) }
            }
        }
    }

    /** Dil veya kalite değişince aynı sesi yeniden dökmek için. */
    fun retranscribe() {
        if (work?.isActive == true) return
        if (audio == null) {
            if (state.value.result != null) toast("Yeni ayar bir sonraki seste geçerli olacak")
            return
        }
        work = viewModelScope.launch { transcribeCurrent() }
    }

    /** İşlemi şu kadar sürecekse bekleme ekranına geçiş reklamı konabilir. */
    private val adWorthyMs = 8_000L

    private suspend fun ensureModel(model: WhisperModel, onProgress: (Float) -> Unit): Boolean {
        if (!vadTried && !ModelStore.vadReady(ctx) && ModelStore.isReady(ctx, model)) {
            vadTried = true
            ModelStore.ensureVad(ctx)
        }
        if (ModelStore.isReady(ctx, model)) return true
        return try {
            ModelStore.download(ctx, model, onProgress)
            true
        } catch (t: Throwable) {
            false
        }
    }

    /** Tek motor çalıştırması; canlı cümleleri state.live'a akıtır. */
    private suspend fun runWhisper(a: DecodedAudio, model: WhisperModel, beam: Int, lang: Lang, stream: Boolean): EngineResult {
        val r = WhisperEngine(ctx, model, beam).transcribe(
            a, lang,
            onProgress = { p ->
                _state.update {
                    if (it.refining != null) it.copy(refining = "✨ En iyi model ile iyileştiriliyor… %$p")
                    else it.copy(phase = Phase.Transcribing(p.coerceIn(0, 100)))
                }
            },
            cancel = cancel,
            onSegment = { seg -> if (stream) _state.update { it.copy(live = it.live + seg) } },
        )
        if (r.error == null) SpeedStore.record(ctx, model, r.transcribeMs, a.durationMs)
        withContext(Dispatchers.IO) { ResultLog.append(ctx, state.value.fileName ?: "?", r) }
        return r
    }

    private suspend fun transcribeCurrent() {
        val a = audio ?: return
        val s = state.value
        val best = s.quality == Quality.BEST
        // En iyi modda önce hızlı önizleme (base), sonra arka planda turbo ile iyileştirme
        val first = if (best) Quality.FAST else s.quality

        if (!ModelStore.isReady(ctx, first.model)) {
            _state.update { it.copy(phase = Phase.Downloading(0f)) }
            if (!ensureModel(first.model) { p -> _state.update { it.copy(phase = Phase.Downloading(p)) } }) {
                _state.update { it.copy(phase = Phase.Failed("Model indirilemedi. İnterneti kontrol et.")) }
                return
            }
        } else ensureModel(first.model) {}

        cancel.set(false)
        val eta = SpeedStore.estimateMs(ctx, first.model, a.durationMs)
        _state.update {
            it.copy(
                phase = Phase.Transcribing(0), live = emptyList(), etaSec = (eta / 1000).toInt(),
                refining = null, suggestBest = false,
                adRequest = if (eta >= adWorthyMs) it.adRequest + 1 else it.adRequest,
            )
        }
        val r = runWhisper(a, first.model, first.beam, s.lang, stream = true)
        if (r.error != null) {
            _state.update { it.copy(phase = if (cancel.get()) Phase.Idle else Phase.Failed(r.error), live = emptyList(), etaSec = null) }
            return
        }
        if (r.segments.isEmpty()) {
            _state.update {
                it.copy(
                    phase = Phase.Failed("Konuşma algılanamadı. Seste net konuşma yoksa (ör. müzik) metin çıkmaz."),
                    live = emptyList(), etaSec = null,
                )
            }
            return
        }
        val detected = r.detectedLanguage ?: s.lang.code
        var t = Transcript(
            id = System.currentTimeMillis(),
            fileName = s.fileName ?: "ses",
            durationMs = a.durationMs,
            language = detected,
            processMs = r.transcribeMs,
            segments = r.segments,
        )
        var h = withContext(Dispatchers.IO) { HistoryStore.add(ctx, t) }
        Ads.onTranscriptionDone()
        _state.update {
            it.copy(
                phase = Phase.Idle, result = t, history = h, live = emptyList(), etaSec = null,
                translation = null, translating = null, translationTarget = targetFor(t.language),
                refining = if (best) "✨ En iyi model hazırlanıyor…" else null,
                suggestBest = !best && detected == Lang.TR.code,
            )
        }

        if (!best) {
            Notifier.notifyDone(ctx, t.text.take(120))
            return
        }

        // --- İyileştirme turu (En iyi) ---
        val ok = ensureModel(Quality.BEST.model) { p ->
            _state.update { it.copy(refining = "✨ En iyi model indiriliyor… %${(p * 100).toInt()}") }
        }
        if (!ok) {
            _state.update { it.copy(refining = null, toast = "En iyi model indirilemedi; hızlı sonuç gösteriliyor") }
            Notifier.notifyDone(ctx, t.text.take(120))
            return
        }
        _state.update { it.copy(refining = "✨ En iyi model ile iyileştiriliyor… %0") }
        // Dil önizlemede algılandı; aynı dili zorla ki iki tur tutarlı olsun
        val refineLang = langOf(detected)?.takeIf { it != Lang.AUTO } ?: s.lang
        val r2 = runWhisper(a, Quality.BEST.model, Quality.BEST.beam, refineLang, stream = false)
        if (r2.error != null || r2.segments.isEmpty()) {
            _state.update { it.copy(refining = null) }
            if (!cancel.get() && r2.error != null) toast("İyileştirme yapılamadı; hızlı sonuç gösteriliyor")
            Notifier.notifyDone(ctx, t.text.take(120))
            return
        }
        t = t.copy(segments = r2.segments, processMs = r.transcribeMs + r2.transcribeMs)
        h = withContext(Dispatchers.IO) { HistoryStore.add(ctx, t) }
        _state.update {
            if (it.result?.id == t.id) it.copy(result = t, history = h, refining = null, translation = null, toast = "✨ Metin iyileştirildi")
            else it.copy(history = h, refining = null)
        }
        Notifier.notifyDone(ctx, t.text.take(120))
    }

    /** Türkçe öneri kartındaki "En iyi ile tekrar". */
    fun rerunWithBest() {
        setQuality(Quality.BEST)
        retranscribe()
    }

    /** Kayıtlı hedef dil kaynakla aynı değilse onu kullan, değilse akıllı varsayılan. */
    private fun targetFor(source: String): Lang =
        prefs.translateTarget?.let { langOf(it) }?.takeIf { it.code != source } ?: defaultTarget(source)

    fun cancelWork() {
        cancel.set(true)
        _state.update { it.copy(refining = null) }
    }

    fun openHistory(t: Transcript) {
        if (work?.isActive == true) {
            if (state.value.refining != null) cancelWork() else return
        }
        stopPlayback()
        audio = null
        player.setSource(null)
        _state.update {
            it.copy(
                result = t, fileName = t.fileName, audioMs = t.durationMs, hasAudio = false,
                waveform = FloatArray(0), tab = Tab.TEXT, phase = Phase.Idle, positionMs = 0,
                translation = null, translating = null, translationTarget = targetFor(t.language),
                suggestBest = false,
            )
        }
    }

    fun clearForNew() {
        if (state.value.refining != null) cancelWork()
        stopPlayback()
        audio = null
        player.setSource(null)
        _state.update {
            it.copy(
                result = null, fileName = null, hasAudio = false, waveform = FloatArray(0), phase = Phase.Idle,
                positionMs = 0, translation = null, translating = null, live = emptyList(), suggestBest = false,
            )
        }
    }

    // --- Çeviri ---
    private var translateJob: Job? = null

    /** Çeviri sekmesini açar; henüz çeviri yoksa başlatır. */
    fun openTranslation() {
        setTab(Tab.TRANSLATION)
        if (state.value.translation == null && state.value.translating == null) translate(state.value.translationTarget)
    }

    fun translate(target: Lang) {
        val r = state.value.result ?: return
        val source = langOf(r.language) ?: run { toast("Kaynak dil tanınmadı"); return }
        if (source == target) {
            _state.update { it.copy(translationTarget = target, translation = r.segments, translating = null) }
            return
        }
        if (!OnDeviceTranslator.supports(source)) { toast("${source.label} için çeviri desteklenmiyor"); return }
        translateJob?.cancel()
        _state.update { it.copy(translationTarget = target, translation = null, translating = "Hazırlanıyor…") }
        translateJob = viewModelScope.launch {
            try {
                val out = OnDeviceTranslator.translate(r.segments, source, target) { msg ->
                    _state.update { it.copy(translating = msg) }
                }
                _state.update { if (it.result?.id == r.id) it.copy(translation = out, translating = null) else it }
            } catch (t: Throwable) {
                if (t is kotlinx.coroutines.CancellationException) throw t
                _state.update {
                    it.copy(translating = null, toast = "Çeviri yapılamadı: ${t.message ?: "internet bağlantısını kontrol et"}")
                }
            }
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
