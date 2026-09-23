package com.aitolian.sesyazibench.engine

/** JNI'den çağrılır; isimler sesyazi_jni.c ile birebir aynı olmalı. */
interface ProgressListener {
    fun onProgress(percent: Int)
    fun isCancelled(): Boolean
    /** Yeni çözülen cümle (UTF-8 bayt; bölünmüş karakterler Kotlin'de güvenle çözülür). */
    fun onSegment(startMs: Long, endMs: Long, text: ByteArray)
}

/** libsesyazi.so JNI köprüsü (bkz. app/src/main/cpp/sesyazi_jni.c). */
object WhisperNative {
    init { System.loadLibrary("sesyazi") }

    external fun nativeLoadBackends(libDir: String): String
    external fun nativeInit(modelPath: String): Long
    external fun nativeFree(ctx: Long)
    external fun nativeSystemInfo(): String
    external fun nativeTranscribe(
        ctx: Long, pcm: FloatArray, lang: String, threads: Int, beamSize: Int, vadModelPath: String?,
        listener: ProgressListener?,
    ): ByteArray
}
