package com.aitolian.sesyazibench

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aitolian.sesyazibench.audio.AudioDecoder
import com.aitolian.sesyazibench.audio.ImportCopy
import com.aitolian.sesyazibench.audio.DecodeException
import com.aitolian.sesyazibench.audio.DecodedAudio
import com.aitolian.sesyazibench.audio.Player
import com.aitolian.sesyazibench.audio.peaks
import com.aitolian.sesyazibench.data.Exports
import com.aitolian.sesyazibench.data.HistoryStore
import com.aitolian.sesyazibench.data.Prefs
import com.aitolian.sesyazibench.data.RangeWarning
import com.aitolian.sesyazibench.data.Transcript
import com.aitolian.sesyazibench.engine.ERR_CANCELLED
import com.aitolian.sesyazibench.engine.EngineResult
import com.aitolian.sesyazibench.engine.Lang
import com.aitolian.sesyazibench.engine.OnDeviceTranslator
import com.aitolian.sesyazibench.engine.Segment
import com.aitolian.sesyazibench.engine.TRANSLATABLE
import com.aitolian.sesyazibench.engine.WitEngine
import com.aitolian.sesyazibench.engine.langOf
import kotlinx.coroutines.CancellationException
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

/*
 * MuteRead v1.22 — YALNIZ Wit.ai (internet). Cihaz içi Whisper modelleri, yerel yedek,
 * "En iyi ile iyileştir", model indirme ve otomatik dil algılama (yerel model) KALDIRILDI.
 * Vaat: hız. Hata olursa sessizce yavaş bir motora geçilmez; açık mesaj + "Tekrar dene".
 * Çeviri (ML Kit, cihazda) olduğu gibi kalır.
 */

sealed interface Phase {
    data object Idle : Phase
    data class Preparing(val message: String) : Phase
    data class Transcribing(val percent: Int) : Phase
    data class Failed(val message: String) : Phase
}

enum class Tab { TEXT, TRANSLATION }

data class MainState(
    val phase: Phase = Phase.Idle,
    /** Konuşma dili (Wit uygulaması bu dile göre seçilir). Otomatik algılama yok. */
    val lang: Lang = Lang.EN,
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
    /** Döküm sürerken kesinleşen cümleler (canlı akış). */
    val live: List<Segment> = emptyList(),
    /** Bölüm yeniden dökülürken durum şeridi metni. */
    val refining: String? = null,
    /** Artınca UI döküm başı geçiş reklamını dener (tek seferlik olay sayacı). */
    val adRequest: Int = 0,
    /** Son silinen not — "Geri al" için 5 sn tutulur. */
    val undoDeleted: Transcript? = null,
    /** Not ekranı yazı boyutu (sp). */
    val readerFont: Int = 19,
    /** Son dökümün süre özeti (geliştirici araçlarında). */
    val lastTiming: String? = null,
    /** Son dökümün teşhisi (yalnız geliştirici modunda; metin/anahtar içermez). */
    val lastDiag: String? = null,
    /** Henüz kesinleşmemiş canlı ara metin. */
    val livePartial: String? = null,
    /** Satır aralığı: 0 sıkı, 1 normal, 2 geniş. */
    val readerLine: Int = 1,
    /** Ses çalarken okunan cümleyi takip et. */
    val followAudio: Boolean = true,
    /** Tema: 0 sistem, 1 açık (varsayılan), 2 koyu. */
    val themeMode: Int = 1,
    /** Her yeni paylaşım/dosya seçiminde artar: ekran Ayarlar/Notlar'dan okuyucuya döner. */
    val importRequestId: Long = 0,
)

/** Wit.ai ile üretilen notların kalite etiketi. */
const val QUALITY_WIT = "WIT"
/** Wit; bazı bölümleri yazıya dökülemedi (notta işaretli). */
const val QUALITY_WIT_MIX = "WIT_MIX"

/** Not internetle (Wit) mi üretildi? */
fun Transcript.isCloud(): Boolean = quality == QUALITY_WIT || quality == QUALITY_WIT_MIX

