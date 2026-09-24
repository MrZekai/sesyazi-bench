# SesYazı (V1 prototip)

"Ses → Yazı" uygulaması için **10 günlük fizibilite prototipi**. Amaç tek bir soruyu cevaplamak:

> 60 saniyelik Türkçe sesli mesaj, orta segment bir telefonda **15 saniyenin altında** okunur metne dönüşüyor mu?

**Güncel mimari (v1.11+):** Yazıya dökme varsayılan olarak **Meta Wit.ai** (ücretsiz, dil başına ayrı Wit uygulaması; anahtarlar GitHub secret `WIT_TOKENS` ile derlemede gelir) üzerinden yapılır — ses Meta'ya gönderilir. İnternet yoksa, anahtar/kota hatasında ya da Ayarlar'da "Telefonda" seçiliyse cihaz içi whisper.cpp kullanılır. Kendi sunucumuz yok.

## Karşılaştırılan motorlar

| Motor | Ne | Not |
|---|---|---|
| whisper.cpp v1.9.4 | tiny / base / small (q5_1), large-v3-turbo (q5_0) | Model uygulama içinden bir kez indirilir (Hugging Face) |
| ML Kit GenAI — BASIC | Android'in cihaz içi tanıyıcısı | `1.0.0-alpha1`, Türkçe beta |
| ML Kit GenAI — ADVANCED (“ML Kit+”) | Gemini Nano tabanlı | Yalnız destekleyen üst segment cihazlar |

## APK nasıl alınır

`main`e her push'ta GitHub Actions **Build benchmark APK** iş akışı çalışır → Actions sekmesinde çalıştırmaya gir → *Artifacts* altından `SesYaziBench-apk-N` indir → zip içindeki APK'yı telefona kur.

APK debug anahtarıyla imzalıdır; **Play'e yüklenmez**, sadece test içindir.

## Test protokolü

1. Uygulamada modeli seç ve **İndir** (önce `base`, sonra `small`).
2. WhatsApp'ta bir sesli mesaja uzun bas → **Paylaş** → **SesYazı Bench**.
3. Dili seç (Türkçe) → **Hepsini sırayla çalıştır**.
4. Her dosya için tekrarla. Hedef seti:
   - 10 × Türkçe (5–30 sn), 10 × Türkçe (45–90 sn), 5 × gürültülü ortam, 5 × başka dil
5. **Sonuçları paylaş (CSV)** → dosyayı Claude'a gönder.

Kart renkleri: **GEÇTİ** = RTF ≤ 0,25 · **YAVAŞ** = hedefin üstünde · **HATA** = motor çalışmadı.

## Karar kriteri

- base veya small, orta segment cihazda Türkçe için RTF ≤ 0,25 **ve** metin okunur → ürüne geç.
- Sadece tiny hedefi tutturuyor ama kalite zayıf → dil setini daralt / ML Kit'i ana motor yap.
- Hiçbiri tutmuyor → fikri durdur.

## Yapı

```
app/src/main/cpp/            whisper.cpp JNI köprüsü (CMake FetchContent ile v1.9.4)
app/src/main/java/.../audio  MediaCodec ile her formatı 16 kHz mono'ya çözme
app/src/main/java/.../engine whisper + ML Kit motorları, model indirme
app/src/main/java/.../ui     Compose ekranı
```
