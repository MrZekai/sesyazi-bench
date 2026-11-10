# Play Console kapalı test kontrol listesi — MuteRead

Paket: `com.aitolian.muteread` · Gizlilik politikası: https://mrzekai.github.io/muteread-privacy.html
(Kaynak: `store/muteread-privacy.html`; ayrı Pages deposuna (MrZekai/mrzekai.github.io) elle kopyalanıp itilir. Kasım 2026 denetiminde yayındaki sayfa 25 Eylül tarihli, depodaki 26 Eylül: yeniden yayınla. **Yayında olduğunu tarayıcıda, gizli sekmede aç ve HTTP 200 + doğru paket adıyla doğrula** — yerel dosya kanıt değildir.)

## 1. Uygulama içeriği (App content)

| Bölüm | Cevap |
|---|---|
| Gizlilik politikası | `https://mrzekai.github.io/muteread-privacy.html` |
| Reklamlar | Evet, reklam içeriyor |
| Uygulama erişimi | Tüm işlevler giriş gerektirmeden kullanılabilir |
| Hedef kitle | 13+ (çocuklara yönelik değil) |
| İçerik derecelendirme | Anketi doldur: kullanıcılar arası iletişim YOK, kullanıcı içeriği paylaşımı YOK (yalnız kendi dosyası) |
| Haber uygulaması / Sağlık / Finans | Hayır |
| Hükümet uygulaması | Hayır |
| Reklam kimliği (Advertising ID) | **Evet** kullanıyor (AdMob SDK `AD_ID` iznini ekler) → amaç: Reklamcılık veya pazarlama, Analiz |
| Kategori | Verimlilik (Productivity); etiketler: sesi yazıya çevirme / transkripsiyon. Alternatif: Araçlar. Yayından sonra sıralamaya göre gözden geçir |
| İletişim bilgileri | E-posta: aitolianrock@gmail.com (mağaza sayfasında görünür) |

## 2. Data safety (taslak — gerçek davranışa göre; son kararı sen ver)

Genel: veriler aktarımda şifreleniyor → **Evet**. Hesap yok.

| Veri türü | Toplanıyor | Paylaşılıyor | Zorunlu mu | Amaç | Not |
|---|---|---|---|---|---|
| Ses → Sesli veya ses kayıtları | Evet | Evet (Meta Wit.ai, modeli geliştirmek için de kullanabiliyor) | Zorunlu (yazıya dökme yalnız Wit.ai ile) | Uygulama işlevselliği | Meta'da ≤ 90 gün |
| Cihaz veya diğer kimlikler | Evet (AdMob) | Evet (AdMob) | Zorunlu | Reklam, analiz, dolandırıcılık önleme | Google'ın AdMob veri açıklaması rehberine göre doldur |
| Konum → Yaklaşık konum | Evet (AdMob, IP) | Evet | Zorunlu | Reklam | AdMob rehberi |
| Uygulama etkinliği → Uygulama etkileşimleri | Evet (AdMob) | Evet | Zorunlu | Reklam, analiz | AdMob rehberi |
| Uygulama bilgileri ve performans → Tanılama | Evet (AdMob) | AdMob rehberine göre (kör 'Hayır' yazma) | Zorunlu | Analiz | Projedeki SDK 24.4.0 için rehberi ayrıca eşleştir |

Toplanmayanlar: ad, e-posta, kişiler, fotoğraf, dosya içeriği (ses dışında), mesajlar, konum (hassas), finans, sağlık.
Notlar ve metinler yalnız telefonda → **toplanmıyor**. Uygulama içi çeviri cihazda.
Silme talebi: notlar kullanıcı tarafından uygulamada silinir; Meta/Google'daki veriler onların politikalarına tabi.

AdMob için resmi rehber: https://developers.google.com/admob/android/privacy/play-data-disclosure

## 3. Kapalı test sürümü

