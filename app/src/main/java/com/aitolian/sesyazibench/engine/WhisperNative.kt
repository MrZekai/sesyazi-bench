package com.aitolian.sesyazibench.engine

/** JNI'den çağrılır; isimler sesyazi_jni.c ile birebir aynı olmalı. */
interface ProgressListener {
    fun onProgress(percent: Int)
    fun isCancelled(): Boolean
}

/** libsesyazi.so JNI köprüsü (bkz. app/src/main/cpp/sesyazi_jni.c). */
object WhisperNative {
    init { System.loadLibrary("sesyazi") }

    external fun nativeLoadBackends(libDir: String): String
    external fun nativeInit(modelPath: String): Long
    external fun nativeFree(ctx: Long)
    external fun nativeSystemInfo(): String
    external fun nativeTranscribe(
        ctx: Long, pcm: FloatArray, lang: String, threads: Int, audioCtx: Int, listener: ProgressListener?,
    ): ByteArray
}
