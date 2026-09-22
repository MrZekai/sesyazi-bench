package com.aitolian.sesyazibench.engine

import android.content.Context
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import com.aitolian.sesyazibench.audio.DecodedAudio
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.audio.AudioSource
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.speechRecognizerRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/** ML Kit GenAI Speech Recognition. advanced=false => MODE_BASIC, true => MODE_ADVANCED. */
class MlKitEngine(private val context: Context, private val advanced: Boolean) : TranscriptionEngine {
    override val name = "ML Kit GenAI"
    private val variant get() = if (advanced) "ADVANCED" else "BASIC"

    override suspend fun transcribe(audio: DecodedAudio, lang: Lang): EngineResult = withContext(Dispatchers.IO) {
        val base = EngineResult(name, variant, lang.code, null, audio.durationMs, 0, 0, "")
        val tag = lang.mlKitTag ?: return@withContext base.copy(error = "Bu dil ML Kit'te yok")

        val options = speechRecognizerOptions {
            locale = Locale.forLanguageTag(tag)
            preferredMode = if (advanced) SpeechRecognizerOptions.Mode.MODE_ADVANCED
            else SpeechRecognizerOptions.Mode.MODE_BASIC
        }
        val recognizer = SpeechRecognition.getClient(options)
        val pcmFile = File(context.cacheDir, "mlkit_input.pcm")
        try {
            val t0 = SystemClock.elapsedRealtime()
            when (val status = recognizer.checkStatus()) {
                FeatureStatus.AVAILABLE -> Unit
                FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                    val final = recognizer.download()
                        .first { it is DownloadStatus.DownloadCompleted || it is DownloadStatus.DownloadFailed }
                    if (final is DownloadStatus.DownloadFailed) {
                        return@withContext base.copy(error = "Model indirilemedi: $final")
                    }
                }
                else -> return@withContext base.copy(error = "Cihazda desteklenmiyor (status=$status)")
            }
            val loadMs = SystemClock.elapsedRealtime() - t0

            audio.writePcm16(pcmFile)
            val pfd = ParcelFileDescriptor.open(pcmFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val request = speechRecognizerRequest { audioSource = AudioSource.fromPfd(pfd) }

            val finals = StringBuilder()
            var error: String? = null
            val t1 = SystemClock.elapsedRealtime()
            pfd.use {
                recognizer.startRecognition(request)
                    .takeWhile { it !is SpeechRecognizerResponse.CompletedResponse }
                    .collect { r ->
                        when (r) {
                            is SpeechRecognizerResponse.FinalTextResponse ->
                                finals.append(r.text.trim()).append(' ')
                            is SpeechRecognizerResponse.ErrorResponse -> error = r.e.message ?: r.e.toString()
                            else -> Unit
                        }
                    }
            }
            val ms = SystemClock.elapsedRealtime() - t1
            base.copy(loadMs = loadMs, transcribeMs = ms, text = finals.toString().trim(), error = error)
        } catch (t: Throwable) {
            base.copy(error = t.javaClass.simpleName + ": " + t.message)
        } finally {
            runCatching { recognizer.close() }
            pcmFile.delete()
        }
    }
}
