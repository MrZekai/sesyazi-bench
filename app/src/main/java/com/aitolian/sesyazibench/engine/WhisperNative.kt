package com.aitolian.sesyazibench.engine

/** libsesyazi.so JNI köprüsü (bkz. app/src/main/cpp/sesyazi_jni.c). */
object WhisperNative {
    init { System.loadLibrary("sesyazi") }

    external fun nativeInit(modelPath: String): Long
    external fun nativeFree(ctx: Long)
    external fun nativeSystemInfo(): String
    external fun nativeTranscribe(ctx: Long, pcm: FloatArray, lang: String, threads: Int): ByteArray
}
