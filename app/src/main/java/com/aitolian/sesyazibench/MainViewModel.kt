package com.aitolian.sesyazibench

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aitolian.sesyazibench.audio.AudioDecoder
import com.aitolian.sesyazibench.audio.DecodeException
import com.aitolian.sesyazibench.audio.DecodedAudio
import com.aitolian.sesyazibench.audio.Player
import com.aitolian.sesyazibench.audio.peaks
import com.aitolian.sesyazibench.data.Exports
import com.aitolian.sesyazibench.data.HistoryStore
import com.aitolian.sesyazibench.data.Prefs
import com.aitolian.sesyazibench.data.SpeedStore
import com.aitolian.sesyazibench.data.RangeWarning
import com.aitolian.sesyazibench.data.Transcript
import com.aitolian.sesyazibench.engine.EngineResult
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.MlKitEngine
import com.aitolian.sesyazibench.engine.ModelStore
import com.aitolian.sesyazibench.engine.OnDeviceTranslator
import com.aitolian.sesyazibench.engine.Postprocess
import com.aitolian.sesyazibench.engine.Segment
import com.aitolian.sesyazibench.engine.WhisperEngine
import com.aitolian.sesyazibench.engine.WhisperModel
import com.aitolian.sesyazibench.engine.WitEngine
import com.aitolian.sesyazibench.engine.langOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
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
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Kalite seviyeleri. Hepsi greedy (beam=1). Tekrar deneme (temperature
 * fallback) otomatikte Hızlı'da kapalı, Dengeli ve En iyi'de açık; bkz. fallbackFor.
 * En iyi'nin modeli deneyde q8_0 olabilir; bkz. modelFor.
 */
enum class Quality(val label: String, val model: WhisperModel, val beam: Int) {
    FAST("Hızlı", WhisperModel.BASE, 1),
    BALANCED("Dengeli", WhisperModel.SMALL, 1),
    BEST("En iyi", WhisperModel.TURBO, 1),
}

sealed interface Phase {
    data object Idle : Phase
    data class Preparing(val message: String) : Phase
    data class Downloading(val progress: Float, val mb: Int = 0) : Phase
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
    /** Yeniden döküm sürerken önceki sonuç: hata/iptalde geri gösterilir. */
    val previousResult: Transcript? = null,
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
    /** Artınca UI döküm başı geçiş reklamını dener (tek seferlik olay sayacı). */
    val adRequest: Int = 0,
    /** Ayarlar ekranındaki model indirmeleri (0..1). */
    val modelDownloads: Map<WhisperModel, Float> = emptyMap(),
    /** Son silinen not — "Geri al" için 5 sn tutulur. */
    val undoDeleted: Transcript? = null,
    /** Not ekranı yazı boyutu (sp). */
    val readerFont: Int = 19,
    /** Son dökümün aşama süreleri (geliştirici araçlarında gösterilir). */
    val lastTiming: String? = null,
    /** Son dökümün teşhisi (yalnız geliştirici modunda; metin/anahtar içermez). */
    val lastDiag: String? = null,
    /** Hızlı modda henüz kesinleşmemiş canlı ara metin (kelime kelime). */
    val livePartial: String? = null,
    /** Motor tercihi: 0 sorulmadı, 1 Hızlı (internet), 2 Gizli (telefonda). */
    val engineMode: Int = 0,
    /** Satır aralığı: 0 sıkı, 1 normal, 2 geniş. */
    val readerLine: Int = 1,
    /** Ses çalarken okunan cümleyi takip et. */
    val followAudio: Boolean = true,
    /** Tema: 0 sistem, 1 açık, 2 koyu. */
    val themeMode: Int = 0,
    /** İlk bulut aktarımından önce seçim penceresi açık mı. */
    val consentAsk: Boolean = false,
)

const val CONSENT_CANCEL = 0
const val CONSENT_CLOUD = 1
const val CONSENT_LOCAL = 2

/** Mono/kanal enerji oranı bunun altındaysa (zıt fazlı stereo) tek kanal kullanılır. */
private const val ANTI_PHASE_RATIO = 0.1

/** Wit.ai ile üretilen notların kalite etiketi. */
const val QUALITY_WIT = "WIT"
/** Wit + başarısız bölümleri telefonda tamamlanmış (ya da işaretlenmiş) not. */
const val QUALITY_WIT_MIX = "WIT_MIX"