1. Test ve yayın → Test etme → **Kapalı test** → kanal (ör. "Alpha") → **Testçiler**: e-posta listesi ya da Google Grubu (hesabın yeni kişisel hesapsa en az 12 testçi, 14 gün kesintisiz katılım gerekebilir — Console'daki koşulu esas al).
2. **Yeni sürüm oluştur** → Play App Signing'i kabul et → CI artifact'inden (`app-<numara>`) `app-<numara>.aab` yükle.
3. Sürüm notu (TR): "İlk kapalı test: sesli mesajı yazıya çevirme, not, çeviri."
4. Ülkeler: Türkiye (+ testçilerin olduğu ülkeler).
5. İncelemeye gönder. Onaydan sonra testçi bağlantısını paylaş.

Her yeni yüklemede `versionCode` = CI çalıştırma numarası (otomatik artar).

Gerçek reklam kimlikleri (GitHub → Settings → Secrets → Actions): `ADMOB_APP_ID`, `ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`, `ADMOB_NATIVE_ID`, `ADMOB_APP_OPEN_ID` (v1.24: AdMob'da "Uygulama açılış" reklam birimi aç). Secret yoksa test kimlikleri kullanılır.

v1.24'ten itibaren yayın paketi R8 ile küçültülür: yüklemeden önce CI APK'sını telefonda dene (paylaş → metin, çeviri, reklam, dışa aktarma).

## 4. Mağaza görselleri (gerçek ekranlardan, TR ve EN)

1. "Dinleyemiyorsan, oku." — büyük, okunur bir transkript.
2. "Paylaş → MuteRead → Oku." — gerçek paylaşım akışı (WhatsApp logosu YOK).
3. "Kopyala, düzelt, sakla." — not düzenleme ve arama.
4. "Metni kendi dilinde oku." — çeviri (kaliteyi abartmadan).
5. "Saniyeler içinde." — gerçek bir dökümün süre satırı (ör. "⚡ 2,8 sn"), ölçülmüş değer.

Kullanma: "%100 doğru", "en iyi", sahte yıldız/yorum, ölçülmemiş süre, "Hemen indir" gibi çağrılar, süreli slogan.

ASO denetimi (marketing-skills:aso) ekleri:

- **Öne çıkan görsel (feature graphic) zorunlu:** tam 1024 × 500 px, JPEG ya da 24 bit PNG (alfa yok). Yoksa öne çıkarma yerleşimlerine girilemez.
- **Sıra:** kullanıcıların çoğu 3. görselden sonrasına bakmaz. İlk üç görsel ana hikâyeyi taşımalı: 1) Dinleyemiyorsan oku, 2) Paylaş → MuteRead → Oku, 3) Saniyeler içinde (şimdiki 5. görsel 3. sıraya).
- **Adet:** en az 5, en fazla 8 telefon görseli; önerilen 1080 × 1920, her görselde tek mesaj, 5–7 kelimelik başlık.
- **Simge:** 512 × 512, 32 bit PNG; içinde yazı olmasın. Simge zemini hâlâ mor (`ic_bg` #7C5CFF), uygulama vurgusu mavi (#3B5BDB): bilinçli değilse eşitle.
- **Tanıtım videosu:** 30 sn – 2 dk YouTube bağlantısı; sessiz otomatik oynayabildiği için ilk saniyelerde paylaş → metin akışı sessiz anlaşılmalı. İsteğe bağlı, yayından sonra deneyle ölç.
- Görseller her yayınlanan dil için yerelleştirilmeli.

## 5. Kapalı test ölçüm satırı

`cihaz / RAM / Android / dil / kaynak türü / ses süresi / ilk metin sn / toplam sn / Wit hata kodu / eksik kelime / çökme-ANR / reklam gösterildi mi`

Durdurma ölçütü: çökme/ANR, yanlış nota metin yazılması, izin verilmeden yükleme, sessiz metin kaybı.
