// whisper.cpp için ince JNI köprüsü.
// Kotlin tarafı: com.aitolian.sesyazibench.engine.WhisperNative (object)
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "whisper.h"
#include "ggml-backend.h"

#ifdef __ANDROID__
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "SesYaziJNI", __VA_ARGS__)
#else
#define LOGI(...) fprintf(stderr, __VA_ARGS__)
#endif

#define JNI_FN(name) Java_com_aitolian_sesyazibench_engine_WhisperNative_##name

static int g_backends_loaded = 0;

/* nativeLibraryDir içindeki libggml-cpu-*.so varyantlarından en uygununu yükler. */
JNIEXPORT jstring JNICALL
JNI_FN(nativeLoadBackends)(JNIEnv *env, jobject thiz, jstring libDir) {
    (void) thiz;
    if (!g_backends_loaded) {
        const char *dir = (*env)->GetStringUTFChars(env, libDir, NULL);
        ggml_backend_load_all_from_path(dir);
        (*env)->ReleaseStringUTFChars(env, libDir, dir);
        g_backends_loaded = 1;
    }
    char buf[512];
    size_t len = 0;
    size_t n = ggml_backend_dev_count();
    len += snprintf(buf + len, sizeof(buf) - len, "devices=%zu", n);
    for (size_t i = 0; i < n && len < sizeof(buf) - 64; i++) {
        ggml_backend_dev_t d = ggml_backend_dev_get(i);
        len += snprintf(buf + len, sizeof(buf) - len, " | %s", ggml_backend_dev_description(d));
    }
    return (*env)->NewStringUTF(env, buf);
}

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

struct progress_ctx {
    JNIEnv *env;
    jobject listener;
    jmethodID method;       // onProgress(I)V
    jmethodID cancelled;    // isCancelled()Z — bir kez çözülür, her adımda tekrar aranmaz
};

/* whisper_full ile aynı iş parçacığında çağrılır; JNIEnv geçerlidir. */
static void on_progress(struct whisper_context *ctx, struct whisper_state *state, int progress, void *user) {
    (void) ctx; (void) state;
    struct progress_ctx *p = (struct progress_ctx *) user;
    if (p && p->listener) (*p->env)->CallVoidMethod(p->env, p->listener, p->method, (jint) progress);
}

/* Kullanıcı iptal ederse whisper_full erken durur. */
static bool on_abort(void *user) {
    struct progress_ctx *p = (struct progress_ctx *) user;
    if (!p || !p->listener || !p->cancelled) return false;
    return (*p->env)->CallBooleanMethod(p->env, p->listener, p->cancelled);
}

/*
 * UTF-8 bayt dizisi döner (NewStringUTF, bölünmüş çok baytlı Türkçe
 * karakterlerde çöker; bu yüzden çözme Kotlin'de yapılır).
 * Çıktı biçimi (satır satır):
 *   LANG\t<dil kodu>
 *   <t0_ms>\t<t1_ms>\t<metin>
 * Hata olursa "ERR\t<kod>" döner.
 * beamSize > 1 ise beam search (daha doğru, daha yavaş), aksi halde greedy.
 * vadPath NULL değilse Silero VAD ile sessizlik/müzik atlanır (halüsinasyonu azaltır);
 * zaman damgaları orijinal ses zamanına geri eşlenir.
 */
JNIEXPORT jbyteArray JNICALL
JNI_FN(nativeTranscribe)(JNIEnv *env, jobject thiz, jlong ctxPtr, jfloatArray pcm,
                         jstring lang, jint threads, jint beamSize, jstring vadPath, jobject listener) {
    (void) thiz;
    struct whisper_context *ctx = (struct whisper_context *) (intptr_t) ctxPtr;
    if (!ctx) return to_bytes(env, "ERR\tno_context");

    const char *language = (*env)->GetStringUTFChars(env, lang, NULL);
    jsize n = (*env)->GetArrayLength(env, pcm);
    jfloat *samples = (*env)->GetFloatArrayElements(env, pcm, NULL);

    const char *vad = vadPath ? (*env)->GetStringUTFChars(env, vadPath, NULL) : NULL;

    struct progress_ctx pctx = { env, listener, NULL, NULL };
    if (listener) {
        jclass cls = (*env)->GetObjectClass(env, listener);
        pctx.method = (*env)->GetMethodID(env, cls, "onProgress", "(I)V");
        pctx.cancelled = (*env)->GetMethodID(env, cls, "isCancelled", "()Z");
        (*env)->DeleteLocalRef(env, cls);
        if (!pctx.method || !pctx.cancelled) { (*env)->ExceptionClear(env); pctx.listener = NULL; }
    }

    struct whisper_full_params p = whisper_full_default_params(
        beamSize > 1 ? WHISPER_SAMPLING_BEAM_SEARCH : WHISPER_SAMPLING_GREEDY);
    if (beamSize > 1) p.beam_search.beam_size = beamSize;
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
    p.suppress_blank = true;
    if (vad) {
        p.vad = true;
        p.vad_model_path = vad;
        p.vad_params = whisper_vad_default_params();
    }
    p.suppress_nst = true;               // [Müzik], (gülüşmeler) gibi etiketleri bastır
    if (pctx.listener) {
        p.progress_callback = on_progress;
        p.progress_callback_user_data = &pctx;
        p.abort_callback = on_abort;
        p.abort_callback_user_data = &pctx;
    }

    int rc = whisper_full(ctx, p, samples, n);
    (*env)->ReleaseFloatArrayElements(env, pcm, samples, JNI_ABORT);
    if (vad) (*env)->ReleaseStringUTFChars(env, vadPath, vad);

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