/** Not internetle (Wit) mi üretildi? */
fun Transcript.isCloud(): Boolean = quality == QUALITY_WIT || quality == QUALITY_WIT_MIX

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

    val prefs = Prefs(app)
    private val player = Player()
    private var audio: DecodedAudio? = null
    private var ticker: Job? = null
    private var translateJob: Job? = null
    private var undoJob: Job? = null
    private var vadTried = false

    // ------------------------------------------------------------------
    // Oturumlar: her döküm (ve onun En iyi iyileştirmesi) bir oturumdur.
    // Yeni oturum eskisini iptal eder; iptal edilmiş/eski oturumun hiçbir
    // geri çağrısı (canlı cümle, ilerleme, sonuç, kayıt) duruma yazamaz.
    // ------------------------------------------------------------------
    private class Session(val id: Long) {
        val cancelled = AtomicBoolean(false)
        var job: Job? = null
        val startedAt: Long = SystemClock.elapsedRealtime()
        @Volatile var firstVisibleAt = 0L
        /** Bu oturumda reklam denendi mi (en fazla bir kez). */
        @Volatile var adRequested = false
    }

    private val sessionIds = AtomicLong(0)
    @Volatile private var session: Session? = null

    private fun Session.alive() = !cancelled.get() && session === this

    /** Yalnızca oturum hâlâ geçerliyse durumu değiştirir (CAS döngüsünde her denemede kontrol). */
    private inline fun Session.update(crossinline f: (MainState) -> MainState) =
        _state.update { if (alive()) f(it) else it }

    private fun newSession(): Session {
        session?.let { cancel(it) }
        return Session(sessionIds.incrementAndGet()).also { session = it }
    }

    private fun cancel(s: Session) {
        s.cancelled.set(true)   // native döküm abort geri çağrısıyla durur
        s.job?.cancel()         // indirme, bekleme ve kopyalama coroutine iptaliyle durur
        if (session === s) session = null
    }

    private fun cancelSession() { session?.let { cancel(it) } }

    val isWorking: Boolean get() = session?.job?.isActive == true

    init {
        val q = prefs.defaultQuality?.let { n -> Quality.entries.firstOrNull { it.name == n } }
            ?: if (WhisperEngine.threadCount() <= 2) Quality.FAST else Quality.BALANCED
        val l = prefs.defaultLang?.let { langOf(it) } ?: Lang.AUTO
        val target = prefs.translateTarget?.let { langOf(it) }
        WhisperEngine.threadOverride = if (prefs.devMode) prefs.threadOverride else 0
        _state.update {
            it.copy(
                quality = q, lang = l, translationTarget = target ?: it.translationTarget, readerFont = prefs.readerFont,
                readerLine = prefs.readerLine.coerceIn(0, 2), followAudio = prefs.followAudio,
                themeMode = prefs.themeMode.coerceIn(0, 2),
                engineMode = if (WitEngine.tokens.isEmpty()) 2 else prefs.engineMode.takeIf { it != 0 } ?: 1,
            )
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { ModelStore.cleanupLegacy(ctx) }
            val h = HistoryStore.load(ctx)
            _state.update { it.copy(history = h) }
        }
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

    fun setThreadOverride(n: Int) {
        prefs.threadOverride = n
        WhisperEngine.threadOverride = n
        toast(if (n == 0) "Thread: otomatik (${WhisperEngine.autoThreads})" else "Thread: $n")
    }

    fun setReaderFont(sp: Int) {
        val v = sp.coerceIn(14, 30)
        prefs.readerFont = v
        _state.update { it.copy(readerFont = v) }
    }

    fun setReaderLine(v: Int) {
        val x = v.coerceIn(0, 2)
        prefs.readerLine = x
        _state.update { it.copy(readerLine = x) }
    }

    fun setFollowAudio(on: Boolean) {
        prefs.followAudio = on
        _state.update { it.copy(followAudio = on) }
    }

    fun setThemeMode(m: Int) {
        val x = m.coerceIn(0, 2)
        prefs.themeMode = x
        _state.update { it.copy(themeMode = x) }
    }

    // ------------------------------------------------------------------
    // Motor tercihi: Hızlı (internet, Wit.ai) / Gizli (telefonda, Whisper)
    // ------------------------------------------------------------------
    /** Hızlı mod bu derlemede kullanılabilir mi (en az bir dil anahtarı var mı)? */
    val cloudAvailable: Boolean get() = WitEngine.tokens.isNotEmpty()

    /**
     * @param grantConsent Ayarlar'da, aktarım açıklaması görülerek Hızlı seçildiyse true
     *   (ilk aktarım penceresi bir daha sorulmaz).
     */
    fun setEngineMode(m: Int, grantConsent: Boolean = false) {
        if (m == 1 && grantConsent) prefs.cloudConsent = CONSENT_CLOUD
        prefs.engineMode = m
        _state.update { it.copy(engineMode = m) }
        // Hızlı modda dil, küçük modelle telefonda bulunur: Wi‑Fi'deyse şimdiden indir
        if (m == 1) prefetchDetector(Lang.AUTO)
    }

    /**
     * Uygulama Wit tabanlı: varsayılan her zaman Hızlı mod (sorulmaz).
     * Kullanıcı Ayarlar'dan "Telefonda"yı seçtiyse (2) yalnızca cihazda çalışır.
     */
    private fun engineChoice(): Int = if (!cloudAvailable) 2 else prefs.engineMode.takeIf { it != 0 } ?: 1

    private fun online(): Boolean {
        val cm = ctx.getSystemService(android.net.ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        // VALIDATED: "bağlı ama internet yok" (ör. giriş sayfalı Wi‑Fi) durumunu eler
        return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun downloadModel(m: WhisperModel) {
        if (m in state.value.modelDownloads) return
        warnIfMetered(m)
        viewModelScope.launch {
            _state.update { it.copy(modelDownloads = it.modelDownloads + (m to 0f)) }
            val err = runCatching {
                ModelStore.download(ctx, m) { p -> _state.update { it.copy(modelDownloads = it.modelDownloads + (m to p)) } }
            }.exceptionOrNull()
            _state.update {
                it.copy(
                    modelDownloads = it.modelDownloads - m,
                    toast = if (err == null) "${m.approxMb} MB model hazır" else "İndirilemedi: ${err.message ?: "interneti kontrol et"}",
                )
            }
        }
    }

    fun setTab(t: Tab) = _state.update { it.copy(tab = t) }
    fun toastShown() = _state.update { it.copy(toast = null) }
    fun toast(msg: String) = _state.update { it.copy(toast = msg) }

    /** Döküm başı geçiş reklamı hâlâ anlamlı mı? Metin ekrana geldiyse ya da iş bittiyse hayır. */
    fun adStillWanted(): Boolean {
        val s = state.value
        val cur = session ?: return false
        if (cur.firstVisibleAt != 0L || !s.livePartial.isNullOrBlank() || s.consentAsk) return false
        return isWorking && s.result == null && s.live.isEmpty() &&
            (s.phase is Phase.Transcribing || s.phase is Phase.Preparing || s.phase is Phase.Downloading)
    }

    // ------------------------------------------------------------------
    // Yeni ses
    // ------------------------------------------------------------------

    /** Ses seçildi / paylaşıldı → kopyala, çöz, otomatik yazıya dök. Önceki her işi iptal eder. */
    fun onAudio(uri: Uri) {
        val s = newSession()
        translateJob?.cancel()
        stopPlayback()
        audio = null            // eski ses: yeni dosya başarısız olursa "Tekrar dene" onu dökmesin
        player.setSource(null)
        _state.update {
            it.copy(
                phase = Phase.Preparing("Ses hazırlanıyor…"), result = null, previousResult = null,
                fileName = null, audioMs = 0, waveform = FloatArray(0), hasAudio = false, positionMs = 0,
                live = emptyList(), livePartial = null, tab = Tab.TEXT, translation = null, translating = null,
                refining = null, suggestBest = false, etaSec = null,
            )
        }
        s.job = viewModelScope.launch {
            try {
                val name = withContext(Dispatchers.IO) { displayName(uri) }
                val copy = withContext(Dispatchers.IO) { copyToCache(uri, name, s) }
                    ?: throw DecodeException("Dosya çok büyük (en fazla $MAX_FILE_MB MB)")
                var decoded = withContext(Dispatchers.IO) {
                    AudioDecoder.decode(ctx, Uri.fromFile(copy), { !s.alive() })
                }
                // Kanallar büyük ölçüde zıt fazlıysa ortalama sesi siler: yalnız sol kanalla
                // yeniden çöz (ölçülmüş, nadir durum; her dosyada "en yüksek kanal" seçilmez)
                val phase = decoded.info?.monoPhaseRatio ?: -1.0
                if (phase in 0.0..ANTI_PHASE_RATIO) {
                    decoded = withContext(Dispatchers.IO) {
                        AudioDecoder.decode(ctx, Uri.fromFile(copy), { !s.alive() }, channelPick = 0)
                    }
                }
                if (decoded.samples.size < AudioDecoder.TARGET_RATE / 2) {
                    throw DecodeException("Ses çok kısa (en az yarım saniye olmalı)")
                }
                val wave = withContext(Dispatchers.Default) { decoded.peaks() }
                if (!s.alive()) return@launch
                audio = decoded
                player.setSource(copy)
                if (prefs.devMode) _state.update { it.copy(lastDiag = Diagnostics.audio(decoded)) }
                s.update {
                    it.copy(fileName = name, audioMs = decoded.durationMs, waveform = wave, hasAudio = true, positionMs = 0)
                }
                transcribe(s, decoded, importMs = SystemClock.elapsedRealtime() - s.startedAt)
            } catch (t: CancellationException) {
                throw t
            } catch (t: DecodeException) {
                s.update { it.copy(phase = Phase.Failed(t.message ?: "Ses açılamadı")) }
            } catch (t: OutOfMemoryError) {
                s.update { it.copy(phase = Phase.Failed("Dosya bu telefon için çok büyük")) }
            } catch (t: Throwable) {
                s.update { it.copy(phase = Phase.Failed("Ses açılamadı: ${t.message ?: t.javaClass.simpleName}")) }
            }
        }
    }

    /** Hata ekranındaki "Tekrar dene": elde geçerli bir ses varsa aynı sesi yeniden döker. */
    fun canRetry() = audio != null

    /**
     * Aynı sesi (başka dil/kalite ile) yeniden döker. Önceki sonuç
     * [MainState.previousResult] olarak saklanır; yeni canlı metin görünür,
     * hata ya da iptalde önceki not geri gelir.
     */
    fun retranscribe(forceLocal: Boolean = false) {
        val a = audio ?: run {
            if (state.value.result != null) toast("Bu notun sesi artık yok; yeni ayar bir sonraki seste geçerli olur")
            return
        }
        val s = newSession()
        translateJob?.cancel()
        _state.update {
            it.copy(
                previousResult = it.result ?: it.previousResult, result = null, live = emptyList(), livePartial = null, etaSec = null,
                phase = Phase.Preparing("Hazırlanıyor…"), tab = Tab.TEXT, translation = null, translating = null,
                refining = null, suggestBest = false,
            )
        }
        s.job = viewModelScope.launch {
            try {
                transcribe(s, a, importMs = 0, forceLocal = forceLocal)
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                fail(s, "Döküm yapılamadı: ${t.message ?: t.javaClass.simpleName}")
            }
        }
    }

    /** Dil değişiminde açık notun kalitesini koru; genel seçim eski kalmış olabilir. */
    fun retranscribeInLanguage(lang: Lang) {
        val st = state.value
        val quality = Quality.entries.firstOrNull { it.name == st.result?.quality } ?: st.quality
        // Telefonda üretilmiş not telefonda kalır (motor sessizce değişmez)
        val noteWasLocal = st.result?.let { !it.isCloud() && it.quality != null } == true
        _state.update { it.copy(lang = lang, quality = quality) }
        retranscribe(forceLocal = noteWasLocal)
    }

    /** Not menüsündeki "X kalite ile yeniden dök": her zaman telefonda (Whisper). */
    fun retranscribeLocal(q: Quality) {
        setQuality(q)
        retranscribe(forceLocal = true)
    }

    /** Hata: önceki not varsa ona dön (not ekranında kal), yoksa hata ekranı. */
    private fun fail(s: Session, message: String) {
        s.update {
            val prev = it.previousResult
            if (prev != null) it.copy(result = prev, previousResult = null, phase = Phase.Idle, live = emptyList(), livePartial = null, etaSec = null, toast = message)
            else it.copy(phase = Phase.Failed(message), live = emptyList(), livePartial = null, etaSec = null)
        }
    }

    private suspend fun ensureModel(model: WhisperModel, onProgress: (Float) -> Unit): Boolean {
        if (!vadTried && !ModelStore.vadReady(ctx) && ModelStore.isReady(ctx, model)) {
            vadTried = true
            ModelStore.ensureVad(ctx)
        }
        if (ModelStore.isReady(ctx, model)) return true
        return try {
            ModelStore.download(ctx, model, onProgress)
            true
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            false
        }
    }

    /** Tek motor çalıştırması; canlı cümleleri state.live'a akıtır. */
    private suspend fun runWhisper(
        s: Session, a: DecodedAudio, model: WhisperModel, beam: Int, fallback: Boolean, lang: Lang, stream: Boolean,
    ): EngineResult {
        val r = WhisperEngine(ctx, model, beam, fallback).transcribe(
            a, lang,
            onProgress = { p ->
                s.update {
                    if (it.refining != null) it.copy(refining = "✨ En iyi model ile iyileştiriliyor… %$p")
                    else it.copy(phase = Phase.Transcribing(p.coerceIn(0, 100)))
                }
            },
            cancel = s.cancelled,
            onSegment = { seg ->
                if (stream) {
                    if (s.firstVisibleAt == 0L) s.firstVisibleAt = SystemClock.elapsedRealtime()
                    s.update { it.copy(live = it.live + seg) }
                }
            },
        )
        if (r.error == null) SpeedStore.record(ctx, model, r.transcribeMs, a.durationMs)
        return r
    }

    private fun logTiming(
        s: Session, r: EngineResult, mode: String, pass: String, fallback: Boolean, importMs: Long, cleanCount: Int,
    ) {
        val first = if (s.firstVisibleAt > 0) s.firstVisibleAt - s.startedAt else -1L
        val total = SystemClock.elapsedRealtime() - s.startedAt
        val line = ResultLog.summary(r, mode, pass, importMs, first, total, fallback, cleanCount)
        _state.update { it.copy(lastTiming = line) }
        if (prefs.devMode) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching { ResultLog.append(ctx, r, mode, pass, fallback, importMs, first, total, cleanCount) }
            }
        }
    }

    // ------------------------------------------------------------------
    // Model ve çözme ayarları (geliştirici deneyleri dahil)
    // ------------------------------------------------------------------

    /** Kalitenin kullanacağı model; En iyi için deneyde q8_0 seçilebilir. */
    private fun modelFor(q: Quality): WhisperModel =
        if (q == Quality.BEST && prefs.devMode && prefs.turboQ8) WhisperModel.TURBO_Q8 else q.model

    /**
     * Tekrar deneme (temperature fallback). Otomatik: Hızlı'da kapalı (hız),
     * Dengeli ve En iyi'de açık (whisper'ın varsayılan doğruluk davranışı).
     * Geliştirici araçlarından hep açık / hep kapalı yapılabilir (A/B ölçümü).
     */
    private fun fallbackFor(q: Quality): Boolean = when (if (prefs.devMode) prefs.fallbackMode else 0) {
        1 -> true
        2 -> false
        else -> q != Quality.FAST
    }

    private suspend fun ensureOrFail(s: Session, model: WhisperModel): Boolean {
        if (ModelStore.isReady(ctx, model)) { ensureModel(model) {}; return true }
        warnIfMetered(model)
        val mb = model.approxMb
        s.update { it.copy(phase = Phase.Downloading(0f, mb)) }
        if (!ensureModel(model) { p -> s.update { it.copy(phase = Phase.Downloading(p, mb)) } }) {
            fail(s, "Model indirilemedi. İnterneti ve boş alanı kontrol et.")
            return false
        }
        return true
    }

    /** Telefonda hazır (indirilmiş) en uygun kalite: tercih → Dengeli → Hızlı; hiçbiri yoksa null. */
    private fun localReadyQuality(pref: Quality): Quality? =
        listOf(pref, Quality.BALANCED, Quality.FAST).distinct().firstOrNull { ModelStore.isReady(ctx, modelFor(it)) }

    private fun metered(): Boolean =
        ctx.getSystemService(android.net.ConnectivityManager::class.java)?.isActiveNetworkMetered != false

    /** Oturum başına en fazla bir reklam denemesi; metin görünmeye başladıysa hiç. */
    private fun requestAd(s: Session) {
        if (s.adRequested || s.firstVisibleAt != 0L) return
        s.adRequested = true
        s.update { it.copy(adRequest = it.adRequest + 1) }
    }

    private suspend fun transcribe(s: Session, a: DecodedAudio, importMs: Long, forceLocal: Boolean = false) {
        // İlk bulut aktarımından ÖNCE açık seçim (Play kullanıcı verisi politikası):
        // seçim yapılmadan ses gönderilmez; kapatma/geri kabul sayılmaz.
        if (!forceLocal && cloudAvailable && engineChoice() == 1 && online() && prefs.cloudConsent != CONSENT_CLOUD) {
            when (askCloudConsent(s)) {
                CONSENT_CLOUD -> prefs.cloudConsent = CONSENT_CLOUD
                CONSENT_LOCAL -> {
                    setEngineMode(2)
                    transcribe(s, a, importMs, forceLocal = true)
                    return
                }
                else -> {
                    abandon(s, "Vazgeçildi; ses gönderilmedi")
                    return
                }
            }
            if (!s.alive()) return
        }
        // Hızlı mod: önce internet (Wit.ai); olmazsa aşağıda telefonda devam eder
        var cloudFailed = false
        if (!forceLocal && cloudAvailable && engineChoice() == 1) {
            cloudFailed = !online() || transcribeCloud(s, a, importMs) == CloudResult.FALLBACK
            if (!cloudFailed || !s.alive()) return
        }
        val st = state.value
        var best = st.quality == Quality.BEST
        var first: Quality? = null
        if (cloudFailed) {
            // İnternet yolu olmadı: sürpriz büyük indirme başlatma. Telefonda hazır model
            // varsa onunla; yoksa yalnızca ölçümsüz (Wi‑Fi) ağda indir, değilse açıkça söyle.
            val ready = localReadyQuality(st.quality)
            if (ready == null && (!online() || metered())) {
                fail(
                    s,
                    if (!online()) "İnternet yok ve telefonda dil modeli yok. İnternete bağlanıp tekrar dene."
                    else "İnternetle yazıya dökülemedi. Telefonda dökmek için Wi‑Fi'ye bağlan ya da Ayarlar > Depolama'dan model indir.",
                )
                return
            }
            first = ready ?: Quality.FAST
            best = false
            toast(if (!online()) "İnternet yok; telefonda yazıya dökülüyor" else "İnternetle yapılamadı; telefonda yazıya dökülüyor")
        }
        // En iyi: varsayılan olarak önce ön izleme (Dengeli hazırsa onunla, değilse Hızlı),
        // sonra arka planda büyük modelle iyileştirme. Deneyde ön izleme kapatılabilir:
        // büyük model doğrudan ve tek tur çalışır.
        val withPreview = best && (!prefs.devMode || prefs.bestPreview)
        val q = first ?: when {
            !best -> st.quality
            !withPreview -> Quality.BEST
            ModelStore.isReady(ctx, Quality.BALANCED.model) -> Quality.BALANCED
            else -> Quality.FAST
        }
        val firstModel = modelFor(q)
        val firstFallback = fallbackFor(q)

        if (!ensureOrFail(s, firstModel)) return
        if (!s.alive()) return

        val eta = SpeedStore.estimateMs(ctx, firstModel, a.durationMs)
        s.update {
            it.copy(
                phase = Phase.Transcribing(0), live = emptyList(), livePartial = null, etaSec = (eta / 1000).toInt(),
                refining = null, suggestBest = false,
            )
        }
        // Metin ekrana gelmeden önce geçiş reklamı (oturumda bir kez); döküm arkada sürer
        requestAd(s)
        val r = runWhisper(s, a, firstModel, q.beam, fallback = firstFallback, lang = st.lang, stream = true)
        if (!s.alive()) return
        val segs = Postprocess.clean(r.segments)
        logTiming(
            s, r, q.label, if (cloudFailed) "yedek" else if (withPreview) "onizleme" else "tek", fallback = firstFallback,
            importMs = importMs, cleanCount = segs.size,
        )
        if (r.error != null) { fail(s, r.error); return }
        if (segs.isEmpty()) {
            fail(s, "Konuşma algılanamadı. Seste net konuşma yoksa (ör. müzik) metin çıkmaz.")
            return
        }
        val detected = r.detectedLanguage ?: st.lang.code
        val t = Transcript(
            id = System.currentTimeMillis(),
            fileName = st.fileName ?: state.value.fileName ?: "ses",
            durationMs = a.durationMs,
            language = detected,
            processMs = r.transcribeMs,
            segments = segs,
            quality = q.name,
        )
        if (!s.alive()) return
        val h = HistoryStore.add(ctx, t)
        s.update {
            it.copy(
                phase = Phase.Idle, result = t, previousResult = null, history = h, live = emptyList(),
                livePartial = null, etaSec = null,
                translation = null, translating = null, translationTarget = targetFor(t.language),
                refining = if (withPreview) "✨ Ön izleme (${q.label}) · En iyi sonuç hazırlanıyor…" else null,
                suggestBest = q != Quality.BEST && detected == Lang.TR.code,
            )
        }
        prefetchDetector(st.lang)
        if (!withPreview) {
            Notifier.notifyDone(ctx, t.text.take(120))
            return
        }
        refine(s, a, t, fallbackLang = st.lang, importMs = importMs)
    }

    private enum class CloudResult { DONE, FALLBACK }

    /**
     * Hızlı mod dökümü. DONE → iş bitti (başarılı, iptal ya da kesin "konuşma yok");
     * FALLBACK → telefonda (Whisper) devam edilmeli (anahtar yok, tüm parçalar başarısız).
     * Bazı parçalar başarısızsa başarılılar korunur; eksik aralıklar telefonda
     * tamamlanır (model hazırsa) ya da notta açıkça işaretlenir — eksik metin tam
     * gibi kaydedilmez.
     */
    private suspend fun transcribeCloud(s: Session, a: DecodedAudio, importMs: Long): CloudResult {
        val st = state.value
        // Gösterilen süre: dil bulma + internet + gerekirse telefonda tamamlama (monoton saat)
        val processingStartedAt = SystemClock.elapsedRealtime()
        // Dil: seçiliyse o; otomatikse küçük modelle telefonda bulunur. Küçük model
        // yoksa telefonun dili (Wit'te varsa), o da yoksa İngilizce VARSAYILIR (kullanıcıya söylenir).
        var detectMs = 0L
        var detectPath = "secili"
        var lang = st.lang.takeIf { it != Lang.AUTO }?.code
        if (lang == null) {
            s.update { it.copy(phase = Phase.Preparing("Dil algılanıyor…"), livePartial = null) }
            val t0 = SystemClock.elapsedRealtime()
            lang = WhisperEngine.detectLanguage(ctx, a)
            detectMs = SystemClock.elapsedRealtime() - t0
            detectPath = if (lang != null) "base" else "telefon_dili"
            if (lang == null) {
                prefetchDetector(Lang.AUTO)
                lang = Locale.getDefault().language.takeIf { WitEngine.supports(it) } ?: "en"
            }
        }
        if (!s.alive()) return CloudResult.DONE
        val wit = WitEngine.forLang(lang) ?: return CloudResult.FALLBACK // bu dil için anahtar yok

        s.update {
            it.copy(
                phase = Phase.Transcribing(-1), live = emptyList(), livePartial = null, etaSec = null,
                refining = null, suggestBest = false,
            )
        }
        requestAd(s)
        val oc = wit.transcribe(
            a, lang, s.cancelled,
            onPartial = { p ->
                if (p != null && s.firstVisibleAt == 0L) s.firstVisibleAt = SystemClock.elapsedRealtime()
                s.update { it.copy(livePartial = p) }
            },
            onSegment = { seg ->
                if (s.firstVisibleAt == 0L) s.firstVisibleAt = SystemClock.elapsedRealtime()
                s.update { it.copy(live = it.live + seg, livePartial = null) }
            },
        )
        val r = oc.result.copy(detectMs = detectMs, detectPath = detectPath)
        if (!s.alive() || r.error == WhisperEngine.CANCELLED) return CloudResult.DONE
        // Wit çıktısına Whisper'ın "uydurma cümle" filtresi UYGULANMAZ (gerçek konuşmayı silebilir)
        var segs = r.segments.mapNotNull { seg -> seg.text.trim().takeIf { it.isNotEmpty() }?.let { seg.copy(text = it) } }
        logTiming(s, r, "Hızlı", "internet", fallback = false, importMs = importMs, cleanCount = segs.size)
        if (prefs.devMode) _state.update { it.copy(lastDiag = Diagnostics.audio(a) + "\n" + Diagnostics.wit(lang, oc, emptyMap())) }

        // Hiçbir parça başarılı olmadı (ya da anahtar geçersiz) → tamamen telefonda
        if (r.error != null || oc.authFailed) {
            s.update { it.copy(live = emptyList(), livePartial = null) }
            return CloudResult.FALLBACK
        }
        var mixed = false
        val fills = mutableMapOf<Int, String>()
        if (oc.failed.isNotEmpty()) {
            mixed = true
            val q = localReadyQuality(st.quality)
            val langObj = langOf(lang) ?: Lang.AUTO
            for (f in oc.failed) {
                if (!s.alive()) return CloudResult.DONE
                val fromMs = f.from * 1000L / AudioDecoder.TARGET_RATE
                val toMs = f.to * 1000L / AudioDecoder.TARGET_RATE
                var filled: List<Segment>? = null
                if (q != null) {
                    val sub = a.slice(f.from, f.to)
                    val lr = runWhisper(s, sub, modelFor(q), q.beam, fallbackFor(q), langObj, stream = false)
                    if (lr.error == null) {
                        // Yerel döküm boş dönerse boşluk sessizce kaybolmasın: işaret konur
                        filled = Postprocess.clean(lr.segments).map { it.copy(startMs = it.startMs + fromMs, endMs = it.endMs + fromMs) }
                            .takeIf { it.isNotEmpty() }
                    }
                }
                fills[f.from] = when {
                    filled != null -> "telefonda tamamlandı (${q?.label}, ${filled.size} parça)"
                    q == null -> "işaretlendi (hazır yerel model yok; indirme yapılmadı)"
                    else -> "işaretlendi (yerel döküm başarısız ya da boş)"
                }
                segs = segs + (filled ?: listOf(
                    Segment(fromMs, toMs, "[⚠ ${Transcript.clock(fromMs)}–${Transcript.clock(toMs)} arası yazıya dökülemedi]"),
                ))
            }
            segs = segs.sortedBy { it.startMs }
        }
        if (!s.alive()) return CloudResult.DONE
        if (prefs.devMode) {
            val d = Diagnostics.audio(a) + "\n" + Diagnostics.wit(lang, oc, fills)
            _state.update { it.copy(lastDiag = d) }
        }
        if (segs.isEmpty()) {
            // Dil tahmin edildiyse yanlış dilin Wit uygulamasına gitmiş olabilir → telefonda dene
            if (detectPath == "telefon_dili") return CloudResult.FALLBACK
            // Yeniden dökümde önceki not korunur (fail önceki nota döner)
            fail(s, "Konuşma algılanamadı. Seste net konuşma yoksa (ör. müzik) metin çıkmaz.")
            return CloudResult.DONE
        }
        val t = Transcript(
            id = System.currentTimeMillis(),
            fileName = st.fileName ?: state.value.fileName ?: "ses",
            durationMs = a.durationMs,
            language = lang,
            segments = segs,
            processMs = SystemClock.elapsedRealtime() - processingStartedAt,
            quality = if (mixed) QUALITY_WIT_MIX else QUALITY_WIT,
            warnings = oc.uncertain.map {
                RangeWarning(
                    it.from * 1000L / AudioDecoder.TARGET_RATE, it.to * 1000L / AudioDecoder.TARGET_RATE,
                    RangeWarning.UNCONFIRMED_REPEAT,
                )
            },
        )
        if (!s.alive()) return CloudResult.DONE
        val h = HistoryStore.add(ctx, t)
        val note = when {
            mixed && segs.any { it.text.startsWith("[⚠") } -> "Bazı bölümler yazıya dökülemedi; notta işaretlendi"
            t.warnings.isNotEmpty() -> "Bir bölüm tam doğrulanamadı; notun altında işaretlendi"
            mixed -> "Bir bölüm internetle dökülemedi; telefonda tamamlandı"
            detectPath == "telefon_dili" ->
                "Dil algılanamadı; ${langOf(lang)?.label ?: lang} varsayıldı. Yanlışsa ⋮ menüsünden dili değiştir."
            else -> null
        }
        s.update {
            it.copy(
                phase = Phase.Idle, result = t, previousResult = null, history = h, live = emptyList(),
                livePartial = null, etaSec = null, translation = null, translating = null,
                translationTarget = targetFor(t.language), refining = null, suggestBest = false,
                toast = note ?: it.toast,
            )
        }
        Notifier.notifyDone(ctx, t.text.take(120))
        return CloudResult.DONE
    }

    /**
     * Büyük modelle iyileştirme: mevcut not ekranda kalır, büyük model TEK KEZ
     * çalışır, bitince not yerinde güncellenir. Dil, mevcut notun dilidir (iki tur
     * tutarlı olsun, ayrıca dil algılama maliyeti olmasın). İptal/hatada mevcut
     * not olduğu gibi kalır; kullanıcı düzenlemesi korunur, silinmiş not geri gelmez.
     */
    private suspend fun refine(
        s: Session, a: DecodedAudio, t: Transcript, fallbackLang: Lang, importMs: Long,
        modelOverride: WhisperModel? = null, fallbackOverride: Boolean? = null,
    ) {
        // Deney düğmeleri ayarları yalnızca BU tur için verir; kalıcı tercihlere yazmaz
        val model = modelOverride ?: modelFor(Quality.BEST)
        val fallback = fallbackOverride ?: fallbackFor(Quality.BEST)
        s.update { it.copy(refining = "✨ En iyi model hazırlanıyor…", suggestBest = false) }
        if (!ModelStore.isReady(ctx, model)) warnIfMetered(model)
        val ok = ensureModel(model) { p ->
            s.update { it.copy(refining = "✨ En iyi model indiriliyor… %${(p * 100).toInt()}") }
        }
        if (!s.alive()) return
        if (!ok) {
            s.update { it.copy(refining = null, toast = "En iyi model indirilemedi; mevcut metin gösteriliyor") }
            Notifier.notifyDone(ctx, t.text.take(120))
            return
        }
        s.update { it.copy(refining = "✨ En iyi model ile iyileştiriliyor… %0") }
        val lang = langOf(t.language)?.takeIf { it != Lang.AUTO } ?: fallbackLang
        val r2 = runWhisper(s, a, model, Quality.BEST.beam, fallback = fallback, lang = lang, stream = false)
        if (!s.alive()) return
        val refined = Postprocess.clean(r2.segments)
        logTiming(s, r2, Quality.BEST.label, "iyilestirme", fallback = fallback, importMs = importMs, cleanCount = refined.size)
        if (r2.error != null || refined.isEmpty()) {
            s.update { it.copy(refining = null, toast = if (r2.error != null) "İyileştirme yapılamadı; mevcut metin gösteriliyor" else it.toast) }
            Notifier.notifyDone(ctx, t.text.take(120))
            return
        }
        // Her zaman diskteki EN GÜNCEL kayıt üzerinden
        val upd = HistoryStore.update(ctx, t.id) { latest ->
            latest.copy(
                segments = refined,
                processMs = r2.transcribeMs,
                // Tekrar Turbo denemesi önceki Turbo süresini "ön izleme" diye yazmasın.
                previewMs = if (latest.quality == Quality.BEST.name) latest.previewMs else latest.processMs,
                quality = Quality.BEST.name,
                warnings = emptyList(), // tüm metin yeniden üretildi
            )
        }
        if (upd == null) {
            s.update { it.copy(refining = null) }
            return
        }
        val (h2, nt) = upd
        s.update {
            if (it.result?.id == nt.id) it.copy(
                result = nt, history = h2, refining = null, translation = null, translating = null,
                toast = if (nt.editedText != null) "✨ İyileştirildi · senin düzenlemen korundu" else "✨ Metin iyileştirildi",
            ) else it.copy(history = h2, refining = null)
        }
        Notifier.notifyDone(ctx, nt.text.take(120))
    }

    /**
     * Not ekranındaki "En iyi ile dene": ön izlemeyi TEKRARLAMADAN büyük modeli
     * doğrudan mevcut ses üzerinde çalıştırır. Mevcut metin ekranda kalır.
     * Ses artık yoksa (geçmişten açılmış not) çalışmaz.
     */
    fun refineWithBest() = startBestRefinement(allowExistingBest = false, model = null, fallback = null)

    /**
     * Doğrulanamayan bölümü kullanıcı isteğiyle yeniden döker (Hızlı mod, tek parça).
     * Yalnız o aralıktaki parçalar yenisiyle DEĞİŞTİRİLİR — eski ve yeni metin yan
     * yana eklenmez. Yeni döküm yine belirsizse uyarı kalır; metin dönmezse ya da hata
     * olursa eski metin aynen korunur.
     */
    fun retryWarning(w: RangeWarning) {
        val a = audio ?: run { toast("Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor"); return }
        val cur = state.value.result ?: return
        if (cur.editedText != null) { toast("Düzenlenmiş notta bölüm yeniden dökülemez"); return }
        if (isWorking) { toast("Önce mevcut işlem bitsin"); return }
        val wit = WitEngine.forLang(cur.language) ?: run { toast("Bu dil için hızlı mod yok"); return }
        if (!online()) { toast("Bunun için internet bağlantısı gerekiyor"); return }
        val s = newSession()
        translateJob?.cancel()
        _state.update {
            it.copy(
                tab = Tab.TEXT, translation = null, translating = null,
                refining = "Bölüm yeniden dökülüyor (${Transcript.clock(w.fromMs)}–${Transcript.clock(w.toMs)})…",
            )
        }
        s.job = viewModelScope.launch {
            try {
                val from = (w.fromMs * AudioDecoder.TARGET_RATE / 1000).toInt().coerceIn(0, a.samples.size)
                val to = (w.toMs * AudioDecoder.TARGET_RATE / 1000).toInt().coerceIn(from, a.samples.size)
                val oc = wit.transcribe(a.slice(from, to), cur.language, s.cancelled)
                if (!s.alive()) return@launch
                val fresh = oc.result.segments.mapNotNull { seg ->
                    seg.text.trim().takeIf { it.isNotEmpty() }?.let {
                        seg.copy(startMs = seg.startMs + w.fromMs, endMs = seg.endMs + w.fromMs, text = it)
                    }
                }
                if (oc.result.error != null || oc.failed.isNotEmpty() || fresh.isEmpty()) {
                    s.update { it.copy(refining = null, toast = "Bölüm yeniden dökülemedi; mevcut metin korundu") }
                    return@launch
                }
                val stillUncertain = oc.uncertain.isNotEmpty()
                val upd = HistoryStore.update(ctx, cur.id) { latest ->
                    if (latest.editedText != null) latest else {
                        val kept = latest.segments.filter { it.startMs < w.fromMs || it.startMs >= w.toMs }
                        latest.copy(
                            segments = (kept + fresh).sortedBy { it.startMs },
                            warnings = if (stillUncertain) latest.warnings else latest.warnings - w,
                            revision = latest.revision + 1, // eski çeviri yeni metne karışmasın
                        )
                    }
                }
                if (upd == null) { s.update { it.copy(refining = null) }; return@launch }
                val (h, nt) = upd
                s.update {
                    if (it.result?.id == nt.id) it.copy(
                        result = nt, history = h, refining = null,
                        toast = if (stillUncertain) "Yeniden döküldü ama bölüm yine tam doğrulanamadı" else "Bölüm yeniden döküldü",
                    ) else it.copy(history = h, refining = null)
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                s.update { it.copy(refining = null, toast = "Bölüm yeniden dökülemedi; mevcut metin korundu") }
            }
        }
    }

    /** Aynı ses üzerinde q5/q8 ve fallback A/B deneyi; mevcut not korunur. */
    fun rerunBestExperiment(q8: Boolean, fallback: Boolean) {
        if (!prefs.devMode) { toast("Bu işlem için geliştirici modu açık olmalı"); return }
        if (isWorking) { toast("Önce mevcut işlem bitsin ya da iptal et"); return }
        val model = if (q8) WhisperModel.TURBO_Q8 else WhisperModel.TURBO
        if (!ModelStore.isReady(ctx, model)) toast("${model.label} indirilecek (${model.approxMb} MB)")
        startBestRefinement(allowExistingBest = true, model = model, fallback = fallback)
    }

    private fun startBestRefinement(allowExistingBest: Boolean, model: WhisperModel?, fallback: Boolean?) {
        val a = audio ?: run { toast("Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor"); return }
        val cur = state.value.result ?: return
        if (!allowExistingBest && cur.quality == Quality.BEST.name) {
            toast("Bu metin zaten En iyi ile üretildi"); return
        }
        if (isWorking && state.value.refining == null) { toast("Önce mevcut işlem bitsin"); return }
        val s = newSession()
        translateJob?.cancel()
        _state.update { it.copy(tab = Tab.TEXT, translation = null, translating = null) }
        s.job = viewModelScope.launch {
            try {
                refine(s, a, cur, fallbackLang = state.value.lang, importMs = 0, modelOverride = model, fallbackOverride = fallback)
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                s.update { it.copy(refining = null, toast = "İyileştirme yapılamadı: ${t.message ?: t.javaClass.simpleName}") }
            }
        }
    }

    // --- Geliştirici deney ayarları ---
    fun disableDevMode() {
        prefs.disableDevModeAndResetExperiments()
        WhisperEngine.threadOverride = 0
        toast("Geliştirici modu kapatıldı; deney ayarları sıfırlandı")
    }

    fun setFallbackMode(m: Int) { prefs.fallbackMode = m }
    fun setBestPreview(on: Boolean) { prefs.bestPreview = on }
    fun setTurboQ8(on: Boolean) {
        prefs.turboQ8 = on
        if (on && !ModelStore.isReady(ctx, WhisperModel.TURBO_Q8)) {
            toast("En iyi seçildiğinde ${WhisperModel.TURBO_Q8.approxMb} MB q8_0 modeli indirilecek")
        }
    }

    /**
     * Otomatik dil seçiliyse küçük base modelini Wi‑Fi'deyken arka planda
     * indirir: sonraki dökümlerde dil onunla bulunur, büyük model bir kez çalışır.
     */
    private fun prefetchDetector(lang: Lang) {
        if (lang != Lang.AUTO || ModelStore.isReady(ctx, WhisperModel.BASE)) return
        val cm = ctx.getSystemService(android.net.ConnectivityManager::class.java)
        if (cm?.isActiveNetworkMetered != false) return
        viewModelScope.launch { runCatching { ModelStore.download(ctx, WhisperModel.BASE) {} } }
    }

    private fun warnIfMetered(m: WhisperModel) {
        val cm = ctx.getSystemService(android.net.ConnectivityManager::class.java)
        if (cm?.isActiveNetworkMetered == true) toast("Mobil veri ile ${m.approxMb} MB indiriliyor. Wi‑Fi önerilir.")
    }

    // ------------------------------------------------------------------
    // Not işlemleri
    // ------------------------------------------------------------------

    /** Not ekranında düzenlenen metni kaydeder (ham dökümle aynıysa düzenleme kaldırılır). */
    fun saveEdit(text: String) {
        val r = state.value.result ?: return
        val norm = { x: String -> x.replace(Regex("\\s+"), " ").trim() }
        val edited = text.trim().takeIf { it.isNotEmpty() && norm(it) != norm(r.rawText) }
        translateJob?.cancel()
        viewModelScope.launch {
            val upd = HistoryStore.update(ctx, r.id) { it.copy(editedText = edited, revision = it.revision + 1) }
            if (upd == null) { toast("Bu not silinmiş"); return@launch }
            val (h, nt) = upd
            _state.update {
                if (it.result?.id == nt.id) it.copy(result = nt, history = h, translation = null, translating = null, toast = "Kaydedildi")
                else it.copy(history = h)
            }
        }
    }

    // ------------------------------------------------------------------
    // İlk bulut aktarımı onayı
    // ------------------------------------------------------------------
    private var consentWaiter: CompletableDeferred<Int>? = null

    private suspend fun askCloudConsent(s: Session): Int {
        val d = CompletableDeferred<Int>()
        consentWaiter = d
        s.update { it.copy(consentAsk = true) }
        return try {
            d.await()
        } finally {
            if (consentWaiter === d) consentWaiter = null
            _state.update { it.copy(consentAsk = false) }
        }
    }

    /** Penceredeki seçim: [CONSENT_CLOUD], [CONSENT_LOCAL] ya da [CONSENT_CANCEL] (kapatma dahil). */
    fun answerCloudConsent(choice: Int) {
        consentWaiter?.complete(choice)
    }

    /** "Telefonda işle" için gerekecek indirme (pencerede gösterilir); hazırsa null. */
    fun localModelDownloadMb(): Int? {
        val q = state.value.quality
        return if (localReadyQuality(q) != null) null else q.model.approxMb
    }

    /** Oturumu sonuç üretmeden bitirir; yeniden dökümse önceki not geri gelir. */
    private fun abandon(s: Session, message: String) {
        s.update {
            val prev = it.previousResult
            if (prev != null) it.copy(result = prev, previousResult = null, phase = Phase.Idle, live = emptyList(), livePartial = null, etaSec = null, toast = message)
            else it.copy(phase = Phase.Idle, live = emptyList(), livePartial = null, etaSec = null, toast = message)
        }
        cancel(s)
    }

    /** Çalışan işi iptal eder. Yeniden döküm iptalinde önceki not geri gelir. */
    fun cancelWork() {
        val s = session ?: return
        cancel(s)
        _state.update {
            val prev = it.previousResult
            when {
                it.refining != null -> it.copy(refining = null)
                prev != null -> it.copy(result = prev, previousResult = null, phase = Phase.Idle, live = emptyList(), livePartial = null, etaSec = null)
                else -> it.copy(phase = Phase.Idle, live = emptyList(), livePartial = null, etaSec = null)
            }
        }
    }

    /** Başlangıç ekranına dön: çalışan her iş iptal, açık not kapanır. */
    fun goHome() {
        cancelSession()
        clearForNew()
    }

    fun clearForNew() {
        cancelSession()
        translateJob?.cancel()
        stopPlayback()
        audio = null
        player.setSource(null)
        // Not kapanınca sesin geçici kopyası da silinir (yalnızca gerektiği kadar tutulur)
        runCatching { ctx.cacheDir.listFiles()?.filter { it.name.startsWith("current_audio") }?.forEach { it.delete() } }
        _state.update {
            it.copy(
                result = null, previousResult = null, fileName = null, hasAudio = false, waveform = FloatArray(0),
                audioMs = 0, phase = Phase.Idle, positionMs = 0, translation = null, translating = null,
                live = emptyList(), livePartial = null, suggestBest = false, refining = null, etaSec = null, tab = Tab.TEXT,
            )
        }
    }

    fun openHistory(t: Transcript) {
        val s = state.value
        if (isWorking && s.refining == null) { toast("Önce mevcut işlem bitsin ya da iptal et"); return }
        clearForNew()
        _state.update {
            it.copy(
                result = t, fileName = t.fileName, audioMs = t.durationMs,
                translationTarget = targetFor(t.language),
            )
        }
    }

    fun deleteCurrent() {
        val r = state.value.result ?: return
        goHome()
        deleteHistory(r)
    }

    fun deleteHistory(t: Transcript) {
        viewModelScope.launch {
            val h = HistoryStore.remove(ctx, t.id)
            _state.update { it.copy(history = h, undoDeleted = t) }
            // Geri al süresi ekrandan bağımsız: Ayarlar'a gidip dönmek süreyi uzatmaz
            undoJob?.cancel()
            undoJob = viewModelScope.launch {
                delay(5_000)
                _state.update { if (it.undoDeleted?.id == t.id) it.copy(undoDeleted = null) else it }
            }
        }
    }

    fun undoDelete() {
        val t = state.value.undoDeleted ?: return
        undoJob?.cancel()
        viewModelScope.launch {
            val h = HistoryStore.restore(ctx, t)
            _state.update { it.copy(history = h, undoDeleted = null) }
        }
    }

    /** Tüm notlar + dışa aktarılan dosyalar + günlükler + önbellekteki ses silinir; bellekteki durum da. */
    fun clearHistory() {
        goHome()
        undoJob?.cancel()
        viewModelScope.launch {
            HistoryStore.clear(ctx)
            withContext(Dispatchers.IO) {
                Exports.clear(ctx)
                File(ctx.filesDir, "results").deleteRecursively()
                ctx.cacheDir.listFiles()
                    ?.filter { it.name.startsWith("current_audio") || it.name == "mlkit_input.pcm" }
                    ?.forEach { it.delete() }
            }
            _state.update {
                it.copy(history = emptyList(), undoDeleted = null, testLog = emptyList(), lastTiming = null, lastDiag = null, toast = "Tüm notlar ve dosyalar silindi")
            }
        }
    }

    /** Kayıtlı hedef dil kaynakla aynı değilse onu kullan, değilse akıllı varsayılan. */
    private fun targetFor(source: String): Lang =
        prefs.translateTarget?.let { langOf(it) }?.takeIf { it.code != source } ?: defaultTarget(source)

    // ------------------------------------------------------------------
    // Çeviri
    // ------------------------------------------------------------------

    /** Çeviri sekmesini açar; henüz çeviri yoksa başlatır. */
    fun openTranslation() {
        setTab(Tab.TRANSLATION)
        if (state.value.translation == null && state.value.translating == null) translate(state.value.translationTarget)
    }

    fun translate(target: Lang) {
        translateJob?.cancel()
        val r = state.value.result ?: return
        val source = langOf(r.language) ?: run { toast("Kaynak dil tanınmadı"); return }
        // Kullanıcı metni düzelttiyse düzeltilmiş metin çevrilir
        val input = r.editedText?.let { listOf(Segment(0, r.durationMs, it)) } ?: r.segments
        if (source == target) {
            _state.update { it.copy(translationTarget = target, translation = input, translating = null) }
            return
        }
        if (!OnDeviceTranslator.supports(source)) { toast("${source.label} için çeviri desteklenmiyor"); return }
        _state.update { it.copy(translationTarget = target, translation = null, translating = "Hazırlanıyor…") }
        translateJob = viewModelScope.launch {
            fun sameNote(s: MainState) = s.result?.let { it.id == r.id && it.revision == r.revision } == true
            try {
                val out = OnDeviceTranslator.translate(input, source, target) { msg ->
                    _state.update { if (sameNote(it)) it.copy(translating = msg) else it }
                }
                _state.update { if (sameNote(it)) it.copy(translation = out, translating = null) else it }
            } catch (t: CancellationException) {
                _state.update { if (sameNote(it)) it.copy(translating = null) else it }
                throw t
            } catch (t: Throwable) {
                _state.update {
                    if (sameNote(it)) it.copy(translating = null, toast = "Çeviri yapılamadı: ${t.message ?: "internet bağlantısını kontrol et"}")
                    else it
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Oynatıcı
    // ------------------------------------------------------------------
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
                // MediaPlayer.seekTo eşzamansız: atlamadan hemen sonra eski konumu okuyup geri sıçramasın
                if (SystemClock.elapsedRealtime() - lastSeekAt > 400) {
                    _state.update { it.copy(positionMs = player.positionMs) }
                }
                delay(150)
            }
            _state.update { it.copy(playing = player.isPlaying) }
        }
    }

    @Volatile private var lastSeekAt = 0L

    fun seekTo(ms: Long) {
        lastSeekAt = SystemClock.elapsedRealtime()
        val max = state.value.audioMs
        val v = if (max > 0) ms.coerceIn(0, max) else ms.coerceAtLeast(0)
        player.seekTo(v)
        _state.update { it.copy(positionMs = v) }
    }

    private fun stopPlayback() {
        ticker?.cancel()
        player.release()
        _state.update { it.copy(playing = false) }
    }

    // ------------------------------------------------------------------
    // Test araçları (Ayarlar > Geliştirici)
    // ------------------------------------------------------------------
    fun runMlKitTest(advanced: Boolean) {
        val a = audio ?: run { toast("Önce bir ses dök"); return }
        viewModelScope.launch {
            log("ML Kit ${if (advanced) "ADVANCED" else "BASIC"} çalışıyor…")
            val r = MlKitEngine(ctx, advanced).transcribe(a, state.value.lang)
            log(describe(r))
        }
    }

    fun deviceInfo(): String {
        WhisperEngine.ensureBackends(ctx)
        return DeviceInfo.summary(ctx) +
            "\nCPU backend: " + WhisperEngine.backendInfo +
            "\nwhisper thread: " + WhisperEngine.threadCount() + " (otomatik " + WhisperEngine.autoThreads + ")" +
            "\nÇekirdekler: " + WhisperEngine.cpuReport() +
            "\nggml: " + WhisperEngine.systemInfo()
    }

    fun modelReady(m: WhisperModel) = ModelStore.isReady(ctx, m)

    fun deleteModel(m: WhisperModel) {
        if (isWorking) { toast("Döküm sürerken model silinemez"); return }
        WhisperEngine.releaseIfIdle()
        ModelStore.delete(ctx, m)
        toast("${m.label} silindi")
    }

    private fun log(line: String) = _state.update { it.copy(testLog = (listOf(line) + it.testLog).take(12)) }

    private fun describe(r: EngineResult): String =
        if (r.error != null) "${r.engine} ${r.variant}: HATA — ${r.error}"
        else "${r.engine} ${r.variant}: ${"%.1f".format(r.transcribeMs / 1000.0)} sn (RTF ${"%.2f".format(r.rtf)}) — ${r.text.take(80)}"

    // ------------------------------------------------------------------
    // Dosya
    // ------------------------------------------------------------------

    /** Önbelleğe kopyalar; [MAX_FILE_MB] aşılırsa kopyayı siler ve null döner. İptal edilebilir. */
    private fun copyToCache(uri: Uri, name: String, s: Session): File? {
        val ext = name.substringAfterLast('.', "bin").filter { it.isLetterOrDigit() }.take(5).ifEmpty { "bin" }
        ctx.cacheDir.listFiles()?.filter { it.name.startsWith("current_audio") }?.forEach { it.delete() }
        val out = File(ctx.cacheDir, "current_audio.$ext")
        val limit = MAX_FILE_MB * 1024L * 1024L
        ctx.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Dosya okunamadı" }
            out.outputStream().use { o ->
                val buf = ByteArray(64 * 1024)
                var total = 0L
                while (true) {
                    if (s.cancelled.get()) throw CancellationException("iptal")
                    val n = input.read(buf)
                    if (n < 0) break
                    total += n
                    if (total > limit) { o.close(); out.delete(); return null }
                    o.write(buf, 0, n)
                }
            }
        }
        if (out.length() == 0L) { out.delete(); throw DecodeException("Dosya boş") }
        return out
    }

    private fun displayName(uri: Uri): String =
        runCatching {
            ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull() ?: uri.lastPathSegment ?: "ses"

    override fun onCleared() {
        cancelSession()
        player.release()
        super.onCleared()
    }

    private companion object { const val MAX_FILE_MB = 500 }
}
