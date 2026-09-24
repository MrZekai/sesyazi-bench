// whisper_get_timings() sonucu C++ `new` ile ayrılıyor; C'den free() edilemez.
// Bu küçük yardımcı değerleri kopyalar ve belleği doğru şekilde (delete) bırakır.
#include "whisper.h"

extern "C" void sesyazi_read_timings(struct whisper_context *ctx, float out[5]) {
    for (int i = 0; i < 5; i++) out[i] = 0.0f;
    whisper_timings *t = whisper_get_timings(ctx);
    if (!t) return;
    // Hepsi çağrı başına ORTALAMA (ms): örnekleme, encoder, decoder, toplu decoder, prompt
    out[0] = t->sample_ms;
    out[1] = t->encode_ms;
    out[2] = t->decode_ms;
    out[3] = t->batchd_ms;
    out[4] = t->prompt_ms;
    delete t;
}
