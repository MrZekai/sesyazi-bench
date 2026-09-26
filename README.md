# Ses → Yazı (Android)

Sesli mesajı, ses dosyasını ya da videonun sesini yazıya döker. Görünen ad henüz kesinleşmedi;
tek kaynak `app/src/main/res/values/strings.xml` → `app_name`. Paket: `com.aitolian.muteread`
(Play'de kayıtlı kimlik; görünen addan bağımsız, toplu değiştirilmez).

## Mimari (v1.22+)

- **Yazıya dökme yalnız Meta Wit.ai** (`/dictation`, dil başına ayrı Wit uygulaması). Anahtarlar
  GitHub secret `WIT_TOKENS` (JSON `{"tr":"…","en":"…"}`) ile derlemede gelir; repoda/logda yok.
  Cihaz içi model, çevrimdışı yedek ve otomatik dil algılama **yok**. İnternet yoksa açık hata.
- Ses sessiz yerlerden ≤ 50 sn parçalara bölünür, en fazla 3 paralel istek; parça başına 3 deneme,
  429'da `Retry-After`'a uyulur (15 sn'den uzunsa yoğunluk hatası). İki turda da dökülemeyen
  aralık notta `[⚠ …]` + yapılandırılmış uyarı olarak işaretlenir, bölüm bazında yeniden dökülebilir.
- Dil: kayıtlı tercih → telefon dili → İngilizce. Ana sayfadan ve not menüsünden değiştirilir.
- Çeviri: Google ML Kit, cihazda.
- Reklam: AdMob (ana sayfa native, not ekranı banner, döküm başında geçiş reklamı — yalnız ekranda
  henüz metin yokken; metin gelirse o dökümde reklam atlanır).
- Kendi sunucumuz yok. Notlar cihazda (en fazla 50).

## Derleme

`main`e her push'ta GitHub Actions çalışır: WIT_TOKENS doğrulama → birim testleri → imzalı
APK + AAB (yükleme anahtarı zorunlu) → 16 KB kapısı (ELF + ZIP hizalama, başarısızsa durur) →
AAB imza denetimi (SHA-256 logda) → `app-<N>` artifact'i (`BUILD-INFO.txt`: commit + SHA-256).

## Dizinler

```
app/src/main/java/.../engine  Wit istemcisi (WitEngine, WitStream), cümle birimleri, çeviri
app/src/main/java/.../audio   MediaCodec çözücü, 16 kHz mono dönüştürme, oynatıcı
app/src/main/java/.../data    Notlar (HistoryStore), dışa aktarma, tercihler
app/src/main/java/.../ui      Compose ekranları
store/                        Gizlilik politikası, mağaza metinleri, Play Console listesi
```
