// whisper.cpp için ince JNI köprüsü.
// Kotlin tarafı: com.aitolian.sesyazibench.engine.WhisperNative (object)
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "whisper.h"

#ifdef __ANDROID__
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "SesYaziJNI", __VA_ARGS__)
#else
#define LOGI(...) fprintf(stderr, __VA_ARGS__)
#endif

#define JNI_FN(name) Java_com_aitolian_sesyazibench_engine_WhisperNative_##name

JNIEXPORT jlong JNICALL
JNI_FN(nativeInit)(JNIEnv *env, jobject thiz, jstring modelPath) {
    (void) thiz;
    const char *path = (*env)->GetStringUTFChars(env, modelPath, NULL);
    struct whisper_context_params cparams = whisper_context_default_params();
    cparams.use_gpu = false;
    struct whisper_context *ctx = whisper_init_from_file_with_params(path, cparams);
    LOGI("whisper init %s -> %p", path, (void *) ctx);
    (*env)->ReleaseStringUTFChars(env, modelPath, path);
    return (jlong) (intptr_t) ctx;
}

JNIEXPORT void JNICALL
JNI_FN(nativeFree)(JNIEnv *env, jobject thiz, jlong ctxPtr) {
    (void) env; (void) thiz;
    struct whisper_context *ctx = (struct whisper_context *) (intptr_t) ctxPtr;
    if (ctx) whisper_free(ctx);
}

JNIEXPORT jstring JNICALL
JNI_FN(nativeSystemInfo)(JNIEnv *env, jobject thiz) {
    (void) thiz;
    return (*env)->NewStringUTF(env, whisper_print_system_info());
}

static jbyteArray to_bytes(JNIEnv *env, const char *s) {
    jsize n = (jsize) strlen(s);
    jbyteArray arr = (*env)->NewByteArray(env, n);
    (*env)->SetByteArrayRegion(env, arr, 0, n, (const jbyte *) s);
    return arr;
}

/*
 * UTF-8 bayt dizisi döner (NewStringUTF, bölünmüş çok baytlı Türkçe
 * karakterlerde çöker; bu yüzden çözme Kotlin'de yapılır).
 * Çıktı biçimi (satır satır):
 *   LANG\t<dil kodu>
 *   <t0_ms>\t<t1_ms>\t<metin>
 * Hata olursa "ERR\t<kod>" döner.
 */
JNIEXPORT jbyteArray JNICALL
JNI_FN(nativeTranscribe)(JNIEnv *env, jobject thiz, jlong ctxPtr, jfloatArray pcm,
                         jstring lang, jint threads) {
    (void) thiz;
    struct whisper_context *ctx = (struct whisper_context *) (intptr_t) ctxPtr;
    if (!ctx) return to_bytes(env, "ERR\tno_context");

    const char *language = (*env)->GetStringUTFChars(env, lang, NULL);
    jsize n = (*env)->GetArrayLength(env, pcm);
    jfloat *samples = (*env)->GetFloatArrayElements(env, pcm, NULL);

    struct whisper_full_params p = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    p.print_realtime = false;
    p.print_progress = false;
    p.print_timestamps = false;
    p.print_special = false;
    p.translate = false;
    p.no_context = true;
    p.single_segment = false;
    p.n_threads = threads;
    p.language = language;               // "auto" => otomatik algılama
    p.detect_language = false;

    int rc = whisper_full(ctx, p, samples, n);
    (*env)->ReleaseFloatArrayElements(env, pcm, samples, JNI_ABORT);

    if (rc != 0) {
        (*env)->ReleaseStringUTFChars(env, lang, language);
        char err[32];
        snprintf(err, sizeof(err), "ERR\t%d", rc);
        return to_bytes(env, err);
    }

    int segs = whisper_full_n_segments(ctx);
    size_t cap = 256;
    for (int i = 0; i < segs; i++) cap += strlen(whisper_full_get_segment_text(ctx, i)) + 48;
    char *out = (char *) malloc(cap);
    if (!out) {
        (*env)->ReleaseStringUTFChars(env, lang, language);
        return to_bytes(env, "ERR\toom");
    }

    size_t len = 0;
    const char *detected = whisper_lang_str(whisper_full_lang_id(ctx));
    len += snprintf(out + len, cap - len, "LANG\t%s\n", detected ? detected : "?");
    for (int i = 0; i < segs; i++) {
        long long t0 = (long long) whisper_full_get_segment_t0(ctx, i) * 10; // 10 ms birim -> ms
        long long t1 = (long long) whisper_full_get_segment_t1(ctx, i) * 10;
        const char *text = whisper_full_get_segment_text(ctx, i);
        len += snprintf(out + len, cap - len, "%lld\t%lld\t%s\n", t0, t1, text);
    }

    (*env)->ReleaseStringUTFChars(env, lang, language);
    jbyteArray result = to_bytes(env, out);
    free(out);
    return result;
}