/** Mono/kanal enerji oranı bunun altındaysa (zıt fazlı stereo) tek kanal kullanılır. */
private const val ANTI_PHASE_RATIO = 0.1

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

    // ------------------------------------------------------------------
    // Oturumlar: her döküm bir oturumdur. Yeni oturum eskisini iptal eder;
    // iptal edilmiş/eski oturumun hiçbir geri çağrısı duruma yazamaz.
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
        s.cancelled.set(true)   // ağ isteği bekçisi bağlantıyı hemen kapatır
        s.job?.cancel()
        if (session === s) session = null
    }

    private fun cancelSession() { session?.let { cancel(it) } }

    val isWorking: Boolean get() = session?.job?.isActive == true

    /** Hızlı mod anahtarları bu derlemede var mı. */
    val cloudAvailable: Boolean get() = WitEngine.tokens.isNotEmpty()

    /** Seçilebilir konuşma dilleri: yalnız Wit anahtarı olanlar. */
    val speechLangs: List<Lang> get() = TRANSLATABLE.filter { WitEngine.supports(it.code) }

    /** Kayıtlı dil; yoksa (ya da eski "otomatik") telefonun dili, o da yoksa İngilizce. */
    private fun initialLang(): Lang {
        val saved = prefs.defaultLang?.let { langOf(it) }?.takeIf { it != Lang.AUTO && WitEngine.supports(it.code) }
        return saved
            ?: langOf(Locale.getDefault().language)?.takeIf { it != Lang.AUTO && WitEngine.supports(it.code) }
            ?: speechLangs.firstOrNull { it == Lang.EN } ?: speechLangs.firstOrNull() ?: Lang.EN
    }

    init {
        val target = prefs.translateTarget?.let { langOf(it) }
        _state.update {
            it.copy(
                lang = initialLang(), translationTarget = target ?: it.translationTarget, readerFont = prefs.readerFont,
                readerLine = prefs.readerLine.coerceIn(0, 2), followAudio = prefs.followAudio,
                themeMode = prefs.themeMode.coerceIn(0, 2),
            )
        }
        viewModelScope.launch {
            // Eski sürümlerin indirdiği yerel modelleri temizle (artık kullanılmıyor; yüzlerce MB)
            withContext(Dispatchers.IO) { runCatching { File(ctx.filesDir, "models").deleteRecursively() } }
            val h = HistoryStore.load(ctx)
            _state.update { it.copy(history = h) }
        }
    }

    // --- Ayarlar (kalıcı varsayılanlar) ---
    fun setDefaultLang(l: Lang) { prefs.defaultLang = l.code; _state.update { it.copy(lang = l) } }
    fun setDefaultTarget(l: Lang) {
        prefs.translateTarget = l.code
        _state.update { it.copy(translationTarget = l) }
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

    private fun online(): Boolean {
        val cm = ctx.getSystemService(android.net.ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        // VALIDATED: "bağlı ama internet yok" (ör. giriş sayfalı Wi‑Fi) durumunu eler
        return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun setTab(t: Tab) = _state.update { it.copy(tab = t) }
    fun toastShown() = _state.update { it.copy(toast = null) }
    fun toast(msg: String) = _state.update { it.copy(toast = msg) }

    /** Reklamı isteyen oturum (iptal/yeni ses gelirse reklam atlanır). */
    @Volatile private var adSession: Session? = null

    /**
     * Döküm başı geçiş reklamı hâlâ gösterilsin mi? Yalnız aynı oturum sürerken ve
     * ekranda henüz hiç metin yokken (AdMob: içerik açıldıktan sonra beklenmedik
     * geçiş reklamı yasak). Metin geldiyse bu dökümde reklam denemesi biter.
     */
    fun adStillWanted(): Boolean {
        val a = adSession ?: return false
        if (!a.alive() || a.firstVisibleAt != 0L) return false
        val s = state.value
        return isWorking && s.phase !is Phase.Failed && s.result == null &&
            s.live.isEmpty() && s.livePartial.isNullOrBlank()
    }

    // ------------------------------------------------------------------
    // Yeni ses
    // ------------------------------------------------------------------

    /** Ses seçildi / paylaşıldı → kopyala, çöz, hemen Wit ile yazıya dök. Önceki her işi iptal eder. */
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
                refining = null, importRequestId = it.importRequestId + 1,
            )
        }
        // Reklam, ses hazırlanırken hemen istenir (metin gelmeden gösterilir; gelirse atlanır)
        requestAd(s)
        s.job = viewModelScope.launch {
            try {
                val name = withContext(Dispatchers.IO) { displayName(uri) }
                val copy = withContext(Dispatchers.IO) { copyToCache(uri, name, s) }
                    ?: throw DecodeException("Dosya çok büyük (en fazla $MAX_FILE_MB MB)")
                var decoded = withContext(Dispatchers.IO) {
                    AudioDecoder.decode(ctx, Uri.fromFile(copy), { !s.alive() })
                }
                // Kanallar büyük ölçüde zıt fazlıysa ortalama sesi siler: yalnız sol kanalla yeniden çöz
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
                // Yalnız bu oturumun dosyası kalır; eski/iptal edilmiş oturumların kopyaları silinir
                runCatching {
                    ctx.cacheDir.listFiles()
                        ?.filter { it.name.startsWith("current_audio") && it != copy }
                        ?.forEach { it.delete() }
                }
                if (prefs.devMode) _state.update { it.copy(lastDiag = Diagnostics.audio(decoded)) }
                s.update {
                    it.copy(fileName = name, audioMs = decoded.durationMs, waveform = wave, hasAudio = true, positionMs = 0)
                }
                transcribe(s, decoded, state.value.lang)
            } catch (t: CancellationException) {
                throw t
            } catch (t: DecodeException) {
                dropOwnCopy(s)
                s.update { it.copy(phase = Phase.Failed(t.message ?: "Ses açılamadı")) }
            } catch (t: OutOfMemoryError) {
                dropOwnCopy(s)
                s.update { it.copy(phase = Phase.Failed("Dosya bu telefon için çok büyük")) }
            } catch (t: Throwable) {
                dropOwnCopy(s)
                s.update { it.copy(phase = Phase.Failed("Ses açılamadı: ${t.message ?: t.javaClass.simpleName}")) }
            }
        }
    }

    /** Hata ekranındaki "Tekrar dene": elde geçerli bir ses varsa aynı sesi yeniden döker. */
    fun canRetry() = audio != null

    /**
     * Aynı sesi yeniden döker (ör. başka dil). Önceki sonuç [MainState.previousResult]
     * olarak saklanır; hata ya da iptalde önceki not geri gelir.
     */
    fun retranscribe(lang: Lang = state.value.lang) {
        val a = audio ?: run {
            _state.update { it.copy(lang = lang) }
            if (state.value.result != null) toast("Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor")
            return
        }
        val s = newSession()
        translateJob?.cancel()
        _state.update {
            it.copy(
                previousResult = it.result ?: it.previousResult, result = null, live = emptyList(), livePartial = null,
                phase = Phase.Preparing("Hazırlanıyor…"), tab = Tab.TEXT, translation = null, translating = null,
                refining = null, lang = lang,
            )
        }
        s.job = viewModelScope.launch {
            try {
                transcribe(s, a, lang)
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                fail(s, "Yazıya dökülemedi: ${t.message ?: t.javaClass.simpleName}")
            }
        }
    }

    /** Not menüsündeki "Dil: X ile yeniden dök". */
    fun retranscribeInLanguage(lang: Lang) = retranscribe(lang)

    /** Hata: önceki not varsa ona dön (not ekranında kal), yoksa hata ekranı. */
    private fun fail(s: Session, message: String) {
        s.update {
            val prev = it.previousResult
            if (prev != null) it.copy(result = prev, previousResult = null, phase = Phase.Idle, live = emptyList(), livePartial = null, toast = message)
            else it.copy(phase = Phase.Failed(message), live = emptyList(), livePartial = null)
        }
    }

    /**
     * Süre kaydı, döküm TAMAMEN bittikten sonra (kurtarma turu + kayıt dahil).
     * firstVisible = ilk metnin geldiği geri çağrı anı (ekranda görünme anı değil).
     */
    private fun logTiming(s: Session, r: EngineResult, importMs: Long, cleanCount: Int, flagged: Int, processingStartedAt: Long) {
        val now = SystemClock.elapsedRealtime()
        val first = if (s.firstVisibleAt > 0) s.firstVisibleAt - s.startedAt else -1L
        val total = now - s.startedAt
        val line = ResultLog.summary(r, importMs, first, total, cleanCount) + " · işlem ${now - processingStartedAt} ms · işaretli $flagged"
        _state.update { it.copy(lastTiming = line) }
        if (prefs.devMode) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching { ResultLog.append(ctx, r, importMs, first, total, cleanCount) }
            }
        }
    }

    /** Oturum başına en fazla bir reklam denemesi (sıklık sınırı Ads'te). */
    private fun requestAd(s: Session) {
        if (s.adRequested) return
        s.adRequested = true
        adSession = s
        _state.update { it.copy(adRequest = it.adRequest + 1) }
    }

    /** Hata kodundan anlaşılır mesaj. */
    private fun messageFor(code: String?, langLabel: String): String = when (code) {
        WitEngine.ERR_NETWORK, WitEngine.ERR_TIMEOUT -> "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene."
        WitEngine.ERR_RATE -> "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene."
        WitEngine.ERR_AUTH -> "$langLabel için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene."
        WitEngine.ERR_SERVER, WitEngine.ERR_SERVICE -> "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene."
        else -> "Yazıya dökülemedi. Tekrar dene."
    }

    /**
     * Wit.ai ile döküm (tek motor). Parçalar paralel gönderilir; başarısız parçalar bir
     * tur daha Wit ile denenir, yine olmazsa notta "[⚠ … yazıya dökülemedi]" diye
     * işaretlenir — eksik metin tam gibi kaydedilmez, başka bir motora geçilmez.
     */
    private suspend fun transcribe(s: Session, a: DecodedAudio, langSel: Lang) {
        val processingStartedAt = SystemClock.elapsedRealtime()
        val importMs = processingStartedAt - s.startedAt
        val lang = langSel.code
        val label = langSel.label
        if (!online()) { fail(s, "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene."); return }
        val wit = WitEngine.forLang(lang) ?: run { fail(s, "$label için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir."); return }

        s.update { it.copy(phase = Phase.Transcribing(-1), live = emptyList(), livePartial = null, refining = null) }
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
        val r = oc.result
        if (!s.alive() || r.error == ERR_CANCELLED) return
        var segs = r.segments.mapNotNull { seg -> seg.text.trim().takeIf { it.isNotEmpty() }?.let { seg.copy(text = it) } }
        fun ms(sample: Int) = sample.toLong() * 1000L / AudioDecoder.TARGET_RATE
        val warnings = oc.uncertain.map { RangeWarning(ms(it.from), ms(it.to), RangeWarning.UNCONFIRMED_REPEAT) }.toMutableList()

        // Hiç metin yoksa açık hata (başka motora geçiş YOK). Kısmi başarıda metin korunur.
        if (segs.isEmpty() && (r.error != null || oc.authFailed)) {
            logTiming(s, r, importMs, 0, oc.failed.size, processingStartedAt)
            fail(s, messageFor(r.error ?: WitEngine.ERR_AUTH, label))
            return
        }
        var mixed = false
        val fills = mutableMapOf<Int, String>()
        if (oc.failed.isNotEmpty()) {
            // Başarısız parçalar: bir tur daha Wit (tek parça); yetki hatasında tekrar istek atılmaz.
            for (f in oc.failed) {
                if (!s.alive()) return
                val fromMs = ms(f.from)
                val toMs = ms(f.to)
                val again = if (f.code == WitEngine.ERR_AUTH) null
                    else runCatching { wit.transcribe(a.slice(f.from, f.to), lang, s.cancelled) }.getOrNull()
                val fresh = again?.takeIf { it.result.error == null && it.failed.isEmpty() }
                    ?.result?.segments
                    ?.mapNotNull { seg -> seg.text.trim().takeIf { it.isNotEmpty() }?.let { seg.copy(startMs = seg.startMs + fromMs, endMs = seg.endMs + fromMs, text = it) } }
                if (fresh != null) {
                    fills[f.from] = "ikinci denemede tamamlandı (${fresh.size} parça)"
                    segs = segs + fresh
                    // İkinci turun belirsiz aralıkları ana ses zamanına kaydırılıp korunur
                    again?.uncertain?.forEach { u ->
                        warnings += RangeWarning(ms(f.from + u.from), ms(f.from + u.to), RangeWarning.UNCONFIRMED_REPEAT)
                    }
                } else {
                    mixed = true
                    fills[f.from] = "işaretlendi (${again?.failed?.firstOrNull()?.code ?: again?.result?.error ?: f.code})"
                    segs = segs + Segment(fromMs, toMs, "[⚠ ${Transcript.clock(fromMs)}–${Transcript.clock(toMs)} arası yazıya dökülemedi]")
                    warnings += RangeWarning(fromMs, toMs, RangeWarning.FAILED)
                }
            }
            segs = segs.sortedBy { it.startMs }
        }
        if (!s.alive()) return
        if (prefs.devMode) {
            val d = Diagnostics.audio(a) + "\n" + Diagnostics.wit(lang, oc, fills)
            _state.update { it.copy(lastDiag = d) }
        }
        if (segs.isEmpty()) {
            logTiming(s, r, importMs, 0, 0, processingStartedAt)
            fail(s, "Konuşma algılanamadı. Konuşma dili $label mi? Değilse dili değiştirip tekrar dene.")
            return
        }
        val t = Transcript(
            id = System.currentTimeMillis(),
            fileName = state.value.fileName ?: "ses",
            durationMs = a.durationMs,
            language = lang,
            segments = segs,
            processMs = SystemClock.elapsedRealtime() - processingStartedAt,
            quality = if (mixed) QUALITY_WIT_MIX else QUALITY_WIT,
            warnings = warnings.distinct().sortedBy { it.fromMs },
        )
        if (!s.alive()) return
        val h = try {
            HistoryStore.add(ctx, t)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null // disk dolu vb.: metin yine gösterilir, kaydedilemediği söylenir
        }
        // Ölçüm: kurtarma turu ve kayıt dahil, en sonda (QA-12)
        logTiming(s, r, importMs, segs.size, warnings.size, processingStartedAt)
        val note = when {
            h == null -> "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)"
            mixed -> "Bazı bölümler yazıya dökülemedi; notta işaretlendi"
            t.warnings.isNotEmpty() -> "Bir bölüm tam doğrulanamadı; notun altında işaretlendi"
            else -> null
        }
        s.update {
            it.copy(
                phase = Phase.Idle, result = t, previousResult = null, history = h ?: it.history, live = emptyList(),
                livePartial = null, translation = null, translating = null,
                translationTarget = targetFor(t.language), refining = null,
                toast = note ?: it.toast,
            )
        }
        Notifier.notifyDone(ctx, t.id)
    }

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
                            // Eski uyarı kalkar; yeni turda belirsizlik varsa o aralık yeniden işaretlenir
                            warnings = (latest.warnings - w + oc.uncertain.map { u ->
                                RangeWarning(
                                    w.fromMs + u.from.toLong() * 1000L / AudioDecoder.TARGET_RATE,
                                    w.fromMs + u.to.toLong() * 1000L / AudioDecoder.TARGET_RATE,
                                    RangeWarning.UNCONFIRMED_REPEAT,
                                )
                            }).distinct().sortedBy { it.fromMs },
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

    // --- Geliştirici ---
    fun disableDevMode() {
        prefs.devMode = false
        toast("Geliştirici modu kapatıldı")
    }

    // ------------------------------------------------------------------
    // Not işlemleri
    // ------------------------------------------------------------------

    /**
     * Not ekranında düzenlenen metni kaydeder. [base] = düzenleme açıldığında gösterilen
     * metin; değişmediyse hiçbir şey yazılmaz. Paragraf ve boşluklar korunur. Boş metin
     * kaydedilmez. Dönüş: true = kaydedildi (ya da değişiklik yok) → editör kapanabilir;
     * false = disk hatası vb. → taslak açık kalır.
     */
    suspend fun saveEdit(text: String, base: String): Boolean {
        val r = state.value.result ?: return true
        val trimmed = text.trim()
        if (trimmed.isEmpty()) { toast("Boş metin kaydedilemez. Notu silmek için menüyü kullan."); return false }
        if (trimmed == base.trim()) return true
        // Ham dökümle (boşluk farkı hariç) aynıysa düzenleme kaldırılır; değilse metin AYNEN (paragraflarıyla) saklanır
        val norm = { x: String -> x.replace(Regex("\\s+"), " ").trim() }
        val edited = trimmed.takeIf { norm(it) != norm(r.rawText) }
        translateJob?.cancel()
        return try {
            val upd = HistoryStore.update(ctx, r.id) { it.copy(editedText = edited, revision = it.revision + 1) }
            if (upd == null) { toast("Bu not silinmiş"); return true }
            val (h, nt) = upd
            _state.update {
                if (it.result?.id == nt.id) it.copy(result = nt, history = h, translation = null, translating = null, toast = "Kaydedildi")
                else it.copy(history = h)
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            toast("Kaydedilemedi (depolama dolu olabilir). Taslağın açık.")
            false
        }
    }

    /** Çalışan işi iptal eder. Yeniden döküm iptalinde önceki not geri gelir. */
    fun cancelWork() {
        val s = session ?: return
        cancel(s)
        _state.update {
            val prev = it.previousResult
            when {
                it.refining != null -> it.copy(refining = null)
                prev != null -> it.copy(result = prev, previousResult = null, phase = Phase.Idle, live = emptyList(), livePartial = null)
                else -> it.copy(phase = Phase.Idle, live = emptyList(), livePartial = null)
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
                live = emptyList(), livePartial = null, refining = null, tab = Tab.TEXT,
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

    /** Bildirimden gelen not kimliği: not geçmişte varsa aç (süreç yeniden başlamış olabilir). */
    fun openNoteById(id: Long) {
        viewModelScope.launch {
            val list = runCatching { HistoryStore.load(ctx) }.getOrNull() ?: state.value.history
            val t = list.firstOrNull { it.id == id } ?: return@launch
            if (state.value.result?.id == id) return@launch
            if (isWorking) return@launch
            openHistory(t)
            _state.update { it.copy(importRequestId = it.importRequestId + 1) }
        }
    }

    fun deleteCurrent() {
        val r = state.value.result ?: return
        goHome()
        deleteHistory(r)
    }

    fun deleteHistory(t: Transcript) {
        viewModelScope.launch {
            val h = try { HistoryStore.remove(ctx, t.id) } catch (e: CancellationException) { throw e } catch (e: Exception) {
                toast("Not silinemedi (depolama hatası)"); return@launch
            }
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
            val h = try { HistoryStore.restore(ctx, t) } catch (e: CancellationException) { throw e } catch (e: Exception) {
                toast("Geri alınamadı (depolama hatası)"); return@launch
            }
            _state.update { it.copy(history = h, undoDeleted = null) }
        }
    }

    /** Tüm notlar + dışa aktarılan dosyalar + günlükler + önbellekteki ses silinir; bellekteki durum da. */
    fun clearHistory() {
        goHome()
        undoJob?.cancel()
        viewModelScope.launch {
            try { HistoryStore.clear(ctx) } catch (e: CancellationException) { throw e } catch (e: Exception) {
                toast("Notlar silinemedi (depolama hatası)"); return@launch
            }
            withContext(Dispatchers.IO) {
                Exports.clear(ctx)
                File(ctx.filesDir, "results").deleteRecursively()
                ctx.cacheDir.listFiles()
                    ?.filter { it.name.startsWith("current_audio") }
                    ?.forEach { it.delete() }
            }
            _state.update {
                it.copy(history = emptyList(), undoDeleted = null, lastTiming = null, lastDiag = null, toast = "Uygulamadaki tüm notlar ve dosyalar silindi")
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

    /** Her çeviri isteği yeni kuşak; eski işin geri çağrıları yeni durumu değiştiremez. */
    @Volatile private var translationGeneration = 0L

    fun translate(target: Lang) {
        val gen = ++translationGeneration
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
            fun sameNote(s: MainState) = gen == translationGeneration &&
                s.result?.let { it.id == r.id && it.revision == r.revision } == true
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
    // Dosya
    // ------------------------------------------------------------------

    /**
     * Önbelleğe kopyalar; [MAX_FILE_MB] aşılırsa kopyayı siler ve null döner.
     * Her oturumun KENDİ dosyası vardır (current_audio_<id>): iptal edilmiş eski iş,
     * engelleyici sağlayıcı çağrısından geç dönse bile yeni sesin dosyasına dokunamaz.
     * Hata/iptalde yalnız bu oturumun dosyası silinir.
     */
    private fun copyToCache(uri: Uri, name: String, s: Session): File? {
        val ext = name.substringAfterLast('.', "bin").filter { it.isLetterOrDigit() }.take(5).ifEmpty { "bin" }
        val out = File(ctx.cacheDir, "current_audio_${s.id}.$ext")
        return try {
            ImportCopy.copy({ ctx.contentResolver.openInputStream(uri) }, out, MAX_FILE_MB * 1024L * 1024L) { s.alive() }
        } catch (e: ImportCopy.EmptyImportException) {
            throw DecodeException("Dosya boş")
        }
    }

    /** Çözülemeyen sesin bu oturuma ait kopyası (tekrar denemede kullanılmaz) silinir. */
    private fun dropOwnCopy(s: Session) {
        runCatching {
            ctx.cacheDir.listFiles()?.filter { it.name.startsWith("current_audio_${s.id}.") }?.forEach { it.delete() }
        }
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
