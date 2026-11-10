# MuteRead (Android)

Sesli mesajı, ses dosyasını ya da videonun sesini yazıya döker. Görünen ad: **MuteRead**
(tek kaynak `app/src/main/res/values/strings.xml` → `app_name`). Paket: `com.aitolian.muteread`
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
- Reklam: AdMob (ana sayfa native, not ekranı banner; döküm başında tam ekran reklam — yalnız ekranda
  henüz metin yokken; metin gelirse o dökümde reklam atlanır). Başka uygulamadan paylaşımla gelindiyse
  uygulama açılış (app open) reklamı, uygulama içinden dosya seçildiyse geçiş reklamı gösterilir
  (AdMob: uygulama yüklenirken geçiş reklamı yasak).
- Arayüz dili: kaynak metinler kodda Türkçe; `I18n.kt` tablosu ile İngilizce (`tx("…")`). Yeni dil = yeni tablo.
- Wit anahtar havuzu: `WIT_TOKENS` değerleri tek anahtar ya da liste olabilir (`{"tr":["…","…"],"en":"…"}`).
  Cihaz havuzdan rastgele bir anahtarla başlar; kota (429) ya da yetki hatasında sıradakine geçer.
- Çoklu paylaşım: en fazla 10 ses birlikte paylaşılabilir; sırayla dökülür, her biri ayrı not olur.
- Hata kartında tek dokunuşla başka dilde deneme (telefon dili / İngilizce).
- Paylaşım imzası (Ayarlar'dan kapatılır) ve ödüllü reklamla 24 saat reklamsız kullanım (Ayarlar).
- Uygulama içi puanlama: sorunsuz 3., 12. ve 30. dökümden sonra, not kapatılırken (Play In-App Review).
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
