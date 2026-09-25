# MuteRead — Play Console kapalı test kontrol listesi

Paket: `com.aitolian.muteread` · Gizlilik politikası: https://mrzekai.github.io/muteread-privacy.html
(Kaynak: `store/muteread-privacy.html`; yayın betiği onu GitHub Pages'e koyar.)

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

## 2. Data safety (taslak — gerçek davranışa göre; son kararı sen ver)

Genel: veriler aktarımda şifreleniyor → **Evet**. Hesap yok.

| Veri türü | Toplanıyor | Paylaşılıyor | Zorunlu mu | Amaç | Not |
|---|---|---|---|---|---|
| Ses → Sesli veya ses kayıtları | Evet | Evet (Meta Wit.ai, modeli geliştirmek için de kullanabiliyor) | İsteğe bağlı (Telefonda modu var) | Uygulama işlevselliği | Meta'da ≤ 90 gün |
| Cihaz veya diğer kimlikler | Evet (AdMob) | Evet (AdMob) | Zorunlu | Reklam, analiz, dolandırıcılık önleme | Google'ın AdMob veri açıklaması rehberine göre doldur |
| Konum → Yaklaşık konum | Evet (AdMob, IP) | Evet | Zorunlu | Reklam | AdMob rehberi |
| Uygulama etkinliği → Uygulama etkileşimleri | Evet (AdMob) | Evet | Zorunlu | Reklam, analiz | AdMob rehberi |
| Uygulama bilgileri ve performans → Tanılama | Evet (AdMob) | Hayır | Zorunlu | Analiz | AdMob rehberi |

Toplanmayanlar: ad, e-posta, kişiler, fotoğraf, dosya içeriği (ses dışında), mesajlar, konum (hassas), finans, sağlık.
Notlar ve metinler yalnız telefonda → **toplanmıyor**. Uygulama içi çeviri cihazda.
Silme talebi: notlar kullanıcı tarafından uygulamada silinir; Meta/Google'daki veriler onların politikalarına tabi.

AdMob için resmi rehber: https://developers.google.com/admob/android/privacy/play-data-disclosure

## 3. Kapalı test sürümü

1. Test ve yayın → Test etme → **Kapalı test** → kanal (ör. "Alpha") → **Testçiler**: e-posta listesi ya da Google Grubu (hesabın yeni kişisel hesapsa en az 12 testçi, 14 gün kesintisiz katılım gerekebilir — Console'daki koşulu esas al).
2. **Yeni sürüm oluştur** → Play App Signing'i kabul et → CI'dan `MuteRead-<numara>.aab` yükle.
3. Sürüm notu (TR): "İlk kapalı test: sesli mesajı yazıya çevirme, not, çeviri."
4. Ülkeler: Türkiye (+ testçilerin olduğu ülkeler).
5. İncelemeye gönder. Onaydan sonra testçi bağlantısını paylaş.

Her yeni yüklemede `versionCode` = CI çalıştırma numarası (otomatik artar).

## 4. Mağaza görselleri (gerçek ekranlardan, TR ve EN)

1. "Dinleyemiyorsan, oku." — büyük, okunur bir transkript.
2. "Paylaş → MuteRead → Oku." — gerçek paylaşım akışı (WhatsApp logosu YOK).
3. "Kopyala, düzelt, sakla." — not düzenleme ve arama.
4. "Metni kendi dilinde oku." — çeviri (kaliteyi abartmadan).
5. "İstersen telefonda işle." — model indirme gereksinimi küçük ama okunur.

Kullanma: "%100 doğru", "en iyi", sahte yıldız/yorum, ölçülmemiş süre.

## 5. Kapalı test ölçüm satırı

`cihaz / RAM / Android / dil / kaynak türü / ses süresi / ilk metin sn / toplam sn / yerel geçiş / eksik kelime / çökme-ANR / reklam gösterildi mi`

Durdurma ölçütü: çökme/ANR, yanlış nota metin yazılması, izin verilmeden yükleme, sessiz metin kaybı.
