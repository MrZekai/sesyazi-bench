package com.aitolian.sesyazibench

/*
 * Arayüz çevirileri (I18n.kt içindeki tx() kullanır). Anahtar = koddaki Türkçe kaynak metin.
 * Bu dosya tools/l10n veri dosyalarından üretilir; elle düzenlenebilir.
 * Boş değer: metin o dilde bir önceki parçayla birleştirilmiştir.
 */
internal object I18nTables {
    val DE: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "Audio wird vorbereitet…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "Datei ist zu groß (max. {0} MB)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "Audio ist zu kurz (mindestens eine halbe Sekunde)",
            "Ses açılamadı" to
                "Audio konnte nicht geöffnet werden",
            "Dosya bu telefon için çok büyük" to
                "Diese Datei ist für dieses Handy zu groß",
            "Ses açılamadı: {0}" to
                "Audio konnte nicht geöffnet werden: {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "Das Audio dieser Notiz ist nicht mehr vorhanden; teile es erneut",
            "Hazırlanıyor…" to
                "Wird vorbereitet…",
            "Yazıya dökülemedi: {0}" to
                "Transkription fehlgeschlagen: {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "Schwache oder keine Internetverbindung. Prüfe die Verbindung und versuche es erneut.",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "Der Dienst ist gerade ausgelastet. Versuche es in ein paar Sekunden erneut.",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "Der Transkriptionsdienst für {0} ist gerade nicht erreichbar. Aktualisiere die App und versuche es erneut.",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "Der Transkriptionsdienst antwortet nicht. Versuche es gleich noch einmal.",
            "Yazıya dökülemedi. Tekrar dene." to
                "Transkription fehlgeschlagen. Versuche es erneut.",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "Internetverbindung erforderlich. Verbinde dich und versuche es erneut.",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "Transkription ist für {0} derzeit nicht verfügbar. Ändere die Sprache der Aufnahme.",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ {0}–{1} konnte nicht transkribiert werden]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "Keine Sprache erkannt. Ist die Sprache der Aufnahme {0}? Falls nicht, ändere die Sprache und versuche es erneut.",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "Text ist fertig, konnte aber nicht gespeichert werden (Speicher evtl. voll)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "Einige Teile konnten nicht transkribiert werden; sie sind in der Notiz markiert",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "Ein Teil konnte nicht vollständig bestätigt werden; er ist unter der Notiz markiert",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "Teile einer bearbeiteten Notiz können nicht erneut transkribiert werden",
            "Önce mevcut işlem bitsin" to
                "Warte, bis der aktuelle Vorgang fertig ist",
            "Bu dil için hızlı mod yok" to
                "Transkription ist für diese Sprache nicht verfügbar",
            "Bunun için internet bağlantısı gerekiyor" to
                "Dafür ist eine Internetverbindung nötig",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "Teil wird erneut transkribiert ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "Teil konnte nicht erneut transkribiert werden; vorhandener Text bleibt",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "Erneut transkribiert, aber der Teil konnte weiterhin nicht vollständig bestätigt werden",
            "Bölüm yeniden döküldü" to
                "Teil erneut transkribiert",
            "Geliştirici modu kapatıldı" to
                "Entwicklermodus deaktiviert",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "Leerer Text kann nicht gespeichert werden. Lösche die Notiz über das Menü.",
            "Bu not silinmiş" to
                "Diese Notiz wurde gelöscht",
            "Kaydedildi" to
                "Gespeichert",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "Speichern fehlgeschlagen (Speicher evtl. voll). Dein Entwurf ist noch geöffnet.",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "Warte, bis der aktuelle Vorgang fertig ist, oder brich ihn ab",
            "Not silinemedi (depolama hatası)" to
                "Notiz konnte nicht gelöscht werden (Speicherfehler)",
            "Geri alınamadı (depolama hatası)" to
                "Rückgängig nicht möglich (Speicherfehler)",
            "Notlar silinemedi (depolama hatası)" to
                "Notizen konnten nicht gelöscht werden (Speicherfehler)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "Alle Notizen und Dateien in der App wurden gelöscht",
            "Kaynak dil tanınmadı" to
                "Ausgangssprache nicht erkannt",
            "{0} için çeviri desteklenmiyor" to
                "Übersetzung wird für {0} nicht unterstützt",
            "Çeviri yapılamadı: {0}" to
                "Übersetzung fehlgeschlagen: {0}",
            "internet bağlantısını kontrol et" to
                "prüfe deine Internetverbindung",
            "Ses çalınamadı" to
                "Audio konnte nicht abgespielt werden",
            "Dosya boş" to
                "Die Datei ist leer",
            "Döküm tamamlandı" to
                "Transkription abgeschlossen",
            "Metnin hazır" to
                "Dein Text ist fertig",
            "Açmak için dokun" to
                "Zum Öffnen tippen",
            "{0} ile yazıya dök" to
                "Mit {0} transkribieren",
            "Reklam" to
                "Anzeige",
            "Dosya açılamadı" to
                "Datei konnte nicht geöffnet werden",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "Diese Datei konnte nicht gelesen werden (nicht unterstütztes Format)",
            "Bu dosyada ses bulunamadı" to
                "In dieser Datei wurde kein Audio gefunden",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "Derzeit wird Audio bis 30 Minuten unterstützt",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "Dieses Audioformat ({0}) wird auf deinem Handy nicht unterstützt",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "Audio konnte nicht dekodiert werden (Decoder antwortet nicht)",
            "Metin dosyasını paylaş" to
                "Textdatei teilen",
            "Altyazı dosyasını paylaş" to
                "Untertiteldatei teilen",
            "{0} çeviri için desteklenmiyor" to
                "{0} wird für die Übersetzung nicht unterstützt",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "Sprachpaket wird heruntergeladen (einmalig, ~30 MB)…",
            "Çevriliyor {0}/{1}…" to
                "Übersetze {0}/{1}…",
            "Paylaş" to
                "Teilen",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "WhatsApp nicht gefunden; du kannst stattdessen eine Datei wählen",
            "Ana sayfaya dön" to
                "Zur Startseite",
            "Ayarlar" to
                "Einstellungen",
            "Dinleyemiyorsan,\noku." to
                "Kannst nicht zuhören?\nLies es.",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "Halte in WhatsApp eine Sprachnachricht gedrückt → Teilen → {0}. Der Text ist in Sekunden fertig.",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "Teile eine Sprachnachricht mit {0} und lies sie in Sekunden.",
            "WhatsApp'ı aç" to
                "WhatsApp öffnen",
            "Ses ya da video dosyası seç" to
                "Audio- oder Videodatei wählen",
            "Ses / video dosyası seç" to
                "Audio- / Videodatei wählen",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ Transkription startet gleich",
            "Yazıya dökülüyor…" to
                "Wird transkribiert…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ In Sekunden · {0}",
            "Bir sorun oldu" to
                "Etwas ist schiefgelaufen",
            "İptal" to
                "Abbrechen",
            "Tekrar dene" to
                "Erneut versuchen",
            "Konuşma dili: {0} ▾" to
                "Sprache der Aufnahme: {0} ▾",
            "Notlarda ara" to
                "Notizen durchsuchen",
            "Silindi: {0}" to
                "Gelöscht: {0}",
            "Geri al" to
                "Rückgängig",
            "Son notlar" to
                "Letzte Notizen",
            "Tümü ({0})" to
                "Alle ({0})",
            "Silmek için nota basılı tut" to
                "Notiz gedrückt halten, um sie zu löschen",
            "Kılavuzu kapat" to
                "Anleitung ausblenden",
            "Kılavuzu aç" to
                "Anleitung anzeigen",
            "Nasıl kullanılır?" to
                "So funktioniert's",
            "Döküm bitince düzenleyebilirsin" to
                "Du kannst bearbeiten, sobald die Transkription fertig ist",
            "Not metni düzenleme alanı" to
                "Textfeld der Notiz",
            " · düzenlendi" to
                " · bearbeitet",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · Ansicht mit Zeitstempeln nur für unbearbeiteten Text",
            "Vazgeç" to
                "Abbrechen",
            "Kaydet" to
                "Speichern",
            "Kopyala" to
                "Kopieren",
            "Kopyalandı" to
                "Kopiert",
            "Değişiklikler kaydedilmedi" to
                "Nicht gespeicherte Änderungen",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "Wenn du ohne Speichern gehst, gehen deine Änderungen verloren.",
            "Kaydetme" to
                "Nicht speichern",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · {2} Wörter",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "Leseeinstellungen (Textgröße, Zeilenabstand, Design)",
            "Diğer seçenekler" to
                "Weitere Optionen",
            "⚡ Yeniden dök ({0})" to
                "⚡ Erneut transkribieren ({0})",
            "Dil: {0} ile yeniden dök" to
                "Erneut transkribieren auf {0}",
            "Bu notu sil" to
                "Diese Notiz löschen",
            "Okuma ayarları" to
                "Leseeinstellungen",
            "Yazı boyutu" to
                "Textgröße",
            "Yazıyı küçült" to
                "Text verkleinern",
            "Örnek metin · {0}" to
                "Beispieltext · {0}",
            "Yazıyı büyüt" to
                "Text vergrößern",
            "Satır aralığı" to
                "Zeilenabstand",
            "Sıkı" to
                "Eng",
            "Normal" to
                "Normal",
            "Geniş" to
                "Weit",
            "Görünüm" to
                "Ansicht",
            "Paragraf" to
                "Absatz",
            "Zaman damgalı" to
                "Mit Zeitstempeln",
            "Sesle takip" to
                "Audio folgen",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "Während das Audio läuft, wird der aktuelle Satz hervorgehoben und der Bildschirm folgt ihm. Manuelles Scrollen pausiert das Folgen.",
            "Tema" to
                "Design",
            "Sistem" to
                "System",
            "Açık" to
                "Hell",
            "Koyu" to
                "Dunkel",
            "Canlı · ⚡ yazıya dökülüyor" to
                "Live · ⚡ wird transkribiert",
            "Canlı · %{0}" to
                "Live · {0} %",
            "Sesi duraklat" to
                "Audio pausieren",
            "Sesi oynat" to
                "Audio abspielen",
            "Ses dalgası; dokunduğun yere atlar" to
                "Wellenform; tippen zum Springen",
            "Oynatıcıyı daralt" to
                "Player einklappen",
            "Oynatıcıyı genişlet" to
                "Player ausklappen",
            "−5 sn" to
                "−5 s",
            "5 saniye geri" to
                "5 Sekunden zurück",
            "+5 sn" to
                "+5 s",
            "5 saniye ileri" to
                "5 Sekunden vor",
            "Metin" to
                "Text",
            "Çeviri" to
                "Übersetzung",
            "Metin birazdan burada belirecek…" to
                "Der Text erscheint gleich hier…",
            "Canlıya dön" to
                "Zurück zu Live",
            "Okunan yere dön" to
                "Zur aktuellen Stelle",
            "Buradan dinle" to
                "Ab hier anhören",
            "Çevir" to
                "Übersetzen",
            "Google Çeviri'de aç" to
                "In Google Übersetzer öffnen",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "Langer Text: in die Zwischenablage kopiert, füge ihn in Google Übersetzer ein",
            "Google Çeviri'de aç ↗" to
                "In Google Übersetzer öffnen ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "Text wird an Google gesendet · Internet erforderlich",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "Die Übersetzung oben läuft auf deinem Gerät; jedes Sprachpaket wird nur einmal heruntergeladen.",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ Ein Teil konnte nicht transkribiert werden",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ {0} Teile konnten nicht transkribiert werden",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ Ein Teil konnte nicht vollständig bestätigt werden",
            "⚠ {0} bölümde sorun var" to
                "⚠ Probleme in {0} Teilen",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "Dieser Abschnitt fehlt im Text wegen eines Verbindungs- oder Dienstfehlers.",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "Eine Zeile in diesem Abschnitt scheint neu begonnen zu haben, wurde aber nicht bestätigt; Text kann fehlen.",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "Einige Abschnitte konnten nicht transkribiert werden, andere wurden nicht bestätigt; Text kann fehlen.",
            "Bu bölümü yeniden dök" to
                "Diesen Teil erneut transkribieren",
            "Diğer" to
                "Mehr",
            "Metni düzenle" to
                "Text bearbeiten",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "Übersetzung als Datei teilen (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "Als Textdatei teilen (.txt)",
            "Çeviri henüz hazır değil" to
                "Übersetzung ist noch nicht fertig",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "Datei konnte nicht erstellt werden (Speicher evtl. voll)",
            "Çeviri altyazısı (.srt)" to
                "Untertitel der Übersetzung (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "Untertitel (.srt) · Originaltranskript",
            "Altyazı (.srt)" to
                "Untertitel (.srt)",
            "✓ Yazıya döküldü" to
                "✓ Transkribiert",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} s · einige Teile konnten nicht transkribiert werden",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ In {0} s transkribiert",
            "Geri" to
                "Zurück",
            "Notlarım" to
                "Meine Notizen",
            "Notlarda ara…" to
                "Notizen durchsuchen…",
            "Aramayı temizle" to
                "Suche löschen",
            "Henüz not yok." to
                "Noch keine Notizen.",
            "\"{0}\" hiçbir notta geçmiyor." to
                "Keine Notiz enthält \"{0}\".",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "{0}/{1} Notizen. Ist das Limit erreicht, wird bei neuen Notizen die älteste entfernt; exportiere wichtige Notizen.",
            "Notu aç" to
                "Notiz öffnen",
            "Notu sil" to
                "Notiz löschen",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — Sprache-zu-Text-Dienst, Wit.ai-Nutzungsbedingungen\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit Übersetzung — Nutzungsbedingungen der Google APIs\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK, User Messaging Platform — Google-Nutzungsbedingungen\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX, Jetpack Compose, Kotlin — Apache-Lizenz 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "Benachrichtigungen nicht erlaubt; wir können dich nicht informieren, wenn es fertig ist",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "Textgröße, Zeilenabstand und Audio folgen: die Schaltfläche \"Aa\" in der Notiz.",
            "Transkript" to
                "Transkript",
            "Varsayılan konuşma dili" to
                "Standardsprache der Aufnahme",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ Audio wird in Sekunden transkribiert (Internet erforderlich). Aus WhatsApp geteiltes Audio wird in dieser Sprache transkribiert; ist sie anders, ändere die Sprache im Notizmenü.",
            "Varsayılan çeviri dili" to
                "Standardsprache für Übersetzung",
            "İndirilen dil paketleri" to
                "Heruntergeladene Sprachpakete",
            "Yükleniyor…" to
                "Wird geladen…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "Noch keine Sprachpakete. Bei der ersten Übersetzung wird eines automatisch geladen (~30 MB).",
            "Sil" to
                "Löschen",
            "{0} paketi silindi" to
                "Paket {0} gelöscht",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "Die Übersetzung läuft auf deinem Handy; dein Text wird an keinen Server gesendet.",
            "Bildirimler" to
                "Benachrichtigungen",
            "İşlem bitince haber ver" to
                "Benachrichtigen, wenn es fertig ist",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "Wenn du die App verlässt, erhältst du eine Benachrichtigung, sobald eine lange Transkription fertig ist.",
            "Gizlilik" to
                "Datenschutz",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "Audio wird zur Transkription an Metas Dienst Wit.ai gesendet. Meta verarbeitet diese Daten nach eigenen Bedingungen; laut Wit.ai-Richtlinie werden Sprachdaten bis zu 90 Tage gespeichert und können zur Verbesserung der Spracherkennung analysiert werden. Wir betreiben keinen Server; deine Notizen und die temporäre Kopie des Audios bleiben nur auf deinem Handy. Die Übersetzung läuft auf dem Gerät. Anzeigen stammen von Google AdMob. Teile nur Audio, das du transkribieren darfst.",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "Datenschutzerklärung",
            "Tüm notları sil ({0})" to
                "Alle Notizen löschen ({0})",
            "Reklam tercihleri" to
                "Anzeigeneinstellungen",
            "Hakkında" to
                "Info",
            "Sürüm" to
                "Version",
            "Geliştirici araçları açıldı" to
                "Entwicklertools aktiviert",
            "Geri bildirim gönder" to
                "Feedback senden",
            " geri bildirim ({0})" to
                " Feedback ({0})",
            "Açık kaynak lisansları" to
                "Open-Source-Lizenzen",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "Diese App wurde nicht von WhatsApp oder Meta entwickelt, unterstützt oder gesponsert. WhatsApp ist eine Marke der WhatsApp LLC. Die Transkription nutzt Metas Dienst Wit.ai.",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "App bewerten",
            "Tüm notlar silinsin mi?" to
                "Alle Notizen löschen?",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "Alle Notizen, exportierten Dateien und Messprotokolle auf diesem Handy werden gelöscht. Eine laufende Transkription wird gestoppt. Das kann nicht rückgängig gemacht werden.",
            "Kapat" to
                "Schließen",
            "Sesli mesaja uzun bas" to
                "Sprachnachricht gedrückt halten",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "Im WhatsApp-Chat, bis die Nachricht ausgewählt ist",
            "Paylaş simgesine dokun" to
                "Auf das Teilen-Symbol tippen",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "In der oberen Leiste (bei manchen Handys im Menü ⋮)",
            "Listeden {0} uygulamasını seç" to
                "{0} aus der Liste wählen",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "Der Text wird sofort erstellt. Beim nächsten Mal steht die App weiter oben in der Liste.",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "{0} Audiodateien transkribiert; alle sind in deinen Notizen",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "{0} Audiodateien transkribiert, {1} fehlgeschlagen",
            "{0} ile yazıya döküldü" to
                "Transkribiert mit {0}",
            "Ses hazırlanıyor… ({0}/{1})" to
                "Audio wird vorbereitet… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "Andere Sprache? Mit einem Tipp versuchen:",
            "Paylaşırken imza ekle" to
                "Beim Teilen Signatur hinzufügen",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "Name und Link der App werden unter den geteilten Text gesetzt.",
            "Reklamsız kullanım" to
                "Werbefrei nutzen",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "Werbung ist aus. Verbleibend: etwa {0} Stunden.",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "Sieh dir eine kurze Anzeige an und nutze die App 24 Stunden ohne Werbung.",
            "Reklam izle · 24 saat reklamsız" to
                "Anzeige ansehen · 24 Std. werbefrei",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "24 Stunden keine Werbung. Danke!",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "Sieh die Anzeige bis zum Ende, um die Belohnung zu erhalten.",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "Die Anzeige konnte gerade nicht geladen werden. Versuche es gleich noch einmal.",
        )
    }

    val FR: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "Préparation de l'audio…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "Fichier trop volumineux (max. {0} Mo)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "Audio trop court (au moins une demi-seconde)",
            "Ses açılamadı" to
                "Impossible d'ouvrir l'audio",
            "Dosya bu telefon için çok büyük" to
                "Ce fichier est trop volumineux pour ce téléphone",
            "Ses açılamadı: {0}" to
                "Impossible d'ouvrir l'audio : {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "L'audio de cette note n'est plus disponible ; partagez-le à nouveau",
            "Hazırlanıyor…" to
                "Préparation…",
            "Yazıya dökülemedi: {0}" to
                "Transcription impossible : {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "Connexion Internet faible ou absente. Vérifiez votre connexion et réessayez.",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "Le service est occupé pour le moment. Réessayez dans quelques secondes.",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "Le service de transcription pour {0} est injoignable pour le moment. Mettez l'appli à jour et réessayez.",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "Le service de transcription ne répond pas. Réessayez dans un instant.",
            "Yazıya dökülemedi. Tekrar dene." to
                "Transcription impossible. Réessayez.",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "Une connexion Internet est nécessaire. Connectez-vous et réessayez.",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "La transcription n'est pas disponible pour {0} pour le moment. Changez la langue parlée.",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ {0}–{1} n'a pas pu être transcrit]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "Aucune parole détectée. La langue parlée est-elle {0} ? Sinon, changez la langue et réessayez.",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "Le texte est prêt mais n'a pas pu être enregistré (stockage peut-être plein)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "Certaines parties n'ont pas pu être transcrites ; elles sont signalées dans la note",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "Une partie n'a pas pu être entièrement vérifiée ; elle est signalée sous la note",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "Les parties d'une note modifiée ne peuvent pas être retranscrites",
            "Önce mevcut işlem bitsin" to
                "Attendez la fin de la tâche en cours",
            "Bu dil için hızlı mod yok" to
                "La transcription n'est pas disponible pour cette langue",
            "Bunun için internet bağlantısı gerekiyor" to
                "Une connexion Internet est nécessaire pour cela",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "Nouvelle transcription de la partie ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "Impossible de retranscrire la partie ; le texte existant est conservé",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "Retranscrit, mais la partie n'a toujours pas pu être entièrement vérifiée",
            "Bölüm yeniden döküldü" to
                "Partie retranscrite",
            "Geliştirici modu kapatıldı" to
                "Mode développeur désactivé",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "Un texte vide ne peut pas être enregistré. Utilisez le menu pour supprimer la note.",
            "Bu not silinmiş" to
                "Cette note a été supprimée",
            "Kaydedildi" to
                "Enregistré",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "Enregistrement impossible (stockage peut-être plein). Votre brouillon reste ouvert.",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "Attendez la fin de la tâche en cours ou annulez-la",
            "Not silinemedi (depolama hatası)" to
                "Impossible de supprimer la note (erreur de stockage)",
            "Geri alınamadı (depolama hatası)" to
                "Annulation impossible (erreur de stockage)",
            "Notlar silinemedi (depolama hatası)" to
                "Impossible de supprimer les notes (erreur de stockage)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "Toutes les notes et tous les fichiers de l'appli ont été supprimés",
            "Kaynak dil tanınmadı" to
                "Langue source non reconnue",
            "{0} için çeviri desteklenmiyor" to
                "La traduction n'est pas prise en charge pour {0}",
            "Çeviri yapılamadı: {0}" to
                "Traduction impossible : {0}",
            "internet bağlantısını kontrol et" to
                "vérifiez votre connexion Internet",
            "Ses çalınamadı" to
                "Impossible de lire l'audio",
            "Dosya boş" to
                "Le fichier est vide",
            "Döküm tamamlandı" to
                "Transcription terminée",
            "Metnin hazır" to
                "Votre texte est prêt",
            "Açmak için dokun" to
                "Touchez pour ouvrir",
            "{0} ile yazıya dök" to
                "Transcrire avec {0}",
            "Reklam" to
                "Annonce",
            "Dosya açılamadı" to
                "Impossible d'ouvrir le fichier",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "Ce fichier n'a pas pu être lu (format non pris en charge)",
            "Bu dosyada ses bulunamadı" to
                "Aucun audio trouvé dans ce fichier",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "Pour l'instant, l'audio est limité à 30 minutes",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "Ce format audio ({0}) n'est pas pris en charge sur votre téléphone",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "Impossible de décoder l'audio (le décodeur ne répond pas)",
            "Metin dosyasını paylaş" to
                "Partager le fichier texte",
            "Altyazı dosyasını paylaş" to
                "Partager le fichier de sous-titres",
            "{0} çeviri için desteklenmiyor" to
                "{0} n'est pas pris en charge pour la traduction",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "Téléchargement du pack de langue (une seule fois, ~30 Mo)…",
            "Çevriliyor {0}/{1}…" to
                "Traduction {0}/{1}…",
            "Paylaş" to
                "Partager",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "WhatsApp introuvable ; vous pouvez choisir un fichier à la place",
            "Ana sayfaya dön" to
                "Retour à l'accueil",
            "Ayarlar" to
                "Paramètres",
            "Dinleyemiyorsan,\noku." to
                "Impossible d'écouter ?\nLisez.",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "Dans WhatsApp, appuyez longuement sur un message vocal → Partager → {0}. Le texte est prêt en quelques secondes.",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "Partagez un message vocal avec {0} et lisez-le en quelques secondes.",
            "WhatsApp'ı aç" to
                "Ouvrir WhatsApp",
            "Ses ya da video dosyası seç" to
                "Choisir un fichier audio ou vidéo",
            "Ses / video dosyası seç" to
                "Choisir un fichier audio / vidéo",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ Transcription dans un instant",
            "Yazıya dökülüyor…" to
                "Transcription…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ En quelques secondes · {0}",
            "Bir sorun oldu" to
                "Un problème est survenu",
            "İptal" to
                "Annuler",
            "Tekrar dene" to
                "Réessayer",
            "Konuşma dili: {0} ▾" to
                "Langue parlée : {0} ▾",
            "Notlarda ara" to
                "Rechercher dans les notes",
            "Silindi: {0}" to
                "Supprimé : {0}",
            "Geri al" to
                "Annuler",
            "Son notlar" to
                "Notes récentes",
            "Tümü ({0})" to
                "Tout ({0})",
            "Silmek için nota basılı tut" to
                "Appuyez longuement sur une note pour la supprimer",
            "Kılavuzu kapat" to
                "Masquer le guide",
            "Kılavuzu aç" to
                "Afficher le guide",
            "Nasıl kullanılır?" to
                "Comment ça marche",
            "Döküm bitince düzenleyebilirsin" to
                "Vous pourrez modifier une fois la transcription terminée",
            "Not metni düzenleme alanı" to
                "Zone d'édition du texte de la note",
            " · düzenlendi" to
                " · modifié",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · la vue horodatée est disponible pour le texte non modifié",
            "Vazgeç" to
                "Annuler",
            "Kaydet" to
                "Enregistrer",
            "Kopyala" to
                "Copier",
            "Kopyalandı" to
                "Copié",
            "Değişiklikler kaydedilmedi" to
                "Modifications non enregistrées",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "Si vous quittez sans enregistrer, vos modifications seront perdues.",
            "Kaydetme" to
                "Ne pas enregistrer",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · {2} mots",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "Réglages de lecture (taille du texte, interligne, thème)",
            "Diğer seçenekler" to
                "Plus d'options",
            "⚡ Yeniden dök ({0})" to
                "⚡ Transcrire à nouveau ({0})",
            "Dil: {0} ile yeniden dök" to
                "Transcrire à nouveau en {0}",
            "Bu notu sil" to
                "Supprimer cette note",
            "Okuma ayarları" to
                "Réglages de lecture",
            "Yazı boyutu" to
                "Taille du texte",
            "Yazıyı küçült" to
                "Réduire le texte",
            "Örnek metin · {0}" to
                "Exemple de texte · {0}",
            "Yazıyı büyüt" to
                "Agrandir le texte",
            "Satır aralığı" to
                "Interligne",
            "Sıkı" to
                "Serré",
            "Normal" to
                "Normal",
            "Geniş" to
                "Large",
            "Görünüm" to
                "Affichage",
            "Paragraf" to
                "Paragraphe",
            "Zaman damgalı" to
                "Horodaté",
            "Sesle takip" to
                "Suivre l'audio",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "Pendant la lecture audio, la phrase en cours est surlignée et l'écran la suit. Faire défiler à la main suspend le suivi.",
            "Tema" to
                "Thème",
            "Sistem" to
                "Système",
            "Açık" to
                "Clair",
            "Koyu" to
                "Sombre",
            "Canlı · ⚡ yazıya dökülüyor" to
                "En direct · ⚡ transcription",
            "Canlı · %{0}" to
                "En direct · {0} %",
            "Sesi duraklat" to
                "Mettre l'audio en pause",
            "Sesi oynat" to
                "Lire l'audio",
            "Ses dalgası; dokunduğun yere atlar" to
                "Forme d'onde ; touchez pour vous déplacer",
            "Oynatıcıyı daralt" to
                "Réduire le lecteur",
            "Oynatıcıyı genişlet" to
                "Agrandir le lecteur",
            "−5 sn" to
                "−5 s",
            "5 saniye geri" to
                "Reculer de 5 secondes",
            "+5 sn" to
                "+5 s",
            "5 saniye ileri" to
                "Avancer de 5 secondes",
            "Metin" to
                "Texte",
            "Çeviri" to
                "Traduction",
            "Metin birazdan burada belirecek…" to
                "Le texte apparaîtra ici dans un instant…",
            "Canlıya dön" to
                "Revenir au direct",
            "Okunan yere dön" to
                "Revenir à la position en cours",
            "Buradan dinle" to
                "Écouter à partir d'ici",
            "Çevir" to
                "Traduire",
            "Google Çeviri'de aç" to
                "Ouvrir dans Google Traduction",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "Texte long : copié dans le presse-papiers, collez-le dans Google Traduction",
            "Google Çeviri'de aç ↗" to
                "Ouvrir dans Google Traduction ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "Le texte est envoyé à Google · Internet requis",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "La traduction ci-dessus se fait sur votre appareil ; chaque pack de langue n'est téléchargé qu'une fois.",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ Une partie n'a pas pu être transcrite",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ {0} parties n'ont pas pu être transcrites",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ Une partie n'a pas pu être entièrement vérifiée",
            "⚠ {0} bölümde sorun var" to
                "⚠ Problèmes dans {0} parties",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "Ce passage manque dans le texte à cause d'une erreur de connexion ou de service.",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "Une ligne de ce passage semble avoir recommencé mais n'a pas été confirmée ; du texte peut manquer.",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "Certains passages n'ont pas pu être transcrits et d'autres n'ont pas été confirmés ; du texte peut manquer.",
            "Bu bölümü yeniden dök" to
                "Transcrire à nouveau cette partie",
            "Diğer" to
                "Plus",
            "Metni düzenle" to
                "Modifier le texte",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "Partager la traduction en fichier (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "Partager en fichier texte (.txt)",
            "Çeviri henüz hazır değil" to
                "La traduction n'est pas encore prête",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "Impossible de créer le fichier (stockage peut-être plein)",
            "Çeviri altyazısı (.srt)" to
                "Sous-titres de la traduction (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "Sous-titres (.srt) · transcription d'origine",
            "Altyazı (.srt)" to
                "Sous-titres (.srt)",
            "✓ Yazıya döküldü" to
                "✓ Transcrit",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} s · certaines parties n'ont pas pu être transcrites",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ Transcrit en {0} s",
            "Geri" to
                "Retour",
            "Notlarım" to
                "Mes notes",
            "Notlarda ara…" to
                "Rechercher dans les notes…",
            "Aramayı temizle" to
                "Effacer la recherche",
            "Henüz not yok." to
                "Aucune note pour l'instant.",
            "\"{0}\" hiçbir notta geçmiyor." to
                "Aucune note ne contient \"{0}\".",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "{0}/{1} notes. Une fois la limite atteinte, la note la plus ancienne est supprimée à l'arrivée d'une nouvelle ; exportez les notes importantes.",
            "Notu aç" to
                "Ouvrir la note",
            "Notu sil" to
                "Supprimer la note",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — service de reconnaissance vocale, Conditions d'utilisation de Wit.ai\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit Traduction — Conditions d'utilisation des API Google\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK, User Messaging Platform — Conditions d'utilisation de Google\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX, Jetpack Compose, Kotlin — Licence Apache 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "Notifications refusées ; impossible de vous prévenir quand c'est terminé",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "Taille du texte, interligne et suivi audio : le bouton \"Aa\" de l'écran de note.",
            "Transkript" to
                "Transcription",
            "Varsayılan konuşma dili" to
                "Langue parlée par défaut",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ L'audio est transcrit en quelques secondes (Internet requis). L'audio partagé depuis WhatsApp est transcrit dans cette langue ; si elle diffère, changez la langue dans le menu de la note.",
            "Varsayılan çeviri dili" to
                "Langue de traduction par défaut",
            "İndirilen dil paketleri" to
                "Packs de langue téléchargés",
            "Yükleniyor…" to
                "Chargement…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "Aucun pack de langue pour l'instant. Il est téléchargé automatiquement à la première traduction (~30 Mo).",
            "Sil" to
                "Supprimer",
            "{0} paketi silindi" to
                "Pack {0} supprimé",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "La traduction se fait sur votre téléphone ; votre texte n'est envoyé à aucun serveur.",
            "Bildirimler" to
                "Notifications",
            "İşlem bitince haber ver" to
                "Me prévenir quand c'est terminé",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "Si vous quittez l'appli, vous recevez une notification à la fin d'une longue transcription.",
            "Gizlilik" to
                "Confidentialité",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "L'audio est envoyé au service Wit.ai de Meta pour être transcrit. Meta traite ces données selon ses propres conditions ; d'après la politique de Wit.ai, les données vocales sont conservées jusqu'à 90 jours et peuvent être analysées pour améliorer la reconnaissance vocale. Nous n'avons pas de serveur ; vos notes et la copie temporaire de l'audio restent uniquement sur votre téléphone. La traduction se fait sur l'appareil. Les annonces sont diffusées par Google AdMob. Ne partagez que des audios que vous avez le droit de transcrire.",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "Politique de confidentialité",
            "Tüm notları sil ({0})" to
                "Supprimer toutes les notes ({0})",
            "Reklam tercihleri" to
                "Préférences publicitaires",
            "Hakkında" to
                "À propos",
            "Sürüm" to
                "Version",
            "Geliştirici araçları açıldı" to
                "Outils développeur activés",
            "Geri bildirim gönder" to
                "Envoyer un commentaire",
            " geri bildirim ({0})" to
                " commentaire ({0})",
            "Açık kaynak lisansları" to
                "Licences open source",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "Cette appli n'est ni développée, ni approuvée, ni sponsorisée par WhatsApp ou Meta. WhatsApp est une marque de WhatsApp LLC. La transcription utilise le service Wit.ai de Meta.",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "Noter l'appli",
            "Tüm notlar silinsin mi?" to
                "Supprimer toutes les notes ?",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "Toutes les notes, les fichiers exportés et les journaux de mesure de ce téléphone seront supprimés. Toute transcription en cours est arrêtée. Cette action est irréversible.",
            "Kapat" to
                "Fermer",
            "Sesli mesaja uzun bas" to
                "Appuyez longuement sur le message vocal",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "Dans la discussion WhatsApp, jusqu'à ce que le message soit sélectionné",
            "Paylaş simgesine dokun" to
                "Touchez l'icône Partager",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "Dans la barre du haut (sur certains téléphones, dans le menu ⋮)",
            "Listeden {0} uygulamasını seç" to
                "Choisissez {0} dans la liste",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "Le texte est préparé aussitôt. La prochaine fois, l'appli apparaîtra en haut de la liste.",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "{0} fichiers audio transcrits ; ils sont tous dans vos notes",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "{0} fichiers audio transcrits, {1} en échec",
            "{0} ile yazıya döküldü" to
                "Transcrit avec {0}",
            "Ses hazırlanıyor… ({0}/{1})" to
                "Préparation de l'audio… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "Une autre langue ? Essayez d'un seul geste :",
            "Paylaşırken imza ekle" to
                "Ajouter une signature au partage",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "Le nom et le lien de l'appli sont ajoutés sous le texte partagé.",
            "Reklamsız kullanım" to
                "Utilisation sans pub",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "Les pubs sont désactivées. Temps restant : environ {0} heures.",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "Regardez une courte annonce et ne voyez aucune pub pendant 24 heures.",
            "Reklam izle · 24 saat reklamsız" to
                "Regarder une annonce · 24 h sans pub",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "Aucune pub pendant 24 heures. Merci !",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "Regardez l'annonce jusqu'au bout pour obtenir la récompense.",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "L'annonce n'a pas pu se charger. Réessayez dans un instant.",
        )
    }

    val ES: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "Preparando el audio…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "El archivo es demasiado grande (máx. {0} MB)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "El audio es demasiado corto (al menos medio segundo)",
            "Ses açılamadı" to
                "No se pudo abrir el audio",
            "Dosya bu telefon için çok büyük" to
                "Este archivo es demasiado grande para este teléfono",
            "Ses açılamadı: {0}" to
                "No se pudo abrir el audio: {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "El audio de esta nota ya no está; vuelve a compartirlo",
            "Hazırlanıyor…" to
                "Preparando…",
            "Yazıya dökülemedi: {0}" to
                "No se pudo transcribir: {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "Conexión a internet débil o inexistente. Revisa tu conexión e inténtalo de nuevo.",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "El servicio está ocupado ahora. Inténtalo de nuevo en unos segundos.",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "No se puede acceder al servicio de transcripción para {0} en este momento. Actualiza la app e inténtalo de nuevo.",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "El servicio de transcripción no responde. Inténtalo de nuevo en un momento.",
            "Yazıya dökülemedi. Tekrar dene." to
                "No se pudo transcribir. Inténtalo de nuevo.",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "Se necesita conexión a internet. Conéctate e inténtalo de nuevo.",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "La transcripción no está disponible para {0} por ahora. Cambia el idioma del audio.",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ {0}–{1} no se pudo transcribir]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "No se detectó voz. ¿El idioma del audio es {0}? Si no, cambia el idioma e inténtalo de nuevo.",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "El texto está listo pero no se pudo guardar (puede que el almacenamiento esté lleno)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "Algunas partes no se pudieron transcribir; están marcadas en la nota",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "Una parte no se pudo verificar del todo; está marcada debajo de la nota",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "Las partes de una nota editada no se pueden volver a transcribir",
            "Önce mevcut işlem bitsin" to
                "Espera a que termine la tarea actual",
            "Bu dil için hızlı mod yok" to
                "La transcripción no está disponible para este idioma",
            "Bunun için internet bağlantısı gerekiyor" to
                "Esto necesita conexión a internet",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "Transcribiendo la parte otra vez ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "No se pudo volver a transcribir la parte; se conserva el texto actual",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "Se volvió a transcribir, pero la parte sigue sin poder verificarse del todo",
            "Bölüm yeniden döküldü" to
                "Parte transcrita de nuevo",
            "Geliştirici modu kapatıldı" to
                "Modo desarrollador desactivado",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "No se puede guardar un texto vacío. Usa el menú para eliminar la nota.",
            "Bu not silinmiş" to
                "Esta nota fue eliminada",
            "Kaydedildi" to
                "Guardado",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "No se pudo guardar (puede que el almacenamiento esté lleno). Tu borrador sigue abierto.",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "Espera a que termine la tarea actual o cancélala",
            "Not silinemedi (depolama hatası)" to
                "No se pudo eliminar la nota (error de almacenamiento)",
            "Geri alınamadı (depolama hatası)" to
                "No se pudo deshacer (error de almacenamiento)",
            "Notlar silinemedi (depolama hatası)" to
                "No se pudieron eliminar las notas (error de almacenamiento)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "Se eliminaron todas las notas y archivos de la app",
            "Kaynak dil tanınmadı" to
                "Idioma de origen no reconocido",
            "{0} için çeviri desteklenmiyor" to
                "La traducción no es compatible con {0}",
            "Çeviri yapılamadı: {0}" to
                "No se pudo traducir: {0}",
            "internet bağlantısını kontrol et" to
                "revisa tu conexión a internet",
            "Ses çalınamadı" to
                "No se pudo reproducir el audio",
            "Dosya boş" to
                "El archivo está vacío",
            "Döküm tamamlandı" to
                "Transcripción terminada",
            "Metnin hazır" to
                "Tu texto está listo",
            "Açmak için dokun" to
                "Toca para abrir",
            "{0} ile yazıya dök" to
                "Transcribir con {0}",
            "Reklam" to
                "Anuncio",
            "Dosya açılamadı" to
                "No se pudo abrir el archivo",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "No se pudo leer este archivo (formato no compatible)",
            "Bu dosyada ses bulunamadı" to
                "No se encontró audio en este archivo",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "Por ahora se admite audio de hasta 30 minutos",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "Este formato de audio ({0}) no es compatible con tu teléfono",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "No se pudo decodificar el audio (el decodificador no responde)",
            "Metin dosyasını paylaş" to
                "Compartir archivo de texto",
            "Altyazı dosyasını paylaş" to
                "Compartir archivo de subtítulos",
            "{0} çeviri için desteklenmiyor" to
                "{0} no es compatible con la traducción",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "Descargando paquete de idioma (una sola vez, ~30 MB)…",
            "Çevriliyor {0}/{1}…" to
                "Traduciendo {0}/{1}…",
            "Paylaş" to
                "Compartir",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "No se encontró WhatsApp; puedes elegir un archivo",
            "Ana sayfaya dön" to
                "Volver al inicio",
            "Ayarlar" to
                "Ajustes",
            "Dinleyemiyorsan,\noku." to
                "¿No puedes escuchar?\nLéelo.",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "En WhatsApp, mantén pulsado un audio → Compartir → {0}. El texto está listo en segundos.",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "Comparte un audio con {0} y léelo en segundos.",
            "WhatsApp'ı aç" to
                "Abrir WhatsApp",
            "Ses ya da video dosyası seç" to
                "Elegir un archivo de audio o vídeo",
            "Ses / video dosyası seç" to
                "Elegir archivo de audio / vídeo",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ Transcribiendo en un momento",
            "Yazıya dökülüyor…" to
                "Transcribiendo…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ En segundos · {0}",
            "Bir sorun oldu" to
                "Algo salió mal",
            "İptal" to
                "Cancelar",
            "Tekrar dene" to
                "Reintentar",
            "Konuşma dili: {0} ▾" to
                "Idioma del audio: {0} ▾",
            "Notlarda ara" to
                "Buscar en las notas",
            "Silindi: {0}" to
                "Eliminado: {0}",
            "Geri al" to
                "Deshacer",
            "Son notlar" to
                "Notas recientes",
            "Tümü ({0})" to
                "Todas ({0})",
            "Silmek için nota basılı tut" to
                "Mantén pulsada una nota para eliminarla",
            "Kılavuzu kapat" to
                "Ocultar guía",
            "Kılavuzu aç" to
                "Mostrar guía",
            "Nasıl kullanılır?" to
                "Cómo se usa",
            "Döküm bitince düzenleyebilirsin" to
                "Podrás editar cuando termine la transcripción",
            "Not metni düzenleme alanı" to
                "Editor del texto de la nota",
            " · düzenlendi" to
                " · editado",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · la vista con marcas de tiempo está disponible para texto sin editar",
            "Vazgeç" to
                "Cancelar",
            "Kaydet" to
                "Guardar",
            "Kopyala" to
                "Copiar",
            "Kopyalandı" to
                "Copiado",
            "Değişiklikler kaydedilmedi" to
                "Cambios sin guardar",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "Si sales sin guardar, perderás tus cambios.",
            "Kaydetme" to
                "No guardar",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · {2} palabras",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "Ajustes de lectura (tamaño del texto, interlineado, tema)",
            "Diğer seçenekler" to
                "Más opciones",
            "⚡ Yeniden dök ({0})" to
                "⚡ Transcribir de nuevo ({0})",
            "Dil: {0} ile yeniden dök" to
                "Transcribir de nuevo en {0}",
            "Bu notu sil" to
                "Eliminar esta nota",
            "Okuma ayarları" to
                "Ajustes de lectura",
            "Yazı boyutu" to
                "Tamaño del texto",
            "Yazıyı küçült" to
                "Texto más pequeño",
            "Örnek metin · {0}" to
                "Texto de ejemplo · {0}",
            "Yazıyı büyüt" to
                "Texto más grande",
            "Satır aralığı" to
                "Interlineado",
            "Sıkı" to
                "Ajustado",
            "Normal" to
                "Normal",
            "Geniş" to
                "Amplio",
            "Görünüm" to
                "Vista",
            "Paragraf" to
                "Párrafo",
            "Zaman damgalı" to
                "Con marcas de tiempo",
            "Sesle takip" to
                "Seguir el audio",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "Mientras suena el audio, se resalta la frase actual y la pantalla la sigue. Desplazarte a mano pausa el seguimiento.",
            "Tema" to
                "Tema",
            "Sistem" to
                "Sistema",
            "Açık" to
                "Claro",
            "Koyu" to
                "Oscuro",
            "Canlı · ⚡ yazıya dökülüyor" to
                "En vivo · ⚡ transcribiendo",
            "Canlı · %{0}" to
                "En vivo · {0} %",
            "Sesi duraklat" to
                "Pausar audio",
            "Sesi oynat" to
                "Reproducir audio",
            "Ses dalgası; dokunduğun yere atlar" to
                "Forma de onda; toca para saltar",
            "Oynatıcıyı daralt" to
                "Contraer reproductor",
            "Oynatıcıyı genişlet" to
                "Expandir reproductor",
            "−5 sn" to
                "−5 s",
            "5 saniye geri" to
                "Retroceder 5 segundos",
            "+5 sn" to
                "+5 s",
            "5 saniye ileri" to
                "Avanzar 5 segundos",
            "Metin" to
                "Texto",
            "Çeviri" to
                "Traducción",
            "Metin birazdan burada belirecek…" to
                "El texto aparecerá aquí en un momento…",
            "Canlıya dön" to
                "Volver al directo",
            "Okunan yere dön" to
                "Volver a la posición actual",
            "Buradan dinle" to
                "Escuchar desde aquí",
            "Çevir" to
                "Traducir",
            "Google Çeviri'de aç" to
                "Abrir en el Traductor de Google",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "Texto largo: copiado al portapapeles, pégalo en el Traductor de Google",
            "Google Çeviri'de aç ↗" to
                "Abrir en el Traductor de Google ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "El texto se envía a Google · requiere internet",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "La traducción de arriba se hace en tu dispositivo; cada paquete de idioma se descarga solo una vez.",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ Una parte no se pudo transcribir",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ {0} partes no se pudieron transcribir",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ Una parte no se pudo verificar del todo",
            "⚠ {0} bölümde sorun var" to
                "⚠ Hay problemas en {0} partes",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "Este tramo falta en el texto por un error de conexión o del servicio.",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "Una línea de este tramo parece haberse reiniciado pero no se confirmó; puede faltar texto.",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "Algunos tramos no se pudieron transcribir y otros no se confirmaron; puede faltar texto.",
            "Bu bölümü yeniden dök" to
                "Transcribir esta parte de nuevo",
            "Diğer" to
                "Más",
            "Metni düzenle" to
                "Editar texto",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "Compartir la traducción como archivo (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "Compartir como archivo de texto (.txt)",
            "Çeviri henüz hazır değil" to
                "La traducción aún no está lista",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "No se pudo crear el archivo (puede que el almacenamiento esté lleno)",
            "Çeviri altyazısı (.srt)" to
                "Subtítulos de la traducción (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "Subtítulos (.srt) · transcripción original",
            "Altyazı (.srt)" to
                "Subtítulos (.srt)",
            "✓ Yazıya döküldü" to
                "✓ Transcrito",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} s · algunas partes no se pudieron transcribir",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ Transcrito en {0} s",
            "Geri" to
                "Atrás",
            "Notlarım" to
                "Mis notas",
            "Notlarda ara…" to
                "Buscar en las notas…",
            "Aramayı temizle" to
                "Borrar búsqueda",
            "Henüz not yok." to
                "Aún no hay notas.",
            "\"{0}\" hiçbir notta geçmiyor." to
                "Ninguna nota contiene \"{0}\".",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "{0}/{1} notas. Al llegar al límite, se elimina la nota más antigua cuando entra una nueva; exporta las notas importantes.",
            "Notu aç" to
                "Abrir nota",
            "Notu sil" to
                "Eliminar nota",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — servicio de voz a texto, Condiciones del servicio de Wit.ai\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit Traducción — Condiciones del servicio de las API de Google\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK, User Messaging Platform — Condiciones del servicio de Google\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX, Jetpack Compose, Kotlin — Licencia Apache 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "Permiso de notificaciones denegado; no podemos avisarte cuando termine",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "Tamaño del texto, interlineado y seguir el audio: el botón \"Aa\" de la pantalla de la nota.",
            "Transkript" to
                "Transcripción",
            "Varsayılan konuşma dili" to
                "Idioma del audio predeterminado",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ El audio se transcribe en segundos (requiere internet). El audio que compartes desde WhatsApp se transcribe en este idioma; si es otro, cambia el idioma en el menú de la nota.",
            "Varsayılan çeviri dili" to
                "Idioma de traducción predeterminado",
            "İndirilen dil paketleri" to
                "Paquetes de idioma descargados",
            "Yükleniyor…" to
                "Cargando…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "Aún no hay paquetes de idioma. Se descarga uno automáticamente en la primera traducción (~30 MB).",
            "Sil" to
                "Eliminar",
            "{0} paketi silindi" to
                "Paquete de {0} eliminado",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "La traducción se hace en tu teléfono; tu texto no se envía a ningún servidor.",
            "Bildirimler" to
                "Notificaciones",
            "İşlem bitince haber ver" to
                "Avisarme cuando termine",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "Si sales de la app, recibes una notificación cuando termina una transcripción larga.",
            "Gizlilik" to
                "Privacidad",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "El audio se envía al servicio Wit.ai de Meta para transcribirlo. Meta trata estos datos según sus propias condiciones; según la política de Wit.ai, los datos de voz se conservan hasta 90 días y pueden analizarse para mejorar el reconocimiento de voz. No tenemos servidor; tus notas y la copia temporal del audio se quedan solo en tu teléfono. La traducción se hace en el dispositivo. Los anuncios los muestra Google AdMob. Comparte solo audios que tengas derecho a transcribir.",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "Política de privacidad",
            "Tüm notları sil ({0})" to
                "Eliminar todas las notas ({0})",
            "Reklam tercihleri" to
                "Preferencias de anuncios",
            "Hakkında" to
                "Acerca de",
            "Sürüm" to
                "Versión",
            "Geliştirici araçları açıldı" to
                "Herramientas de desarrollador activadas",
            "Geri bildirim gönder" to
                "Enviar comentarios",
            " geri bildirim ({0})" to
                " comentarios ({0})",
            "Açık kaynak lisansları" to
                "Licencias de código abierto",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "Esta app no ha sido desarrollada, avalada ni patrocinada por WhatsApp o Meta. WhatsApp es una marca de WhatsApp LLC. La transcripción usa el servicio Wit.ai de Meta.",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "Valorar la app",
            "Tüm notlar silinsin mi?" to
                "¿Eliminar todas las notas?",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "Se eliminarán todas las notas, los archivos exportados y los registros de medición de este teléfono. Se detendrá cualquier transcripción en curso. Esto no se puede deshacer.",
            "Kapat" to
                "Cerrar",
            "Sesli mesaja uzun bas" to
                "Mantén pulsado el audio",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "En el chat de WhatsApp, hasta que el mensaje quede seleccionado",
            "Paylaş simgesine dokun" to
                "Toca el icono Compartir",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "En la barra superior (en algunos teléfonos, en el menú ⋮)",
            "Listeden {0} uygulamasını seç" to
                "Elige {0} en la lista",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "El texto se prepara al instante. La próxima vez aparecerá entre los primeros de la lista.",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "{0} audios transcritos; todos están en tus notas",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "{0} audios transcritos, {1} con error",
            "{0} ile yazıya döküldü" to
                "Transcrito con {0}",
            "Ses hazırlanıyor… ({0}/{1})" to
                "Preparando el audio… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "¿El audio está en otro idioma? Prueba con un toque:",
            "Paylaşırken imza ekle" to
                "Añadir firma al compartir",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "El nombre y el enlace de la app se añaden debajo del texto que compartes.",
            "Reklamsız kullanım" to
                "Uso sin anuncios",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "Los anuncios están desactivados. Tiempo restante: unas {0} horas.",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "Mira un anuncio corto y no veas anuncios durante 24 horas.",
            "Reklam izle · 24 saat reklamsız" to
                "Ver un anuncio · 24 h sin anuncios",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "Sin anuncios durante 24 horas. ¡Gracias!",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "Mira el anuncio hasta el final para obtener la recompensa.",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "El anuncio no se pudo cargar ahora. Inténtalo de nuevo en un momento.",
        )
    }

    val IT: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "Preparazione dell'audio…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "File troppo grande (max {0} MB)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "Audio troppo breve (almeno mezzo secondo)",
            "Ses açılamadı" to
                "Impossibile aprire l'audio",
            "Dosya bu telefon için çok büyük" to
                "Questo file è troppo grande per questo telefono",
            "Ses açılamadı: {0}" to
                "Impossibile aprire l'audio: {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "L'audio di questa nota non c'è più; condividilo di nuovo",
            "Hazırlanıyor…" to
                "Preparazione…",
            "Yazıya dökülemedi: {0}" to
                "Trascrizione non riuscita: {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "Connessione internet debole o assente. Controlla la connessione e riprova.",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "Il servizio è occupato in questo momento. Riprova tra qualche secondo.",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "Il servizio di trascrizione per {0} non è raggiungibile in questo momento. Aggiorna l'app e riprova.",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "Il servizio di trascrizione non risponde. Riprova tra poco.",
            "Yazıya dökülemedi. Tekrar dene." to
                "Trascrizione non riuscita. Riprova.",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "Serve una connessione internet. Connettiti e riprova.",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "La trascrizione non è disponibile per {0} al momento. Cambia la lingua del parlato.",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ {0}–{1} non è stato trascritto]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "Nessun parlato rilevato. La lingua del parlato è {0}? Se no, cambia lingua e riprova.",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "Il testo è pronto ma non è stato salvato (memoria forse piena)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "Alcune parti non sono state trascritte; sono segnalate nella nota",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "Una parte non è stata verificata del tutto; è segnalata sotto la nota",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "Le parti di una nota modificata non possono essere ritrascritte",
            "Önce mevcut işlem bitsin" to
                "Attendi la fine dell'operazione in corso",
            "Bu dil için hızlı mod yok" to
                "La trascrizione non è disponibile per questa lingua",
            "Bunun için internet bağlantısı gerekiyor" to
                "Serve una connessione internet",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "Nuova trascrizione della parte ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "Impossibile ritrascrivere la parte; il testo esistente è stato mantenuto",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "Ritrascritta, ma la parte non è ancora verificata del tutto",
            "Bölüm yeniden döküldü" to
                "Parte ritrascritta",
            "Geliştirici modu kapatıldı" to
                "Modalità sviluppatore disattivata",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "Non si può salvare un testo vuoto. Usa il menu per eliminare la nota.",
            "Bu not silinmiş" to
                "Questa nota è stata eliminata",
            "Kaydedildi" to
                "Salvato",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "Salvataggio non riuscito (memoria forse piena). La bozza resta aperta.",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "Attendi la fine dell'operazione in corso oppure annullala",
            "Not silinemedi (depolama hatası)" to
                "Impossibile eliminare la nota (errore di memoria)",
            "Geri alınamadı (depolama hatası)" to
                "Impossibile annullare (errore di memoria)",
            "Notlar silinemedi (depolama hatası)" to
                "Impossibile eliminare le note (errore di memoria)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "Tutte le note e i file dell'app sono stati eliminati",
            "Kaynak dil tanınmadı" to
                "Lingua di origine non riconosciuta",
            "{0} için çeviri desteklenmiyor" to
                "La traduzione non è supportata per {0}",
            "Çeviri yapılamadı: {0}" to
                "Traduzione non riuscita: {0}",
            "internet bağlantısını kontrol et" to
                "controlla la connessione internet",
            "Ses çalınamadı" to
                "Impossibile riprodurre l'audio",
            "Dosya boş" to
                "Il file è vuoto",
            "Döküm tamamlandı" to
                "Trascrizione completata",
            "Metnin hazır" to
                "Il tuo testo è pronto",
            "Açmak için dokun" to
                "Tocca per aprire",
            "{0} ile yazıya dök" to
                "Trascrivi con {0}",
            "Reklam" to
                "Annuncio",
            "Dosya açılamadı" to
                "Impossibile aprire il file",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "Impossibile leggere questo file (formato non supportato)",
            "Bu dosyada ses bulunamadı" to
                "Nessun audio trovato in questo file",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "Per ora è supportato audio fino a 30 minuti",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "Questo formato audio ({0}) non è supportato sul tuo telefono",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "Impossibile decodificare l'audio (il decoder non risponde)",
            "Metin dosyasını paylaş" to
                "Condividi file di testo",
            "Altyazı dosyasını paylaş" to
                "Condividi file dei sottotitoli",
            "{0} çeviri için desteklenmiyor" to
                "{0} non è supportato per la traduzione",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "Download del pacchetto lingua (una sola volta, ~30 MB)…",
            "Çevriliyor {0}/{1}…" to
                "Traduzione {0}/{1}…",
            "Paylaş" to
                "Condividi",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "WhatsApp non trovato; puoi scegliere un file",
            "Ana sayfaya dön" to
                "Torna alla home",
            "Ayarlar" to
                "Impostazioni",
            "Dinleyemiyorsan,\noku." to
                "Non puoi ascoltare?\nLeggi.",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "In WhatsApp, tieni premuto un vocale → Condividi → {0}. Il testo è pronto in pochi secondi.",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "Condividi un vocale con {0} e leggilo in pochi secondi.",
            "WhatsApp'ı aç" to
                "Apri WhatsApp",
            "Ses ya da video dosyası seç" to
                "Scegli un file audio o video",
            "Ses / video dosyası seç" to
                "Scegli file audio / video",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ Trascrizione tra un attimo",
            "Yazıya dökülüyor…" to
                "Trascrizione in corso…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ In pochi secondi · {0}",
            "Bir sorun oldu" to
                "Qualcosa è andato storto",
            "İptal" to
                "Annulla",
            "Tekrar dene" to
                "Riprova",
            "Konuşma dili: {0} ▾" to
                "Lingua del parlato: {0} ▾",
            "Notlarda ara" to
                "Cerca nelle note",
            "Silindi: {0}" to
                "Eliminata: {0}",
            "Geri al" to
                "Annulla",
            "Son notlar" to
                "Note recenti",
            "Tümü ({0})" to
                "Tutte ({0})",
            "Silmek için nota basılı tut" to
                "Tieni premuta una nota per eliminarla",
            "Kılavuzu kapat" to
                "Nascondi guida",
            "Kılavuzu aç" to
                "Mostra guida",
            "Nasıl kullanılır?" to
                "Come si usa",
            "Döküm bitince düzenleyebilirsin" to
                "Potrai modificare al termine della trascrizione",
            "Not metni düzenleme alanı" to
                "Area di modifica del testo della nota",
            " · düzenlendi" to
                " · modificata",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · la vista con orari è disponibile per il testo non modificato",
            "Vazgeç" to
                "Annulla",
            "Kaydet" to
                "Salva",
            "Kopyala" to
                "Copia",
            "Kopyalandı" to
                "Copiato",
            "Değişiklikler kaydedilmedi" to
                "Modifiche non salvate",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "Se esci senza salvare, le modifiche andranno perse.",
            "Kaydetme" to
                "Non salvare",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · {2} parole",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "Impostazioni di lettura (dimensione testo, interlinea, tema)",
            "Diğer seçenekler" to
                "Altre opzioni",
            "⚡ Yeniden dök ({0})" to
                "⚡ Trascrivi di nuovo ({0})",
            "Dil: {0} ile yeniden dök" to
                "Trascrivi di nuovo in {0}",
            "Bu notu sil" to
                "Elimina questa nota",
            "Okuma ayarları" to
                "Impostazioni di lettura",
            "Yazı boyutu" to
                "Dimensione testo",
            "Yazıyı küçült" to
                "Testo più piccolo",
            "Örnek metin · {0}" to
                "Testo di esempio · {0}",
            "Yazıyı büyüt" to
                "Testo più grande",
            "Satır aralığı" to
                "Interlinea",
            "Sıkı" to
                "Stretta",
            "Normal" to
                "Normale",
            "Geniş" to
                "Ampia",
            "Görünüm" to
                "Vista",
            "Paragraf" to
                "Paragrafo",
            "Zaman damgalı" to
                "Con orari",
            "Sesle takip" to
                "Segui l'audio",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "Durante la riproduzione la frase corrente viene evidenziata e lo schermo la segue. Scorrere a mano mette in pausa il seguimento.",
            "Tema" to
                "Tema",
            "Sistem" to
                "Sistema",
            "Açık" to
                "Chiaro",
            "Koyu" to
                "Scuro",
            "Canlı · ⚡ yazıya dökülüyor" to
                "Live · ⚡ trascrizione in corso",
            "Canlı · %{0}" to
                "Live · {0}%",
            "Sesi duraklat" to
                "Metti in pausa l'audio",
            "Sesi oynat" to
                "Riproduci l'audio",
            "Ses dalgası; dokunduğun yere atlar" to
                "Forma d'onda; tocca per spostarti",
            "Oynatıcıyı daralt" to
                "Comprimi il player",
            "Oynatıcıyı genişlet" to
                "Espandi il player",
            "−5 sn" to
                "−5 s",
            "5 saniye geri" to
                "Indietro di 5 secondi",
            "+5 sn" to
                "+5 s",
            "5 saniye ileri" to
                "Avanti di 5 secondi",
            "Metin" to
                "Testo",
            "Çeviri" to
                "Traduzione",
            "Metin birazdan burada belirecek…" to
                "Il testo apparirà qui tra poco…",
            "Canlıya dön" to
                "Torna al live",
            "Okunan yere dön" to
                "Torna alla posizione corrente",
            "Buradan dinle" to
                "Ascolta da qui",
            "Çevir" to
                "Traduci",
            "Google Çeviri'de aç" to
                "Apri in Google Traduttore",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "Testo lungo: copiato negli appunti, incollalo in Google Traduttore",
            "Google Çeviri'de aç ↗" to
                "Apri in Google Traduttore ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "Il testo viene inviato a Google · serve internet",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "La traduzione qui sopra avviene sul dispositivo; ogni pacchetto lingua si scarica una sola volta.",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ Una parte non è stata trascritta",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ {0} parti non sono state trascritte",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ Una parte non è stata verificata del tutto",
            "⚠ {0} bölümde sorun var" to
                "⚠ Problemi in {0} parti",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "Questo intervallo manca nel testo per un errore di connessione o del servizio.",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "Una riga in questo intervallo sembra ricominciata ma non è stata confermata; potrebbe mancare del testo.",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "Alcuni intervalli non sono stati trascritti e altri non sono stati confermati; potrebbe mancare del testo.",
            "Bu bölümü yeniden dök" to
                "Trascrivi di nuovo questa parte",
            "Diğer" to
                "Altro",
            "Metni düzenle" to
                "Modifica testo",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "Condividi la traduzione come file (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "Condividi come file di testo (.txt)",
            "Çeviri henüz hazır değil" to
                "La traduzione non è ancora pronta",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "Impossibile creare il file (memoria forse piena)",
            "Çeviri altyazısı (.srt)" to
                "Sottotitoli della traduzione (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "Sottotitoli (.srt) · trascrizione originale",
            "Altyazı (.srt)" to
                "Sottotitoli (.srt)",
            "✓ Yazıya döküldü" to
                "✓ Trascritto",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} s · alcune parti non sono state trascritte",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ Trascritto in {0} s",
            "Geri" to
                "Indietro",
            "Notlarım" to
                "Le mie note",
            "Notlarda ara…" to
                "Cerca nelle note…",
            "Aramayı temizle" to
                "Cancella ricerca",
            "Henüz not yok." to
                "Ancora nessuna nota.",
            "\"{0}\" hiçbir notta geçmiyor." to
                "Nessuna nota contiene \"{0}\".",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "{0}/{1} note. Raggiunto il limite, all'arrivo di una nuova nota viene rimossa la più vecchia; esporta le note importanti.",
            "Notu aç" to
                "Apri nota",
            "Notu sil" to
                "Elimina nota",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — servizio di riconoscimento vocale, Termini di servizio di Wit.ai\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit Traduzione — Termini di servizio delle API di Google\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK, User Messaging Platform — Termini di servizio di Google\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX, Jetpack Compose, Kotlin — Licenza Apache 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "Notifiche non consentite; non possiamo avvisarti al termine",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "Dimensione testo, interlinea e segui l'audio: il pulsante \"Aa\" nella schermata della nota.",
            "Transkript" to
                "Trascrizione",
            "Varsayılan konuşma dili" to
                "Lingua del parlato predefinita",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ L'audio viene trascritto in pochi secondi (serve internet). L'audio condiviso da WhatsApp viene trascritto in questa lingua; se è diversa, cambiala dal menu della nota.",
            "Varsayılan çeviri dili" to
                "Lingua di traduzione predefinita",
            "İndirilen dil paketleri" to
                "Pacchetti lingua scaricati",
            "Yükleniyor…" to
                "Caricamento…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "Ancora nessun pacchetto lingua. Viene scaricato automaticamente alla prima traduzione (~30 MB).",
            "Sil" to
                "Elimina",
            "{0} paketi silindi" to
                "Pacchetto {0} eliminato",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "La traduzione avviene sul telefono; il tuo testo non viene inviato a nessun server.",
            "Bildirimler" to
                "Notifiche",
            "İşlem bitince haber ver" to
                "Avvisami al termine",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "Se esci dall'app, ricevi una notifica quando finisce una trascrizione lunga.",
            "Gizlilik" to
                "Privacy",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "L'audio viene inviato al servizio Wit.ai di Meta per essere trascritto. Meta tratta questi dati secondo le proprie condizioni; in base alle norme di Wit.ai, i dati vocali sono conservati fino a 90 giorni e possono essere analizzati per migliorare il riconoscimento vocale. Non abbiamo un server; le tue note e la copia temporanea dell'audio restano solo sul telefono. La traduzione avviene sul dispositivo. Gli annunci sono mostrati da Google AdMob. Condividi solo audio che hai il diritto di trascrivere.",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "Informativa sulla privacy",
            "Tüm notları sil ({0})" to
                "Elimina tutte le note ({0})",
            "Reklam tercihleri" to
                "Preferenze annunci",
            "Hakkında" to
                "Informazioni",
            "Sürüm" to
                "Versione",
            "Geliştirici araçları açıldı" to
                "Strumenti sviluppatore attivati",
            "Geri bildirim gönder" to
                "Invia feedback",
            " geri bildirim ({0})" to
                " feedback ({0})",
            "Açık kaynak lisansları" to
                "Licenze open source",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "Questa app non è sviluppata, approvata o sponsorizzata da WhatsApp o Meta. WhatsApp è un marchio di WhatsApp LLC. La trascrizione usa il servizio Wit.ai di Meta.",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "Valuta l'app",
            "Tüm notlar silinsin mi?" to
                "Eliminare tutte le note?",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "Tutte le note, i file esportati e i registri di misurazione su questo telefono saranno eliminati. Qualsiasi trascrizione in corso viene interrotta. L'operazione non può essere annullata.",
            "Kapat" to
                "Chiudi",
            "Sesli mesaja uzun bas" to
                "Tieni premuto il vocale",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "Nella chat di WhatsApp, finché il messaggio non è selezionato",
            "Paylaş simgesine dokun" to
                "Tocca l'icona Condividi",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "Nella barra in alto (su alcuni telefoni, nel menu ⋮)",
            "Listeden {0} uygulamasını seç" to
                "Scegli {0} dall'elenco",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "Il testo viene preparato subito. La prossima volta l'app sarà tra le prime dell'elenco.",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "{0} audio trascritti; sono tutti nelle tue note",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "{0} audio trascritti, {1} non riusciti",
            "{0} ile yazıya döküldü" to
                "Trascritto con {0}",
            "Ses hazırlanıyor… ({0}/{1})" to
                "Preparazione dell'audio… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "Il parlato è in un'altra lingua? Prova con un tocco:",
            "Paylaşırken imza ekle" to
                "Aggiungi una firma quando condividi",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "Nome e link dell'app vengono aggiunti sotto il testo che condividi.",
            "Reklamsız kullanım" to
                "Uso senza annunci",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "Gli annunci sono disattivati. Tempo rimasto: circa {0} ore.",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "Guarda un breve annuncio e non vedere annunci per 24 ore.",
            "Reklam izle · 24 saat reklamsız" to
                "Guarda un annuncio · 24 ore senza annunci",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "Nessun annuncio per 24 ore. Grazie!",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "Guarda l'annuncio fino alla fine per ottenere il premio.",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "L'annuncio non si è caricato. Riprova tra poco.",
        )
    }

    val PT: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "Preparando o áudio…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "O arquivo é grande demais (máx. {0} MB)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "O áudio é curto demais (pelo menos meio segundo)",
            "Ses açılamadı" to
                "Não foi possível abrir o áudio",
            "Dosya bu telefon için çok büyük" to
                "Este arquivo é grande demais para este celular",
            "Ses açılamadı: {0}" to
                "Não foi possível abrir o áudio: {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "O áudio desta nota não existe mais; compartilhe o áudio de novo",
            "Hazırlanıyor…" to
                "Preparando…",
            "Yazıya dökülemedi: {0}" to
                "Não foi possível transcrever: {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "Conexão com a internet fraca ou ausente. Verifique a conexão e tente de novo.",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "O serviço está ocupado agora. Tente de novo em alguns segundos.",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "O serviço de transcrição para {0} está inacessível no momento. Atualize o app e tente de novo.",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "O serviço de transcrição não está respondendo. Tente de novo em instantes.",
            "Yazıya dökülemedi. Tekrar dene." to
                "Não foi possível transcrever. Tente de novo.",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "É necessária conexão com a internet. Conecte-se e tente de novo.",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "A transcrição não está disponível para {0} no momento. Mude o idioma da fala.",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ {0}–{1} não pôde ser transcrito]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "Nenhuma fala detectada. O idioma da fala é {0}? Se não, mude o idioma e tente de novo.",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "O texto está pronto, mas não foi salvo (o armazenamento pode estar cheio)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "Algumas partes não puderam ser transcritas; estão marcadas na nota",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "Uma parte não pôde ser totalmente verificada; está marcada abaixo da nota",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "Partes de uma nota editada não podem ser transcritas de novo",
            "Önce mevcut işlem bitsin" to
                "Aguarde a tarefa atual terminar",
            "Bu dil için hızlı mod yok" to
                "A transcrição não está disponível para este idioma",
            "Bunun için internet bağlantısı gerekiyor" to
                "Isso precisa de conexão com a internet",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "Transcrevendo a parte de novo ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "Não foi possível transcrever a parte de novo; o texto atual foi mantido",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "Transcrita de novo, mas a parte ainda não pôde ser totalmente verificada",
            "Bölüm yeniden döküldü" to
                "Parte transcrita de novo",
            "Geliştirici modu kapatıldı" to
                "Modo desenvolvedor desativado",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "Texto vazio não pode ser salvo. Use o menu para excluir a nota.",
            "Bu not silinmiş" to
                "Esta nota foi excluída",
            "Kaydedildi" to
                "Salvo",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "Não foi possível salvar (o armazenamento pode estar cheio). Seu rascunho continua aberto.",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "Aguarde a tarefa atual terminar ou cancele-a",
            "Not silinemedi (depolama hatası)" to
                "Não foi possível excluir a nota (erro de armazenamento)",
            "Geri alınamadı (depolama hatası)" to
                "Não foi possível desfazer (erro de armazenamento)",
            "Notlar silinemedi (depolama hatası)" to
                "Não foi possível excluir as notas (erro de armazenamento)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "Todas as notas e arquivos do app foram excluídos",
            "Kaynak dil tanınmadı" to
                "Idioma de origem não reconhecido",
            "{0} için çeviri desteklenmiyor" to
                "A tradução não é compatível com {0}",
            "Çeviri yapılamadı: {0}" to
                "Não foi possível traduzir: {0}",
            "internet bağlantısını kontrol et" to
                "verifique sua conexão com a internet",
            "Ses çalınamadı" to
                "Não foi possível reproduzir o áudio",
            "Dosya boş" to
                "O arquivo está vazio",
            "Döküm tamamlandı" to
                "Transcrição concluída",
            "Metnin hazır" to
                "Seu texto está pronto",
            "Açmak için dokun" to
                "Toque para abrir",
            "{0} ile yazıya dök" to
                "Transcrever com {0}",
            "Reklam" to
                "Anúncio",
            "Dosya açılamadı" to
                "Não foi possível abrir o arquivo",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "Não foi possível ler este arquivo (formato não compatível)",
            "Bu dosyada ses bulunamadı" to
                "Nenhum áudio encontrado neste arquivo",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "Por enquanto, áudios de até 30 minutos são aceitos",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "Este formato de áudio ({0}) não é compatível com seu celular",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "Não foi possível decodificar o áudio (o decodificador não responde)",
            "Metin dosyasını paylaş" to
                "Compartilhar arquivo de texto",
            "Altyazı dosyasını paylaş" to
                "Compartilhar arquivo de legendas",
            "{0} çeviri için desteklenmiyor" to
                "{0} não é compatível com a tradução",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "Baixando pacote de idioma (uma única vez, ~30 MB)…",
            "Çevriliyor {0}/{1}…" to
                "Traduzindo {0}/{1}…",
            "Paylaş" to
                "Compartilhar",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "WhatsApp não encontrado; você pode escolher um arquivo",
            "Ana sayfaya dön" to
                "Voltar ao início",
            "Ayarlar" to
                "Configurações",
            "Dinleyemiyorsan,\noku." to
                "Não pode ouvir?\nLeia.",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "No WhatsApp, toque e segure um áudio → Compartilhar → {0}. O texto fica pronto em segundos.",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "Compartilhe um áudio com {0} e leia em segundos.",
            "WhatsApp'ı aç" to
                "Abrir o WhatsApp",
            "Ses ya da video dosyası seç" to
                "Escolher um arquivo de áudio ou vídeo",
            "Ses / video dosyası seç" to
                "Escolher arquivo de áudio / vídeo",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ Transcrevendo em instantes",
            "Yazıya dökülüyor…" to
                "Transcrevendo…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ Em segundos · {0}",
            "Bir sorun oldu" to
                "Algo deu errado",
            "İptal" to
                "Cancelar",
            "Tekrar dene" to
                "Tentar de novo",
            "Konuşma dili: {0} ▾" to
                "Idioma da fala: {0} ▾",
            "Notlarda ara" to
                "Buscar nas notas",
            "Silindi: {0}" to
                "Excluída: {0}",
            "Geri al" to
                "Desfazer",
            "Son notlar" to
                "Notas recentes",
            "Tümü ({0})" to
                "Todas ({0})",
            "Silmek için nota basılı tut" to
                "Toque e segure uma nota para excluí-la",
            "Kılavuzu kapat" to
                "Ocultar guia",
            "Kılavuzu aç" to
                "Mostrar guia",
            "Nasıl kullanılır?" to
                "Como usar",
            "Döküm bitince düzenleyebilirsin" to
                "Você poderá editar quando a transcrição terminar",
            "Not metni düzenleme alanı" to
                "Editor do texto da nota",
            " · düzenlendi" to
                " · editada",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · a visualização com horários está disponível para texto não editado",
            "Vazgeç" to
                "Cancelar",
            "Kaydet" to
                "Salvar",
            "Kopyala" to
                "Copiar",
            "Kopyalandı" to
                "Copiado",
            "Değişiklikler kaydedilmedi" to
                "Alterações não salvas",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "Se sair sem salvar, suas alterações serão perdidas.",
            "Kaydetme" to
                "Não salvar",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · {2} palavras",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "Configurações de leitura (tamanho do texto, espaçamento, tema)",
            "Diğer seçenekler" to
                "Mais opções",
            "⚡ Yeniden dök ({0})" to
                "⚡ Transcrever de novo ({0})",
            "Dil: {0} ile yeniden dök" to
                "Transcrever de novo em {0}",
            "Bu notu sil" to
                "Excluir esta nota",
            "Okuma ayarları" to
                "Configurações de leitura",
            "Yazı boyutu" to
                "Tamanho do texto",
            "Yazıyı küçült" to
                "Texto menor",
            "Örnek metin · {0}" to
                "Texto de exemplo · {0}",
            "Yazıyı büyüt" to
                "Texto maior",
            "Satır aralığı" to
                "Espaçamento entre linhas",
            "Sıkı" to
                "Apertado",
            "Normal" to
                "Normal",
            "Geniş" to
                "Amplo",
            "Görünüm" to
                "Exibição",
            "Paragraf" to
                "Parágrafo",
            "Zaman damgalı" to
                "Com horários",
            "Sesle takip" to
                "Acompanhar o áudio",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "Enquanto o áudio toca, a frase atual é destacada e a tela a acompanha. Rolar com o dedo pausa o acompanhamento.",
            "Tema" to
                "Tema",
            "Sistem" to
                "Sistema",
            "Açık" to
                "Claro",
            "Koyu" to
                "Escuro",
            "Canlı · ⚡ yazıya dökülüyor" to
                "Ao vivo · ⚡ transcrevendo",
            "Canlı · %{0}" to
                "Ao vivo · {0}%",
            "Sesi duraklat" to
                "Pausar áudio",
            "Sesi oynat" to
                "Reproduzir áudio",
            "Ses dalgası; dokunduğun yere atlar" to
                "Forma de onda; toque para pular",
            "Oynatıcıyı daralt" to
                "Recolher o player",
            "Oynatıcıyı genişlet" to
                "Expandir o player",
            "−5 sn" to
                "−5 s",
            "5 saniye geri" to
                "Voltar 5 segundos",
            "+5 sn" to
                "+5 s",
            "5 saniye ileri" to
                "Avançar 5 segundos",
            "Metin" to
                "Texto",
            "Çeviri" to
                "Tradução",
            "Metin birazdan burada belirecek…" to
                "O texto vai aparecer aqui em instantes…",
            "Canlıya dön" to
                "Voltar ao vivo",
            "Okunan yere dön" to
                "Voltar à posição atual",
            "Buradan dinle" to
                "Ouvir a partir daqui",
            "Çevir" to
                "Traduzir",
            "Google Çeviri'de aç" to
                "Abrir no Google Tradutor",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "Texto longo: copiado para a área de transferência, cole no Google Tradutor",
            "Google Çeviri'de aç ↗" to
                "Abrir no Google Tradutor ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "O texto é enviado ao Google · requer internet",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "A tradução acima é feita no seu aparelho; cada pacote de idioma é baixado só uma vez.",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ Uma parte não pôde ser transcrita",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ {0} partes não puderam ser transcritas",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ Uma parte não pôde ser totalmente verificada",
            "⚠ {0} bölümde sorun var" to
                "⚠ Há problemas em {0} partes",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "Este trecho está faltando no texto por um erro de conexão ou do serviço.",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "Uma linha deste trecho parece ter recomeçado, mas não foi confirmada; pode faltar texto.",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "Alguns trechos não puderam ser transcritos e outros não foram confirmados; pode faltar texto.",
            "Bu bölümü yeniden dök" to
                "Transcrever esta parte de novo",
            "Diğer" to
                "Mais",
            "Metni düzenle" to
                "Editar texto",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "Compartilhar a tradução como arquivo (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "Compartilhar como arquivo de texto (.txt)",
            "Çeviri henüz hazır değil" to
                "A tradução ainda não está pronta",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "Não foi possível criar o arquivo (o armazenamento pode estar cheio)",
            "Çeviri altyazısı (.srt)" to
                "Legendas da tradução (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "Legendas (.srt) · transcrição original",
            "Altyazı (.srt)" to
                "Legendas (.srt)",
            "✓ Yazıya döküldü" to
                "✓ Transcrito",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} s · algumas partes não puderam ser transcritas",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ Transcrito em {0} s",
            "Geri" to
                "Voltar",
            "Notlarım" to
                "Minhas notas",
            "Notlarda ara…" to
                "Buscar nas notas…",
            "Aramayı temizle" to
                "Limpar busca",
            "Henüz not yok." to
                "Ainda não há notas.",
            "\"{0}\" hiçbir notta geçmiyor." to
                "Nenhuma nota contém \"{0}\".",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "{0}/{1} notas. Ao atingir o limite, a nota mais antiga é removida quando chega uma nova; exporte as notas importantes.",
            "Notu aç" to
                "Abrir nota",
            "Notu sil" to
                "Excluir nota",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — serviço de voz para texto, Termos de Serviço do Wit.ai\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit Tradução — Termos de Serviço das APIs do Google\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK, User Messaging Platform — Termos de Serviço do Google\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX, Jetpack Compose, Kotlin — Licença Apache 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "Permissão de notificação negada; não podemos avisar quando terminar",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "Tamanho do texto, espaçamento e acompanhar o áudio: o botão \"Aa\" na tela da nota.",
            "Transkript" to
                "Transcrição",
            "Varsayılan konuşma dili" to
                "Idioma da fala padrão",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ O áudio é transcrito em segundos (requer internet). O áudio compartilhado do WhatsApp é transcrito neste idioma; se for outro, mude o idioma no menu da nota.",
            "Varsayılan çeviri dili" to
                "Idioma de tradução padrão",
            "İndirilen dil paketleri" to
                "Pacotes de idioma baixados",
            "Yükleniyor…" to
                "Carregando…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "Ainda não há pacotes de idioma. Um é baixado automaticamente na primeira tradução (~30 MB).",
            "Sil" to
                "Excluir",
            "{0} paketi silindi" to
                "Pacote de {0} excluído",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "A tradução é feita no seu celular; seu texto não é enviado a nenhum servidor.",
            "Bildirimler" to
                "Notificações",
            "İşlem bitince haber ver" to
                "Avisar quando terminar",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "Se você sair do app, recebe uma notificação quando uma transcrição longa terminar.",
            "Gizlilik" to
                "Privacidade",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "O áudio é enviado ao serviço Wit.ai da Meta para ser transcrito. A Meta trata esses dados conforme seus próprios termos; pela política do Wit.ai, os dados de voz ficam guardados por até 90 dias e podem ser analisados para melhorar o reconhecimento de fala. Não temos servidor; suas notas e a cópia temporária do áudio ficam só no seu celular. A tradução é feita no aparelho. Os anúncios são exibidos pelo Google AdMob. Compartilhe apenas áudios que você tem o direito de transcrever.",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "Política de privacidade",
            "Tüm notları sil ({0})" to
                "Excluir todas as notas ({0})",
            "Reklam tercihleri" to
                "Preferências de anúncios",
            "Hakkında" to
                "Sobre",
            "Sürüm" to
                "Versão",
            "Geliştirici araçları açıldı" to
                "Ferramentas de desenvolvedor ativadas",
            "Geri bildirim gönder" to
                "Enviar feedback",
            " geri bildirim ({0})" to
                " feedback ({0})",
            "Açık kaynak lisansları" to
                "Licenças de código aberto",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "Este app não é desenvolvido, endossado nem patrocinado pelo WhatsApp ou pela Meta. WhatsApp é uma marca da WhatsApp LLC. A transcrição usa o serviço Wit.ai da Meta.",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "Avaliar o app",
            "Tüm notlar silinsin mi?" to
                "Excluir todas as notas?",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "Todas as notas, os arquivos exportados e os registros de medição deste celular serão excluídos. Qualquer transcrição em andamento é interrompida. Isso não pode ser desfeito.",
            "Kapat" to
                "Fechar",
            "Sesli mesaja uzun bas" to
                "Toque e segure o áudio",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "Na conversa do WhatsApp, até a mensagem ficar selecionada",
            "Paylaş simgesine dokun" to
                "Toque no ícone Compartilhar",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "Na barra superior (em alguns celulares, no menu ⋮)",
            "Listeden {0} uygulamasını seç" to
                "Escolha {0} na lista",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "O texto é preparado na hora. Da próxima vez, o app aparece entre os primeiros da lista.",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "{0} áudios transcritos; todos estão nas suas notas",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "{0} áudios transcritos, {1} com falha",
            "{0} ile yazıya döküldü" to
                "Transcrito com {0}",
            "Ses hazırlanıyor… ({0}/{1})" to
                "Preparando o áudio… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "A fala está em outro idioma? Tente com um toque:",
            "Paylaşırken imza ekle" to
                "Adicionar assinatura ao compartilhar",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "O nome e o link do app são adicionados abaixo do texto que você compartilha.",
            "Reklamsız kullanım" to
                "Uso sem anúncios",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "Os anúncios estão desativados. Tempo restante: cerca de {0} horas.",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "Assista a um anúncio curto e fique 24 horas sem anúncios.",
            "Reklam izle · 24 saat reklamsız" to
                "Assistir a um anúncio · 24 h sem anúncios",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "Sem anúncios por 24 horas. Obrigado!",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "Assista ao anúncio até o fim para ganhar a recompensa.",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "O anúncio não carregou agora. Tente de novo em instantes.",
        )
    }

    val RU: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "Подготовка аудио…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "Файл слишком большой (макс. {0} МБ)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "Аудио слишком короткое (минимум полсекунды)",
            "Ses açılamadı" to
                "Не удалось открыть аудио",
            "Dosya bu telefon için çok büyük" to
                "Этот файл слишком большой для этого телефона",
            "Ses açılamadı: {0}" to
                "Не удалось открыть аудио: {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "Аудио этой заметки больше нет; поделитесь им ещё раз",
            "Hazırlanıyor…" to
                "Подготовка…",
            "Yazıya dökülemedi: {0}" to
                "Не удалось расшифровать: {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "Слабое соединение или нет интернета. Проверьте подключение и повторите попытку.",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "Сервис сейчас перегружен. Повторите через несколько секунд.",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "Сервис расшифровки для языка «{0}» сейчас недоступен. Обновите приложение и повторите попытку.",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "Сервис расшифровки не отвечает. Повторите чуть позже.",
            "Yazıya dökülemedi. Tekrar dene." to
                "Не удалось расшифровать. Повторите попытку.",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "Нужен интернет. Подключитесь и повторите попытку.",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "Расшифровка для языка «{0}» сейчас недоступна. Измените язык речи.",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ {0}–{1} не удалось расшифровать]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "Речь не обнаружена. Язык речи — {0}? Если нет, измените язык и повторите попытку.",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "Текст готов, но сохранить его не удалось (возможно, память заполнена)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "Некоторые части не удалось расшифровать; они отмечены в заметке",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "Одну часть не удалось полностью проверить; она отмечена под заметкой",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "Части отредактированной заметки нельзя расшифровать заново",
            "Önce mevcut işlem bitsin" to
                "Дождитесь завершения текущей задачи",
            "Bu dil için hızlı mod yok" to
                "Расшифровка для этого языка недоступна",
            "Bunun için internet bağlantısı gerekiyor" to
                "Для этого нужен интернет",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "Повторная расшифровка части ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "Не удалось расшифровать часть заново; текущий текст сохранён",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "Расшифровано заново, но часть всё ещё не удалось полностью проверить",
            "Bölüm yeniden döküldü" to
                "Часть расшифрована заново",
            "Geliştirici modu kapatıldı" to
                "Режим разработчика выключен",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "Пустой текст сохранить нельзя. Чтобы удалить заметку, используйте меню.",
            "Bu not silinmiş" to
                "Эта заметка удалена",
            "Kaydedildi" to
                "Сохранено",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "Не удалось сохранить (возможно, память заполнена). Черновик остаётся открытым.",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "Дождитесь завершения текущей задачи или отмените её",
            "Not silinemedi (depolama hatası)" to
                "Не удалось удалить заметку (ошибка хранилища)",
            "Geri alınamadı (depolama hatası)" to
                "Не удалось отменить (ошибка хранилища)",
            "Notlar silinemedi (depolama hatası)" to
                "Не удалось удалить заметки (ошибка хранилища)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "Все заметки и файлы в приложении удалены",
            "Kaynak dil tanınmadı" to
                "Исходный язык не распознан",
            "{0} için çeviri desteklenmiyor" to
                "Перевод для языка «{0}» не поддерживается",
            "Çeviri yapılamadı: {0}" to
                "Не удалось перевести: {0}",
            "internet bağlantısını kontrol et" to
                "проверьте подключение к интернету",
            "Ses çalınamadı" to
                "Не удалось воспроизвести аудио",
            "Dosya boş" to
                "Файл пуст",
            "Döküm tamamlandı" to
                "Расшифровка завершена",
            "Metnin hazır" to
                "Ваш текст готов",
            "Açmak için dokun" to
                "Нажмите, чтобы открыть",
            "{0} ile yazıya dök" to
                "Расшифровать в {0}",
            "Reklam" to
                "Реклама",
            "Dosya açılamadı" to
                "Не удалось открыть файл",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "Не удалось прочитать файл (формат не поддерживается)",
            "Bu dosyada ses bulunamadı" to
                "В этом файле нет аудио",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "Пока поддерживается аудио до 30 минут",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "Этот аудиоформат ({0}) не поддерживается на вашем телефоне",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "Не удалось декодировать аудио (декодер не отвечает)",
            "Metin dosyasını paylaş" to
                "Поделиться текстовым файлом",
            "Altyazı dosyasını paylaş" to
                "Поделиться файлом субтитров",
            "{0} çeviri için desteklenmiyor" to
                "Язык «{0}» не поддерживается для перевода",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "Загрузка языкового пакета (один раз, ~30 МБ)…",
            "Çevriliyor {0}/{1}…" to
                "Перевод {0}/{1}…",
            "Paylaş" to
                "Поделиться",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "WhatsApp не найден; можно выбрать файл",
            "Ana sayfaya dön" to
                "На главную",
            "Ayarlar" to
                "Настройки",
            "Dinleyemiyorsan,\noku." to
                "Не можете слушать?\nЧитайте.",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "В WhatsApp нажмите и удерживайте голосовое сообщение → Поделиться → {0}. Текст готов за секунды.",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "Поделитесь голосовым сообщением с {0} и прочитайте его за секунды.",
            "WhatsApp'ı aç" to
                "Открыть WhatsApp",
            "Ses ya da video dosyası seç" to
                "Выбрать аудио- или видеофайл",
            "Ses / video dosyası seç" to
                "Выбрать аудио / видео",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ Расшифровка вот-вот начнётся",
            "Yazıya dökülüyor…" to
                "Расшифровка…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ За секунды · {0}",
            "Bir sorun oldu" to
                "Что-то пошло не так",
            "İptal" to
                "Отмена",
            "Tekrar dene" to
                "Повторить",
            "Konuşma dili: {0} ▾" to
                "Язык речи: {0} ▾",
            "Notlarda ara" to
                "Поиск по заметкам",
            "Silindi: {0}" to
                "Удалено: {0}",
            "Geri al" to
                "Отменить",
            "Son notlar" to
                "Недавние заметки",
            "Tümü ({0})" to
                "Все ({0})",
            "Silmek için nota basılı tut" to
                "Нажмите и удерживайте заметку, чтобы удалить её",
            "Kılavuzu kapat" to
                "Скрыть инструкцию",
            "Kılavuzu aç" to
                "Показать инструкцию",
            "Nasıl kullanılır?" to
                "Как пользоваться",
            "Döküm bitince düzenleyebilirsin" to
                "Редактировать можно после завершения расшифровки",
            "Not metni düzenleme alanı" to
                "Поле редактирования текста заметки",
            " · düzenlendi" to
                " · изменено",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · вид с метками времени доступен для неизменённого текста",
            "Vazgeç" to
                "Отмена",
            "Kaydet" to
                "Сохранить",
            "Kopyala" to
                "Копировать",
            "Kopyalandı" to
                "Скопировано",
            "Değişiklikler kaydedilmedi" to
                "Изменения не сохранены",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "Если выйти без сохранения, изменения будут потеряны.",
            "Kaydetme" to
                "Не сохранять",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · слов: {2}",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "Настройки чтения (размер текста, межстрочный интервал, тема)",
            "Diğer seçenekler" to
                "Другие действия",
            "⚡ Yeniden dök ({0})" to
                "⚡ Расшифровать заново ({0})",
            "Dil: {0} ile yeniden dök" to
                "Расшифровать заново: {0}",
            "Bu notu sil" to
                "Удалить эту заметку",
            "Okuma ayarları" to
                "Настройки чтения",
            "Yazı boyutu" to
                "Размер текста",
            "Yazıyı küçült" to
                "Уменьшить текст",
            "Örnek metin · {0}" to
                "Пример текста · {0}",
            "Yazıyı büyüt" to
                "Увеличить текст",
            "Satır aralığı" to
                "Межстрочный интервал",
            "Sıkı" to
                "Плотный",
            "Normal" to
                "Обычный",
            "Geniş" to
                "Широкий",
            "Görünüm" to
                "Вид",
            "Paragraf" to
                "Абзацы",
            "Zaman damgalı" to
                "С метками времени",
            "Sesle takip" to
                "Следовать за аудио",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "Во время воспроизведения текущая фраза подсвечивается, и экран следует за ней. Прокрутка вручную приостанавливает следование.",
            "Tema" to
                "Тема",
            "Sistem" to
                "Системная",
            "Açık" to
                "Светлая",
            "Koyu" to
                "Тёмная",
            "Canlı · ⚡ yazıya dökülüyor" to
                "В эфире · ⚡ расшифровка",
            "Canlı · %{0}" to
                "В эфире · {0}%",
            "Sesi duraklat" to
                "Приостановить аудио",
            "Sesi oynat" to
                "Воспроизвести аудио",
            "Ses dalgası; dokunduğun yere atlar" to
                "Звуковая волна; нажмите для перехода",
            "Oynatıcıyı daralt" to
                "Свернуть плеер",
            "Oynatıcıyı genişlet" to
                "Развернуть плеер",
            "−5 sn" to
                "−5 с",
            "5 saniye geri" to
                "Назад на 5 секунд",
            "+5 sn" to
                "+5 с",
            "5 saniye ileri" to
                "Вперёд на 5 секунд",
            "Metin" to
                "Текст",
            "Çeviri" to
                "Перевод",
            "Metin birazdan burada belirecek…" to
                "Текст скоро появится здесь…",
            "Canlıya dön" to
                "Вернуться к эфиру",
            "Okunan yere dön" to
                "Вернуться к текущему месту",
            "Buradan dinle" to
                "Слушать отсюда",
            "Çevir" to
                "Перевести",
            "Google Çeviri'de aç" to
                "Открыть в Google Переводчике",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "Длинный текст: скопирован в буфер обмена, вставьте его в Google Переводчик",
            "Google Çeviri'de aç ↗" to
                "Открыть в Google Переводчике ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "Текст отправляется в Google · нужен интернет",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "Перевод выше выполняется на устройстве; каждый языковой пакет загружается только один раз.",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ Одну часть не удалось расшифровать",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ Не удалось расшифровать частей: {0}",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ Одну часть не удалось полностью проверить",
            "⚠ {0} bölümde sorun var" to
                "⚠ Частей с проблемами: {0}",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "Этого отрезка нет в тексте из-за ошибки соединения или сервиса.",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "Похоже, строка в этом отрезке началась заново, но не была подтверждена; текст может быть неполным.",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "Некоторые отрезки не удалось расшифровать, а некоторые не подтверждены; текст может быть неполным.",
            "Bu bölümü yeniden dök" to
                "Расшифровать эту часть заново",
            "Diğer" to
                "Ещё",
            "Metni düzenle" to
                "Изменить текст",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "Поделиться переводом как файлом (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "Поделиться как текстовым файлом (.txt)",
            "Çeviri henüz hazır değil" to
                "Перевод ещё не готов",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "Не удалось создать файл (возможно, память заполнена)",
            "Çeviri altyazısı (.srt)" to
                "Субтитры перевода (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "Субтитры (.srt) · исходная расшифровка",
            "Altyazı (.srt)" to
                "Субтитры (.srt)",
            "✓ Yazıya döküldü" to
                "✓ Расшифровано",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} с · некоторые части не удалось расшифровать",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ Расшифровано за {0} с",
            "Geri" to
                "Назад",
            "Notlarım" to
                "Мои заметки",
            "Notlarda ara…" to
                "Поиск по заметкам…",
            "Aramayı temizle" to
                "Очистить поиск",
            "Henüz not yok." to
                "Заметок пока нет.",
            "\"{0}\" hiçbir notta geçmiyor." to
                "Ни в одной заметке нет \"{0}\".",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "Заметок: {0}/{1}. Когда лимит достигнут, при появлении новой заметки самая старая удаляется; экспортируйте важные заметки.",
            "Notu aç" to
                "Открыть заметку",
            "Notu sil" to
                "Удалить заметку",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — сервис распознавания речи, Условия использования Wit.ai\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit Перевод — Условия использования Google APIs\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK, User Messaging Platform — Условия использования Google\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX, Jetpack Compose, Kotlin — лицензия Apache 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "Уведомления запрещены; мы не сможем сообщить о завершении",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "Размер текста, интервал и следование за аудио: кнопка \"Aa\" на экране заметки.",
            "Transkript" to
                "Расшифровка",
            "Varsayılan konuşma dili" to
                "Язык речи по умолчанию",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ Аудио расшифровывается за секунды (нужен интернет). Аудио из WhatsApp расшифровывается на этом языке; если язык другой, измените его в меню заметки.",
            "Varsayılan çeviri dili" to
                "Язык перевода по умолчанию",
            "İndirilen dil paketleri" to
                "Загруженные языковые пакеты",
            "Yükleniyor…" to
                "Загрузка…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "Языковых пакетов пока нет. Пакет загрузится автоматически при первом переводе (~30 МБ).",
            "Sil" to
                "Удалить",
            "{0} paketi silindi" to
                "Пакет «{0}» удалён",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "Перевод выполняется на телефоне; ваш текст не отправляется ни на какой сервер.",
            "Bildirimler" to
                "Уведомления",
            "İşlem bitince haber ver" to
                "Сообщить о завершении",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "Если выйти из приложения, вы получите уведомление, когда завершится долгая расшифровка.",
            "Gizlilik" to
                "Конфиденциальность",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "Аудио отправляется в сервис Wit.ai компании Meta для расшифровки. Meta обрабатывает эти данные на своих условиях; согласно политике Wit.ai голосовые данные хранятся до 90 дней и могут анализироваться для улучшения распознавания речи. У нас нет своего сервера; заметки и временная копия аудио остаются только на вашем телефоне. Перевод выполняется на устройстве. Рекламу показывает Google AdMob. Делитесь только теми аудио, которые вы вправе расшифровывать.",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "Политика конфиденциальности",
            "Tüm notları sil ({0})" to
                "Удалить все заметки ({0})",
            "Reklam tercihleri" to
                "Настройки рекламы",
            "Hakkında" to
                "О приложении",
            "Sürüm" to
                "Версия",
            "Geliştirici araçları açıldı" to
                "Инструменты разработчика включены",
            "Geri bildirim gönder" to
                "Отправить отзыв",
            " geri bildirim ({0})" to
                " отзыв ({0})",
            "Açık kaynak lisansları" to
                "Лицензии открытого ПО",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "Это приложение не разработано, не одобрено и не спонсируется WhatsApp или Meta. WhatsApp — товарный знак WhatsApp LLC. Для расшифровки используется сервис Wit.ai компании Meta.",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "Оценить приложение",
            "Tüm notlar silinsin mi?" to
                "Удалить все заметки?",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "Все заметки, экспортированные файлы и журналы измерений на этом телефоне будут удалены. Текущая расшифровка будет остановлена. Это действие нельзя отменить.",
            "Kapat" to
                "Закрыть",
            "Sesli mesaja uzun bas" to
                "Нажмите и удерживайте голосовое сообщение",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "В чате WhatsApp, пока сообщение не будет выбрано",
            "Paylaş simgesine dokun" to
                "Нажмите значок «Поделиться»",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "На верхней панели (на некоторых телефонах — в меню ⋮)",
            "Listeden {0} uygulamasını seç" to
                "Выберите {0} в списке",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "Текст готовится сразу. В следующий раз приложение будет в начале списка.",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "Расшифровано аудио: {0}; все в ваших заметках",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "Расшифровано аудио: {0}, с ошибкой: {1}",
            "{0} ile yazıya döküldü" to
                "Расшифровано в {0}",
            "Ses hazırlanıyor… ({0}/{1})" to
                "Подготовка аудио… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "Речь на другом языке? Попробуйте одним нажатием:",
            "Paylaşırken imza ekle" to
                "Добавлять подпись при отправке",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "Под текстом, которым вы делитесь, добавляются название приложения и ссылка.",
            "Reklamsız kullanım" to
                "Без рекламы",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "Реклама отключена. Осталось примерно {0} ч.",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "Посмотрите одну короткую рекламу и пользуйтесь 24 часа без рекламы.",
            "Reklam izle · 24 saat reklamsız" to
                "Смотреть рекламу · 24 ч без рекламы",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "24 часа без рекламы. Спасибо!",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "Чтобы получить награду, досмотрите рекламу до конца.",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "Сейчас не удалось загрузить рекламу. Повторите чуть позже.",
        )
    }

    val AR: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "جارٍ تجهيز الصوت…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "الملف كبير جدًا (الحد الأقصى {0} ميغابايت)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "الصوت قصير جدًا (نصف ثانية على الأقل)",
            "Ses açılamadı" to
                "تعذّر فتح الصوت",
            "Dosya bu telefon için çok büyük" to
                "هذا الملف كبير جدًا لهذا الهاتف",
            "Ses açılamadı: {0}" to
                "تعذّر فتح الصوت: {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "صوت هذه الملاحظة لم يعد موجودًا؛ شارك الصوت مرة أخرى",
            "Hazırlanıyor…" to
                "جارٍ التجهيز…",
            "Yazıya dökülemedi: {0}" to
                "تعذّر التحويل إلى نص: {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "اتصال الإنترنت ضعيف أو غير موجود. تحقق من الاتصال وحاول مرة أخرى.",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "الخدمة مشغولة الآن. حاول مرة أخرى بعد ثوانٍ.",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "تعذّر الوصول إلى خدمة التحويل للغة {0} حاليًا. حدّث التطبيق وحاول مرة أخرى.",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "خدمة التحويل إلى نص لا تستجيب. حاول مرة أخرى بعد قليل.",
            "Yazıya dökülemedi. Tekrar dene." to
                "تعذّر التحويل إلى نص. حاول مرة أخرى.",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "يلزم اتصال بالإنترنت. اتصل ثم حاول مرة أخرى.",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "التحويل إلى نص غير متاح للغة {0} حاليًا. غيّر لغة الكلام.",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ تعذّر تحويل {0}–{1} إلى نص]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "لم يُكتشف كلام. هل لغة الكلام هي {0}؟ إن لم تكن كذلك فغيّر اللغة وحاول مرة أخرى.",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "النص جاهز لكن تعذّر حفظه (قد تكون مساحة التخزين ممتلئة)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "تعذّر تحويل بعض الأجزاء؛ وُضعت عليها علامة في الملاحظة",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "تعذّر التحقق من جزء بالكامل؛ وُضعت عليه علامة أسفل الملاحظة",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "لا يمكن إعادة تحويل أجزاء من ملاحظة معدّلة",
            "Önce mevcut işlem bitsin" to
                "انتظر انتهاء المهمة الحالية",
            "Bu dil için hızlı mod yok" to
                "التحويل إلى نص غير متاح لهذه اللغة",
            "Bunun için internet bağlantısı gerekiyor" to
                "يتطلب هذا اتصالًا بالإنترنت",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "جارٍ إعادة تحويل الجزء ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "تعذّرت إعادة تحويل الجزء؛ تم الإبقاء على النص الحالي",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "أُعيد التحويل، لكن تعذّر التحقق من الجزء بالكامل مرة أخرى",
            "Bölüm yeniden döküldü" to
                "أُعيد تحويل الجزء",
            "Geliştirici modu kapatıldı" to
                "تم إيقاف وضع المطوّر",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "لا يمكن حفظ نص فارغ. استخدم القائمة لحذف الملاحظة.",
            "Bu not silinmiş" to
                "تم حذف هذه الملاحظة",
            "Kaydedildi" to
                "تم الحفظ",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "تعذّر الحفظ (قد تكون مساحة التخزين ممتلئة). مسودتك ما زالت مفتوحة.",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "انتظر انتهاء المهمة الحالية أو ألغِها",
            "Not silinemedi (depolama hatası)" to
                "تعذّر حذف الملاحظة (خطأ في التخزين)",
            "Geri alınamadı (depolama hatası)" to
                "تعذّر التراجع (خطأ في التخزين)",
            "Notlar silinemedi (depolama hatası)" to
                "تعذّر حذف الملاحظات (خطأ في التخزين)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "تم حذف جميع الملاحظات والملفات في التطبيق",
            "Kaynak dil tanınmadı" to
                "لم يتم التعرف على اللغة المصدر",
            "{0} için çeviri desteklenmiyor" to
                "الترجمة غير مدعومة للغة {0}",
            "Çeviri yapılamadı: {0}" to
                "تعذّرت الترجمة: {0}",
            "internet bağlantısını kontrol et" to
                "تحقق من اتصالك بالإنترنت",
            "Ses çalınamadı" to
                "تعذّر تشغيل الصوت",
            "Dosya boş" to
                "الملف فارغ",
            "Döküm tamamlandı" to
                "اكتمل التحويل إلى نص",
            "Metnin hazır" to
                "نصّك جاهز",
            "Açmak için dokun" to
                "انقر للفتح",
            "{0} ile yazıya dök" to
                "حوّل إلى نص عبر {0}",
            "Reklam" to
                "إعلان",
            "Dosya açılamadı" to
                "تعذّر فتح الملف",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "تعذّرت قراءة هذا الملف (تنسيق غير مدعوم)",
            "Bu dosyada ses bulunamadı" to
                "لا يوجد صوت في هذا الملف",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "حاليًا يُدعم الصوت حتى 30 دقيقة",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "تنسيق الصوت هذا ({0}) غير مدعوم على هاتفك",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "تعذّر فك ترميز الصوت (برنامج الترميز لا يستجيب)",
            "Metin dosyasını paylaş" to
                "مشاركة ملف النص",
            "Altyazı dosyasını paylaş" to
                "مشاركة ملف الترجمة النصية",
            "{0} çeviri için desteklenmiyor" to
                "اللغة {0} غير مدعومة للترجمة",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "جارٍ تنزيل حزمة اللغة (مرة واحدة، ~30 ميغابايت)…",
            "Çevriliyor {0}/{1}…" to
                "جارٍ الترجمة {0}/{1}…",
            "Paylaş" to
                "مشاركة",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "لم يُعثر على واتساب؛ يمكنك اختيار ملف بدلًا من ذلك",
            "Ana sayfaya dön" to
                "العودة إلى الرئيسية",
            "Ayarlar" to
                "الإعدادات",
            "Dinleyemiyorsan,\noku." to
                "لا تستطيع الاستماع؟\nاقرأ.",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "في واتساب، اضغط مطولًا على رسالة صوتية ← مشاركة ← {0}. النص جاهز خلال ثوانٍ.",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "شارك رسالة صوتية مع {0} واقرأها خلال ثوانٍ.",
            "WhatsApp'ı aç" to
                "فتح واتساب",
            "Ses ya da video dosyası seç" to
                "اختر ملف صوت أو فيديو",
            "Ses / video dosyası seç" to
                "اختر ملف صوت / فيديو",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ سيبدأ التحويل بعد لحظات",
            "Yazıya dökülüyor…" to
                "جارٍ التحويل إلى نص…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ خلال ثوانٍ · {0}",
            "Bir sorun oldu" to
                "حدث خطأ ما",
            "İptal" to
                "إلغاء",
            "Tekrar dene" to
                "إعادة المحاولة",
            "Konuşma dili: {0} ▾" to
                "لغة الكلام: {0} ▾",
            "Notlarda ara" to
                "البحث في الملاحظات",
            "Silindi: {0}" to
                "تم الحذف: {0}",
            "Geri al" to
                "تراجع",
            "Son notlar" to
                "أحدث الملاحظات",
            "Tümü ({0})" to
                "الكل ({0})",
            "Silmek için nota basılı tut" to
                "اضغط مطولًا على ملاحظة لحذفها",
            "Kılavuzu kapat" to
                "إخفاء الدليل",
            "Kılavuzu aç" to
                "إظهار الدليل",
            "Nasıl kullanılır?" to
                "طريقة الاستخدام",
            "Döküm bitince düzenleyebilirsin" to
                "يمكنك التعديل بعد انتهاء التحويل",
            "Not metni düzenleme alanı" to
                "حقل تعديل نص الملاحظة",
            " · düzenlendi" to
                " · معدّلة",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · عرض الطوابع الزمنية متاح للنص غير المعدّل",
            "Vazgeç" to
                "إلغاء",
            "Kaydet" to
                "حفظ",
            "Kopyala" to
                "نسخ",
            "Kopyalandı" to
                "تم النسخ",
            "Değişiklikler kaydedilmedi" to
                "تغييرات غير محفوظة",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "إذا خرجت دون حفظ فستفقد تغييراتك.",
            "Kaydetme" to
                "عدم الحفظ",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · {2} كلمة",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "إعدادات القراءة (حجم النص، تباعد الأسطر، المظهر)",
            "Diğer seçenekler" to
                "خيارات أخرى",
            "⚡ Yeniden dök ({0})" to
                "⚡ إعادة التحويل ({0})",
            "Dil: {0} ile yeniden dök" to
                "إعادة التحويل باللغة {0}",
            "Bu notu sil" to
                "حذف هذه الملاحظة",
            "Okuma ayarları" to
                "إعدادات القراءة",
            "Yazı boyutu" to
                "حجم النص",
            "Yazıyı küçült" to
                "تصغير النص",
            "Örnek metin · {0}" to
                "نص تجريبي · {0}",
            "Yazıyı büyüt" to
                "تكبير النص",
            "Satır aralığı" to
                "تباعد الأسطر",
            "Sıkı" to
                "ضيق",
            "Normal" to
                "عادي",
            "Geniş" to
                "واسع",
            "Görünüm" to
                "العرض",
            "Paragraf" to
                "فقرات",
            "Zaman damgalı" to
                "بطوابع زمنية",
            "Sesle takip" to
                "متابعة الصوت",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "أثناء تشغيل الصوت تُميَّز الجملة الحالية وتتبعها الشاشة. التمرير يدويًا يوقف المتابعة مؤقتًا.",
            "Tema" to
                "المظهر",
            "Sistem" to
                "النظام",
            "Açık" to
                "فاتح",
            "Koyu" to
                "داكن",
            "Canlı · ⚡ yazıya dökülüyor" to
                "مباشر · ⚡ جارٍ التحويل",
            "Canlı · %{0}" to
                "مباشر · {0}%",
            "Sesi duraklat" to
                "إيقاف الصوت مؤقتًا",
            "Sesi oynat" to
                "تشغيل الصوت",
            "Ses dalgası; dokunduğun yere atlar" to
                "الموجة الصوتية؛ انقر للانتقال",
            "Oynatıcıyı daralt" to
                "تصغير المشغّل",
            "Oynatıcıyı genişlet" to
                "توسيع المشغّل",
            "−5 sn" to
                "−5 ث",
            "5 saniye geri" to
                "رجوع 5 ثوانٍ",
            "+5 sn" to
                "+5 ث",
            "5 saniye ileri" to
                "تقديم 5 ثوانٍ",
            "Metin" to
                "النص",
            "Çeviri" to
                "الترجمة",
            "Metin birazdan burada belirecek…" to
                "سيظهر النص هنا بعد لحظات…",
            "Canlıya dön" to
                "العودة إلى المباشر",
            "Okunan yere dön" to
                "العودة إلى الموضع الحالي",
            "Buradan dinle" to
                "استمع من هنا",
            "Çevir" to
                "ترجمة",
            "Google Çeviri'de aç" to
                "فتح في ترجمة Google",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "النص طويل: نُسخ إلى الحافظة، الصقه في ترجمة Google",
            "Google Çeviri'de aç ↗" to
                "فتح في ترجمة Google ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "يُرسل النص إلى Google · يلزم الإنترنت",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "الترجمة أعلاه تتم على جهازك؛ تُنزَّل كل حزمة لغة مرة واحدة فقط.",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ تعذّر تحويل جزء واحد",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ تعذّر تحويل {0} أجزاء",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ تعذّر التحقق من جزء واحد بالكامل",
            "⚠ {0} bölümde sorun var" to
                "⚠ توجد مشكلات في {0} أجزاء",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "هذا المقطع غير موجود في النص بسبب خطأ في الاتصال أو الخدمة.",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "يبدو أن سطرًا في هذا المقطع بدأ من جديد لكنه لم يتأكد؛ قد يكون النص ناقصًا.",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "تعذّر تحويل بعض المقاطع ولم يتأكد بعضها الآخر؛ قد يكون النص ناقصًا.",
            "Bu bölümü yeniden dök" to
                "إعادة تحويل هذا الجزء",
            "Diğer" to
                "المزيد",
            "Metni düzenle" to
                "تعديل النص",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "مشاركة الترجمة كملف (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "مشاركة كملف نصي (.txt)",
            "Çeviri henüz hazır değil" to
                "الترجمة ليست جاهزة بعد",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "تعذّر إنشاء الملف (قد تكون مساحة التخزين ممتلئة)",
            "Çeviri altyazısı (.srt)" to
                "ترجمة نصية للترجمة (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "ترجمة نصية (.srt) · النص الأصلي",
            "Altyazı (.srt)" to
                "ترجمة نصية (.srt)",
            "✓ Yazıya döküldü" to
                "✓ تم التحويل إلى نص",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} ث · تعذّر تحويل بعض الأجزاء",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ تم التحويل خلال {0} ث",
            "Geri" to
                "رجوع",
            "Notlarım" to
                "ملاحظاتي",
            "Notlarda ara…" to
                "البحث في الملاحظات…",
            "Aramayı temizle" to
                "مسح البحث",
            "Henüz not yok." to
                "لا توجد ملاحظات بعد.",
            "\"{0}\" hiçbir notta geçmiyor." to
                "لا توجد ملاحظة تحتوي على \"{0}\".",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "{0}/{1} ملاحظة. عند بلوغ الحد تُحذف أقدم ملاحظة مع وصول ملاحظة جديدة؛ صدّر الملاحظات المهمة.",
            "Notu aç" to
                "فتح الملاحظة",
            "Notu sil" to
                "حذف الملاحظة",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — خدمة تحويل الكلام إلى نص، شروط خدمة Wit.ai\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit للترجمة — شروط خدمة Google APIs\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK، User Messaging Platform — شروط خدمة Google\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX، Jetpack Compose، Kotlin — ترخيص Apache 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "رُفض إذن الإشعارات؛ لا يمكننا إعلامك عند الانتهاء",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "حجم النص وتباعد الأسطر ومتابعة الصوت: زر \"Aa\" في شاشة الملاحظة.",
            "Transkript" to
                "النص المحوَّل",
            "Varsayılan konuşma dili" to
                "لغة الكلام الافتراضية",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ يُحوَّل الصوت إلى نص خلال ثوانٍ (يلزم الإنترنت). الصوت الذي تشاركه من واتساب يُحوَّل بهذه اللغة؛ إن كانت مختلفة فغيّر اللغة من قائمة الملاحظة.",
            "Varsayılan çeviri dili" to
                "لغة الترجمة الافتراضية",
            "İndirilen dil paketleri" to
                "حزم اللغات المنزَّلة",
            "Yükleniyor…" to
                "جارٍ التحميل…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "لا توجد حزم لغات بعد. تُنزَّل حزمة تلقائيًا عند أول ترجمة (~30 ميغابايت).",
            "Sil" to
                "حذف",
            "{0} paketi silindi" to
                "تم حذف حزمة {0}",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "الترجمة تتم على هاتفك؛ لا يُرسل نصّك إلى أي خادم.",
            "Bildirimler" to
                "الإشعارات",
            "İşlem bitince haber ver" to
                "أعلمني عند الانتهاء",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "إذا غادرت التطبيق فستصلك إشعار عند انتهاء تحويل طويل.",
            "Gizlilik" to
                "الخصوصية",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "يُرسل الصوت إلى خدمة Wit.ai التابعة لشركة Meta لتحويله إلى نص. تعالج Meta هذه البيانات وفق شروطها؛ وبحسب سياسة Wit.ai تُحفظ البيانات الصوتية مدة تصل إلى 90 يومًا وقد تُحلَّل لتحسين التعرف على الكلام. ليس لدينا خادم؛ ملاحظاتك والنسخة المؤقتة من الصوت تبقى على هاتفك فقط. الترجمة تتم على الجهاز. الإعلانات تعرضها Google AdMob. شارك فقط الصوت الذي يحق لك تحويله إلى نص.",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "سياسة الخصوصية",
            "Tüm notları sil ({0})" to
                "حذف كل الملاحظات ({0})",
            "Reklam tercihleri" to
                "تفضيلات الإعلانات",
            "Hakkında" to
                "حول",
            "Sürüm" to
                "الإصدار",
            "Geliştirici araçları açıldı" to
                "تم تفعيل أدوات المطوّر",
            "Geri bildirim gönder" to
                "إرسال ملاحظات",
            " geri bildirim ({0})" to
                " ملاحظات ({0})",
            "Açık kaynak lisansları" to
                "تراخيص المصادر المفتوحة",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "هذا التطبيق ليس من تطوير واتساب أو Meta ولا يحظى بدعمهما أو رعايتهما. واتساب علامة تجارية لشركة WhatsApp LLC. يستخدم التحويل إلى نص خدمة Wit.ai التابعة لشركة Meta.",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "قيّم التطبيق",
            "Tüm notlar silinsin mi?" to
                "حذف كل الملاحظات؟",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "ستُحذف جميع الملاحظات والملفات المصدَّرة وسجلات القياس على هذا الهاتف. يتوقف أي تحويل جارٍ. لا يمكن التراجع عن هذا الإجراء.",
            "Kapat" to
                "إغلاق",
            "Sesli mesaja uzun bas" to
                "اضغط مطولًا على الرسالة الصوتية",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "في محادثة واتساب، حتى تُحدَّد الرسالة",
            "Paylaş simgesine dokun" to
                "انقر على أيقونة المشاركة",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "في الشريط العلوي (في بعض الهواتف داخل قائمة ⋮)",
            "Listeden {0} uygulamasını seç" to
                "اختر {0} من القائمة",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "يُجهَّز النص فورًا. في المرة القادمة يظهر التطبيق في أول القائمة.",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "تم تحويل {0} ملفات صوتية؛ كلها في ملاحظاتك",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "تم تحويل {0} ملفات صوتية، وفشل {1}",
            "{0} ile yazıya döküldü" to
                "حُوِّل إلى نص عبر {0}",
            "Ses hazırlanıyor… ({0}/{1})" to
                "جارٍ تجهيز الصوت… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "هل الكلام بلغة أخرى؟ جرّب بنقرة واحدة:",
            "Paylaşırken imza ekle" to
                "إضافة توقيع عند المشاركة",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "يُضاف اسم التطبيق ورابطه أسفل النص الذي تشاركه.",
            "Reklamsız kullanım" to
                "استخدام بلا إعلانات",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "الإعلانات متوقفة. الوقت المتبقي: نحو {0} ساعة.",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "شاهد إعلانًا قصيرًا واستخدم التطبيق 24 ساعة بلا إعلانات.",
            "Reklam izle · 24 saat reklamsız" to
                "شاهد إعلانًا · 24 ساعة بلا إعلانات",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "لا إعلانات لمدة 24 ساعة. شكرًا لك!",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "شاهد الإعلان حتى النهاية للحصول على المكافأة.",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "تعذّر تحميل الإعلان الآن. حاول مرة أخرى بعد قليل.",
        )
    }

    val HI: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "ऑडियो तैयार हो रहा है…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "फ़ाइल बहुत बड़ी है (अधिकतम {0} MB)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "ऑडियो बहुत छोटा है (कम से कम आधा सेकंड)",
            "Ses açılamadı" to
                "ऑडियो नहीं खुल सका",
            "Dosya bu telefon için çok büyük" to
                "यह फ़ाइल इस फ़ोन के लिए बहुत बड़ी है",
            "Ses açılamadı: {0}" to
                "ऑडियो नहीं खुल सका: {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "इस नोट का ऑडियो अब नहीं है; ऑडियो दोबारा शेयर करें",
            "Hazırlanıyor…" to
                "तैयारी हो रही है…",
            "Yazıya dökülemedi: {0}" to
                "टेक्स्ट में नहीं बदला जा सका: {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "इंटरनेट कनेक्शन कमज़ोर है या नहीं है। कनेक्शन जाँचें और फिर कोशिश करें।",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "सेवा अभी व्यस्त है। कुछ सेकंड बाद फिर कोशिश करें।",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "{0} के लिए ट्रांसक्रिप्शन सेवा अभी उपलब्ध नहीं है। ऐप अपडेट करें और फिर कोशिश करें।",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "ट्रांसक्रिप्शन सेवा जवाब नहीं दे रही। थोड़ी देर बाद फिर कोशिश करें।",
            "Yazıya dökülemedi. Tekrar dene." to
                "टेक्स्ट में नहीं बदला जा सका। फिर कोशिश करें।",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "इंटरनेट कनेक्शन ज़रूरी है। कनेक्ट करें और फिर कोशिश करें।",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "{0} के लिए ट्रांसक्रिप्शन अभी उपलब्ध नहीं है। बोली की भाषा बदलें।",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ {0}–{1} टेक्स्ट में नहीं बदला जा सका]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "कोई बोली नहीं मिली। क्या बोली की भाषा {0} है? अगर नहीं, तो भाषा बदलें और फिर कोशिश करें।",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "टेक्स्ट तैयार है पर सेव नहीं हो सका (स्टोरेज भरा हो सकता है)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "कुछ हिस्से टेक्स्ट में नहीं बदले जा सके; वे नोट में चिह्नित हैं",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "एक हिस्सा पूरी तरह सत्यापित नहीं हो सका; वह नोट के नीचे चिह्नित है",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "बदले गए नोट के हिस्से दोबारा ट्रांसक्राइब नहीं किए जा सकते",
            "Önce mevcut işlem bitsin" to
                "मौजूदा काम पूरा होने दें",
            "Bu dil için hızlı mod yok" to
                "इस भाषा के लिए ट्रांसक्रिप्शन उपलब्ध नहीं है",
            "Bunun için internet bağlantısı gerekiyor" to
                "इसके लिए इंटरनेट कनेक्शन चाहिए",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "हिस्सा दोबारा ट्रांसक्राइब हो रहा है ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "हिस्सा दोबारा ट्रांसक्राइब नहीं हो सका; मौजूदा टेक्स्ट रखा गया",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "दोबारा ट्रांसक्राइब हुआ, पर हिस्सा अब भी पूरी तरह सत्यापित नहीं हो सका",
            "Bölüm yeniden döküldü" to
                "हिस्सा दोबारा ट्रांसक्राइब हुआ",
            "Geliştirici modu kapatıldı" to
                "डेवलपर मोड बंद किया गया",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "खाली टेक्स्ट सेव नहीं हो सकता। नोट हटाने के लिए मेन्यू इस्तेमाल करें।",
            "Bu not silinmiş" to
                "यह नोट हटाया जा चुका है",
            "Kaydedildi" to
                "सेव हो गया",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "सेव नहीं हो सका (स्टोरेज भरा हो सकता है)। आपका ड्राफ़्ट खुला है।",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "मौजूदा काम पूरा होने दें या उसे रद्द करें",
            "Not silinemedi (depolama hatası)" to
                "नोट नहीं हटाया जा सका (स्टोरेज की गड़बड़ी)",
            "Geri alınamadı (depolama hatası)" to
                "पहले जैसा नहीं किया जा सका (स्टोरेज की गड़बड़ी)",
            "Notlar silinemedi (depolama hatası)" to
                "नोट नहीं हटाए जा सके (स्टोरेज की गड़बड़ी)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "ऐप के सभी नोट और फ़ाइलें हटा दी गईं",
            "Kaynak dil tanınmadı" to
                "स्रोत भाषा पहचानी नहीं गई",
            "{0} için çeviri desteklenmiyor" to
                "{0} के लिए अनुवाद उपलब्ध नहीं है",
            "Çeviri yapılamadı: {0}" to
                "अनुवाद नहीं हो सका: {0}",
            "internet bağlantısını kontrol et" to
                "अपना इंटरनेट कनेक्शन जाँचें",
            "Ses çalınamadı" to
                "ऑडियो नहीं चल सका",
            "Dosya boş" to
                "फ़ाइल खाली है",
            "Döküm tamamlandı" to
                "ट्रांसक्रिप्शन पूरा हुआ",
            "Metnin hazır" to
                "आपका टेक्स्ट तैयार है",
            "Açmak için dokun" to
                "खोलने के लिए टैप करें",
            "{0} ile yazıya dök" to
                "{0} से टेक्स्ट में बदलें",
            "Reklam" to
                "विज्ञापन",
            "Dosya açılamadı" to
                "फ़ाइल नहीं खुल सकी",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "यह फ़ाइल पढ़ी नहीं जा सकी (फ़ॉर्मैट समर्थित नहीं)",
            "Bu dosyada ses bulunamadı" to
                "इस फ़ाइल में कोई ऑडियो नहीं मिला",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "फ़िलहाल 30 मिनट तक का ऑडियो समर्थित है",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "यह ऑडियो फ़ॉर्मैट ({0}) आपके फ़ोन पर समर्थित नहीं है",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "ऑडियो डिकोड नहीं हो सका (डिकोडर जवाब नहीं दे रहा)",
            "Metin dosyasını paylaş" to
                "टेक्स्ट फ़ाइल शेयर करें",
            "Altyazı dosyasını paylaş" to
                "सबटाइटल फ़ाइल शेयर करें",
            "{0} çeviri için desteklenmiyor" to
                "{0} अनुवाद के लिए समर्थित नहीं है",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "भाषा पैक डाउनलोड हो रहा है (सिर्फ़ एक बार, ~30 MB)…",
            "Çevriliyor {0}/{1}…" to
                "अनुवाद हो रहा है {0}/{1}…",
            "Paylaş" to
                "शेयर करें",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "WhatsApp नहीं मिला; आप फ़ाइल चुन सकते हैं",
            "Ana sayfaya dön" to
                "होम पर वापस जाएँ",
            "Ayarlar" to
                "सेटिंग",
            "Dinleyemiyorsan,\noku." to
                "सुन नहीं सकते?\nपढ़ लें।",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "WhatsApp में वॉइस मैसेज को दबाकर रखें → शेयर करें → {0}। टेक्स्ट कुछ ही सेकंड में तैयार।",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "वॉइस मैसेज {0} पर शेयर करें और कुछ ही सेकंड में पढ़ें।",
            "WhatsApp'ı aç" to
                "WhatsApp खोलें",
            "Ses ya da video dosyası seç" to
                "ऑडियो या वीडियो फ़ाइल चुनें",
            "Ses / video dosyası seç" to
                "ऑडियो / वीडियो फ़ाइल चुनें",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ ट्रांसक्रिप्शन अभी शुरू होगा",
            "Yazıya dökülüyor…" to
                "टेक्स्ट में बदला जा रहा है…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ कुछ ही सेकंड में · {0}",
            "Bir sorun oldu" to
                "कुछ गड़बड़ हो गई",
            "İptal" to
                "रद्द करें",
            "Tekrar dene" to
                "फिर कोशिश करें",
            "Konuşma dili: {0} ▾" to
                "बोली की भाषा: {0} ▾",
            "Notlarda ara" to
                "नोट में खोजें",
            "Silindi: {0}" to
                "हटाया गया: {0}",
            "Geri al" to
                "पहले जैसा करें",
            "Son notlar" to
                "हाल के नोट",
            "Tümü ({0})" to
                "सभी ({0})",
            "Silmek için nota basılı tut" to
                "नोट हटाने के लिए उसे दबाकर रखें",
            "Kılavuzu kapat" to
                "गाइड छिपाएँ",
            "Kılavuzu aç" to
                "गाइड दिखाएँ",
            "Nasıl kullanılır?" to
                "कैसे इस्तेमाल करें",
            "Döküm bitince düzenleyebilirsin" to
                "ट्रांसक्रिप्शन पूरा होने पर आप बदलाव कर सकते हैं",
            "Not metni düzenleme alanı" to
                "नोट का टेक्स्ट बदलने की जगह",
            " · düzenlendi" to
                " · बदला गया",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · टाइमस्टैम्प वाला व्यू बिना बदले टेक्स्ट के लिए उपलब्ध है",
            "Vazgeç" to
                "रद्द करें",
            "Kaydet" to
                "सेव करें",
            "Kopyala" to
                "कॉपी करें",
            "Kopyalandı" to
                "कॉपी हो गया",
            "Değişiklikler kaydedilmedi" to
                "बदलाव सेव नहीं हुए",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "बिना सेव किए बाहर जाने पर आपके बदलाव खो जाएँगे।",
            "Kaydetme" to
                "सेव न करें",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · {2} शब्द",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "पढ़ने की सेटिंग (टेक्स्ट का आकार, लाइनों की दूरी, थीम)",
            "Diğer seçenekler" to
                "और विकल्प",
            "⚡ Yeniden dök ({0})" to
                "⚡ दोबारा ट्रांसक्राइब करें ({0})",
            "Dil: {0} ile yeniden dök" to
                "{0} में दोबारा ट्रांसक्राइब करें",
            "Bu notu sil" to
                "यह नोट हटाएँ",
            "Okuma ayarları" to
                "पढ़ने की सेटिंग",
            "Yazı boyutu" to
                "टेक्स्ट का आकार",
            "Yazıyı küçült" to
                "टेक्स्ट छोटा करें",
            "Örnek metin · {0}" to
                "नमूना टेक्स्ट · {0}",
            "Yazıyı büyüt" to
                "टेक्स्ट बड़ा करें",
            "Satır aralığı" to
                "लाइनों की दूरी",
            "Sıkı" to
                "कम",
            "Normal" to
                "सामान्य",
            "Geniş" to
                "ज़्यादा",
            "Görünüm" to
                "व्यू",
            "Paragraf" to
                "पैराग्राफ़",
            "Zaman damgalı" to
                "टाइमस्टैम्प के साथ",
            "Sesle takip" to
                "ऑडियो के साथ चलें",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "ऑडियो चलते समय मौजूदा वाक्य हाइलाइट होता है और स्क्रीन उसके साथ चलती है। हाथ से स्क्रोल करने पर यह रुक जाता है।",
            "Tema" to
                "थीम",
            "Sistem" to
                "सिस्टम",
            "Açık" to
                "हल्की",
            "Koyu" to
                "गहरी",
            "Canlı · ⚡ yazıya dökülüyor" to
                "लाइव · ⚡ टेक्स्ट में बदल रहा है",
            "Canlı · %{0}" to
                "लाइव · {0}%",
            "Sesi duraklat" to
                "ऑडियो रोकें",
            "Sesi oynat" to
                "ऑडियो चलाएँ",
            "Ses dalgası; dokunduğun yere atlar" to
                "वेवफ़ॉर्म; जाने के लिए टैप करें",
            "Oynatıcıyı daralt" to
                "प्लेयर छोटा करें",
            "Oynatıcıyı genişlet" to
                "प्लेयर बड़ा करें",
            "−5 sn" to
                "−5 से",
            "5 saniye geri" to
                "5 सेकंड पीछे",
            "+5 sn" to
                "+5 से",
            "5 saniye ileri" to
                "5 सेकंड आगे",
            "Metin" to
                "टेक्स्ट",
            "Çeviri" to
                "अनुवाद",
            "Metin birazdan burada belirecek…" to
                "टेक्स्ट थोड़ी देर में यहाँ दिखेगा…",
            "Canlıya dön" to
                "लाइव पर वापस जाएँ",
            "Okunan yere dön" to
                "मौजूदा जगह पर वापस जाएँ",
            "Buradan dinle" to
                "यहाँ से सुनें",
            "Çevir" to
                "अनुवाद करें",
            "Google Çeviri'de aç" to
                "Google Translate में खोलें",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "टेक्स्ट लंबा है: क्लिपबोर्ड पर कॉपी किया गया, Google Translate में पेस्ट करें",
            "Google Çeviri'de aç ↗" to
                "Google Translate में खोलें ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "टेक्स्ट Google को भेजा जाता है · इंटरनेट ज़रूरी",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "ऊपर का अनुवाद आपके डिवाइस पर होता है; हर भाषा पैक सिर्फ़ एक बार डाउनलोड होता है।",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ एक हिस्सा टेक्स्ट में नहीं बदला जा सका",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ {0} हिस्से टेक्स्ट में नहीं बदले जा सके",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ एक हिस्सा पूरी तरह सत्यापित नहीं हो सका",
            "⚠ {0} bölümde sorun var" to
                "⚠ {0} हिस्सों में समस्या है",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "कनेक्शन या सेवा की गड़बड़ी के कारण यह हिस्सा टेक्स्ट में नहीं है।",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "इस हिस्से की एक लाइन दोबारा शुरू हुई लगती है पर पक्की नहीं हुई; टेक्स्ट अधूरा हो सकता है।",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "कुछ हिस्से टेक्स्ट में नहीं बदले जा सके और कुछ पक्के नहीं हुए; टेक्स्ट अधूरा हो सकता है।",
            "Bu bölümü yeniden dök" to
                "यह हिस्सा दोबारा ट्रांसक्राइब करें",
            "Diğer" to
                "और",
            "Metni düzenle" to
                "टेक्स्ट बदलें",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "अनुवाद फ़ाइल के रूप में शेयर करें (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "टेक्स्ट फ़ाइल के रूप में शेयर करें (.txt)",
            "Çeviri henüz hazır değil" to
                "अनुवाद अभी तैयार नहीं है",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "फ़ाइल नहीं बन सकी (स्टोरेज भरा हो सकता है)",
            "Çeviri altyazısı (.srt)" to
                "अनुवाद के सबटाइटल (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "सबटाइटल (.srt) · मूल ट्रांसक्रिप्ट",
            "Altyazı (.srt)" to
                "सबटाइटल (.srt)",
            "✓ Yazıya döküldü" to
                "✓ टेक्स्ट में बदला गया",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} से · कुछ हिस्से टेक्स्ट में नहीं बदले जा सके",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ {0} सेकंड में टेक्स्ट में बदला गया",
            "Geri" to
                "वापस",
            "Notlarım" to
                "मेरे नोट",
            "Notlarda ara…" to
                "नोट में खोजें…",
            "Aramayı temizle" to
                "खोज मिटाएँ",
            "Henüz not yok." to
                "अभी कोई नोट नहीं है।",
            "\"{0}\" hiçbir notta geçmiyor." to
                "किसी नोट में \"{0}\" नहीं है।",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "{0}/{1} नोट। सीमा पूरी होने पर नया नोट आते ही सबसे पुराना हट जाता है; ज़रूरी नोट एक्सपोर्ट करें।",
            "Notu aç" to
                "नोट खोलें",
            "Notu sil" to
                "नोट हटाएँ",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — बोली को टेक्स्ट में बदलने की सेवा, Wit.ai सेवा की शर्तें\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit अनुवाद — Google APIs सेवा की शर्तें\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK, User Messaging Platform — Google सेवा की शर्तें\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX, Jetpack Compose, Kotlin — Apache लाइसेंस 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "सूचना की अनुमति नहीं मिली; पूरा होने पर हम आपको नहीं बता पाएँगे",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "टेक्स्ट का आकार, लाइनों की दूरी और ऑडियो के साथ चलना: नोट स्क्रीन का \"Aa\" बटन।",
            "Transkript" to
                "ट्रांसक्रिप्ट",
            "Varsayılan konuşma dili" to
                "बोली की डिफ़ॉल्ट भाषा",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ ऑडियो कुछ ही सेकंड में टेक्स्ट में बदलता है (इंटरनेट ज़रूरी)। WhatsApp से शेयर किया गया ऑडियो इसी भाषा में बदला जाता है; भाषा अलग हो तो नोट के मेन्यू से बदलें।",
            "Varsayılan çeviri dili" to
                "अनुवाद की डिफ़ॉल्ट भाषा",
            "İndirilen dil paketleri" to
                "डाउनलोड किए गए भाषा पैक",
            "Yükleniyor…" to
                "लोड हो रहा है…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "अभी कोई भाषा पैक नहीं है। पहले अनुवाद पर एक पैक अपने-आप डाउनलोड होता है (~30 MB)।",
            "Sil" to
                "हटाएँ",
            "{0} paketi silindi" to
                "{0} पैक हटाया गया",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "अनुवाद आपके फ़ोन पर होता है; आपका टेक्स्ट किसी सर्वर को नहीं भेजा जाता।",
            "Bildirimler" to
                "सूचनाएँ",
            "İşlem bitince haber ver" to
                "पूरा होने पर मुझे बताएँ",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "ऐप से बाहर जाने पर, लंबा ट्रांसक्रिप्शन पूरा होते ही आपको सूचना मिलती है।",
            "Gizlilik" to
                "निजता",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "ऑडियो को टेक्स्ट में बदलने के लिए Meta की Wit.ai सेवा को भेजा जाता है। Meta इस डेटा को अपनी शर्तों के तहत प्रोसेस करता है; Wit.ai की नीति के अनुसार आवाज़ का डेटा 90 दिन तक रखा जाता है और बोली की पहचान सुधारने के लिए उसका विश्लेषण हो सकता है। हमारा कोई सर्वर नहीं है; आपके नोट और ऑडियो की अस्थायी कॉपी सिर्फ़ आपके फ़ोन पर रहती है। अनुवाद डिवाइस पर होता है। विज्ञापन Google AdMob दिखाता है। सिर्फ़ वही ऑडियो शेयर करें जिसे टेक्स्ट में बदलने का आपको अधिकार है।",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "निजता नीति",
            "Tüm notları sil ({0})" to
                "सभी नोट हटाएँ ({0})",
            "Reklam tercihleri" to
                "विज्ञापन की पसंद",
            "Hakkında" to
                "ऐप के बारे में",
            "Sürüm" to
                "वर्शन",
            "Geliştirici araçları açıldı" to
                "डेवलपर टूल चालू किए गए",
            "Geri bildirim gönder" to
                "सुझाव भेजें",
            " geri bildirim ({0})" to
                " सुझाव ({0})",
            "Açık kaynak lisansları" to
                "ओपन सोर्स लाइसेंस",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "यह ऐप WhatsApp या Meta ने नहीं बनाया है, न ही उनसे समर्थित या प्रायोजित है। WhatsApp, WhatsApp LLC का ट्रेडमार्क है। ट्रांसक्रिप्शन Meta की Wit.ai सेवा से होता है।",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "ऐप को रेट करें",
            "Tüm notlar silinsin mi?" to
                "सभी नोट हटाएँ?",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "इस फ़ोन के सभी नोट, एक्सपोर्ट की गई फ़ाइलें और माप के लॉग हटा दिए जाएँगे। चल रहा ट्रांसक्रिप्शन रोक दिया जाएगा। इसे वापस नहीं किया जा सकता।",
            "Kapat" to
                "बंद करें",
            "Sesli mesaja uzun bas" to
                "वॉइस मैसेज को दबाकर रखें",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "WhatsApp चैट में, जब तक मैसेज चुन न लिया जाए",
            "Paylaş simgesine dokun" to
                "शेयर आइकन पर टैप करें",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "ऊपर की पट्टी में (कुछ फ़ोन में ⋮ मेन्यू में)",
            "Listeden {0} uygulamasını seç" to
                "सूची से {0} चुनें",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "टेक्स्ट तुरंत तैयार होता है। अगली बार ऐप सूची में ऊपर दिखेगा।",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "{0} ऑडियो टेक्स्ट में बदले गए; सभी आपके नोट में हैं",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "{0} ऑडियो टेक्स्ट में बदले गए, {1} नहीं बदल सके",
            "{0} ile yazıya döküldü" to
                "{0} से टेक्स्ट में बदला गया",
            "Ses hazırlanıyor… ({0}/{1})" to
                "ऑडियो तैयार हो रहा है… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "क्या बोली किसी और भाषा में है? एक टैप में आज़माएँ:",
            "Paylaşırken imza ekle" to
                "शेयर करते समय हस्ताक्षर जोड़ें",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "आपके शेयर किए टेक्स्ट के नीचे ऐप का नाम और लिंक जुड़ता है।",
            "Reklamsız kullanım" to
                "बिना विज्ञापन इस्तेमाल",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "विज्ञापन बंद हैं। बचा समय: लगभग {0} घंटे।",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "एक छोटा विज्ञापन देखें और 24 घंटे तक कोई विज्ञापन न देखें।",
            "Reklam izle · 24 saat reklamsız" to
                "विज्ञापन देखें · 24 घंटे बिना विज्ञापन",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "24 घंटे तक कोई विज्ञापन नहीं। धन्यवाद!",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "इनाम पाने के लिए विज्ञापन अंत तक देखें।",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "विज्ञापन अभी लोड नहीं हो सका। थोड़ी देर बाद फिर कोशिश करें।",
        )
    }

    val ID: Map<String, String> by lazy {
        mapOf(
            "Ses hazırlanıyor…" to
                "Menyiapkan audio…",
            "Dosya çok büyük (en fazla {0} MB)" to
                "File terlalu besar (maks. {0} MB)",
            "Ses çok kısa (en az yarım saniye olmalı)" to
                "Audio terlalu pendek (minimal setengah detik)",
            "Ses açılamadı" to
                "Tidak dapat membuka audio",
            "Dosya bu telefon için çok büyük" to
                "File ini terlalu besar untuk ponsel ini",
            "Ses açılamadı: {0}" to
                "Tidak dapat membuka audio: {0}",
            "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
                "Audio catatan ini sudah tidak ada; bagikan audionya lagi",
            "Hazırlanıyor…" to
                "Menyiapkan…",
            "Yazıya dökülemedi: {0}" to
                "Tidak dapat mentranskripsi: {0}",
            "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
                "Koneksi internet lemah atau tidak ada. Periksa koneksi lalu coba lagi.",
            "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
                "Layanan sedang sibuk. Coba lagi dalam beberapa detik.",
            "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
                "Layanan transkripsi untuk {0} tidak dapat dijangkau saat ini. Perbarui aplikasi lalu coba lagi.",
            "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
                "Layanan transkripsi tidak merespons. Coba lagi sebentar lagi.",
            "Yazıya dökülemedi. Tekrar dene." to
                "Tidak dapat mentranskripsi. Coba lagi.",
            "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
                "Perlu koneksi internet. Sambungkan lalu coba lagi.",
            "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
                "Transkripsi belum tersedia untuk {0} saat ini. Ubah bahasa ucapan.",
            "[⚠ {0}–{1} arası yazıya dökülemedi]" to
                "[⚠ {0}–{1} tidak dapat ditranskripsi]",
            "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
                "Tidak ada ucapan terdeteksi. Apakah bahasa ucapannya {0}? Jika bukan, ubah bahasa lalu coba lagi.",
            "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
                "Teks sudah siap tetapi tidak dapat disimpan (penyimpanan mungkin penuh)",
            "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
                "Beberapa bagian tidak dapat ditranskripsi; ditandai di catatan",
            "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
                "Satu bagian tidak dapat diverifikasi sepenuhnya; ditandai di bawah catatan",
            "Düzenlenmiş notta bölüm yeniden dökülemez" to
                "Bagian dari catatan yang sudah diedit tidak dapat ditranskripsi ulang",
            "Önce mevcut işlem bitsin" to
                "Tunggu tugas saat ini selesai",
            "Bu dil için hızlı mod yok" to
                "Transkripsi tidak tersedia untuk bahasa ini",
            "Bunun için internet bağlantısı gerekiyor" to
                "Ini memerlukan koneksi internet",
            "Bölüm yeniden dökülüyor ({0}–{1})…" to
                "Mentranskripsi ulang bagian ({0}–{1})…",
            "Bölüm yeniden dökülemedi; mevcut metin korundu" to
                "Tidak dapat mentranskripsi ulang bagian; teks yang ada dipertahankan",
            "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
                "Sudah ditranskripsi ulang, tetapi bagian itu masih belum terverifikasi sepenuhnya",
            "Bölüm yeniden döküldü" to
                "Bagian ditranskripsi ulang",
            "Geliştirici modu kapatıldı" to
                "Mode pengembang dimatikan",
            "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
                "Teks kosong tidak dapat disimpan. Gunakan menu untuk menghapus catatan.",
            "Bu not silinmiş" to
                "Catatan ini sudah dihapus",
            "Kaydedildi" to
                "Tersimpan",
            "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
                "Tidak dapat menyimpan (penyimpanan mungkin penuh). Draf Anda masih terbuka.",
            "Önce mevcut işlem bitsin ya da iptal et" to
                "Tunggu tugas saat ini selesai, atau batalkan",
            "Not silinemedi (depolama hatası)" to
                "Tidak dapat menghapus catatan (kesalahan penyimpanan)",
            "Geri alınamadı (depolama hatası)" to
                "Tidak dapat mengurungkan (kesalahan penyimpanan)",
            "Notlar silinemedi (depolama hatası)" to
                "Tidak dapat menghapus catatan (kesalahan penyimpanan)",
            "Uygulamadaki tüm notlar ve dosyalar silindi" to
                "Semua catatan dan file di aplikasi telah dihapus",
            "Kaynak dil tanınmadı" to
                "Bahasa sumber tidak dikenali",
            "{0} için çeviri desteklenmiyor" to
                "Terjemahan tidak didukung untuk {0}",
            "Çeviri yapılamadı: {0}" to
                "Tidak dapat menerjemahkan: {0}",
            "internet bağlantısını kontrol et" to
                "periksa koneksi internet Anda",
            "Ses çalınamadı" to
                "Tidak dapat memutar audio",
            "Dosya boş" to
                "File kosong",
            "Döküm tamamlandı" to
                "Transkripsi selesai",
            "Metnin hazır" to
                "Teks Anda sudah siap",
            "Açmak için dokun" to
                "Ketuk untuk membuka",
            "{0} ile yazıya dök" to
                "Transkripsi dengan {0}",
            "Reklam" to
                "Iklan",
            "Dosya açılamadı" to
                "Tidak dapat membuka file",
            "Bu dosya okunamadı (desteklenmeyen biçim)" to
                "File ini tidak dapat dibaca (format tidak didukung)",
            "Bu dosyada ses bulunamadı" to
                "Tidak ada audio di file ini",
            "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
                "Untuk saat ini, audio hingga 30 menit didukung",
            "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
                "Format audio ini ({0}) tidak didukung di ponsel Anda",
            "Ses çözülemedi (çözücü yanıt vermiyor)" to
                "Tidak dapat mendekode audio (dekoder tidak merespons)",
            "Metin dosyasını paylaş" to
                "Bagikan file teks",
            "Altyazı dosyasını paylaş" to
                "Bagikan file subtitle",
            "{0} çeviri için desteklenmiyor" to
                "{0} tidak didukung untuk terjemahan",
            "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
                "Mengunduh paket bahasa (sekali saja, ~30 MB)…",
            "Çevriliyor {0}/{1}…" to
                "Menerjemahkan {0}/{1}…",
            "Paylaş" to
                "Bagikan",
            "WhatsApp bulunamadı, dosyadan seçebilirsin" to
                "WhatsApp tidak ditemukan; Anda bisa memilih file",
            "Ana sayfaya dön" to
                "Kembali ke beranda",
            "Ayarlar" to
                "Setelan",
            "Dinleyemiyorsan,\noku." to
                "Tidak bisa mendengarkan?\nBaca saja.",
            "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
                "Di WhatsApp, tekan lama pesan suara → Bagikan → {0}. Teks siap dalam hitungan detik.",
            "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
                "Bagikan pesan suara ke {0} dan baca dalam hitungan detik.",
            "WhatsApp'ı aç" to
                "Buka WhatsApp",
            "Ses ya da video dosyası seç" to
                "Pilih file audio atau video",
            "Ses / video dosyası seç" to
                "Pilih file audio / video",
            "⚡ Birazdan yazıya dökülüyor" to
                "⚡ Transkripsi segera dimulai",
            "Yazıya dökülüyor…" to
                "Mentranskripsi…",
            "⚡ Saniyeler içinde · {0}" to
                "⚡ Dalam hitungan detik · {0}",
            "Bir sorun oldu" to
                "Terjadi masalah",
            "İptal" to
                "Batal",
            "Tekrar dene" to
                "Coba lagi",
            "Konuşma dili: {0} ▾" to
                "Bahasa ucapan: {0} ▾",
            "Notlarda ara" to
                "Cari di catatan",
            "Silindi: {0}" to
                "Dihapus: {0}",
            "Geri al" to
                "Urungkan",
            "Son notlar" to
                "Catatan terbaru",
            "Tümü ({0})" to
                "Semua ({0})",
            "Silmek için nota basılı tut" to
                "Tekan lama catatan untuk menghapusnya",
            "Kılavuzu kapat" to
                "Sembunyikan panduan",
            "Kılavuzu aç" to
                "Tampilkan panduan",
            "Nasıl kullanılır?" to
                "Cara pakai",
            "Döküm bitince düzenleyebilirsin" to
                "Anda bisa mengedit setelah transkripsi selesai",
            "Not metni düzenleme alanı" to
                "Area edit teks catatan",
            " · düzenlendi" to
                " · diedit",
            " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
                " · tampilan berstempel waktu tersedia untuk teks yang belum diedit",
            "Vazgeç" to
                "Batal",
            "Kaydet" to
                "Simpan",
            "Kopyala" to
                "Salin",
            "Kopyalandı" to
                "Disalin",
            "Değişiklikler kaydedilmedi" to
                "Perubahan belum disimpan",
            "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
                "Jika keluar tanpa menyimpan, perubahan Anda akan hilang.",
            "Kaydetme" to
                "Jangan simpan",
            "{0} · {1} · {2} kelime" to
                "{0} · {1} · {2} kata",
            "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
                "Setelan baca (ukuran teks, jarak baris, tema)",
            "Diğer seçenekler" to
                "Opsi lainnya",
            "⚡ Yeniden dök ({0})" to
                "⚡ Transkripsi ulang ({0})",
            "Dil: {0} ile yeniden dök" to
                "Transkripsi ulang dalam {0}",
            "Bu notu sil" to
                "Hapus catatan ini",
            "Okuma ayarları" to
                "Setelan baca",
            "Yazı boyutu" to
                "Ukuran teks",
            "Yazıyı küçült" to
                "Perkecil teks",
            "Örnek metin · {0}" to
                "Contoh teks · {0}",
            "Yazıyı büyüt" to
                "Perbesar teks",
            "Satır aralığı" to
                "Jarak baris",
            "Sıkı" to
                "Rapat",
            "Normal" to
                "Normal",
            "Geniş" to
                "Lebar",
            "Görünüm" to
                "Tampilan",
            "Paragraf" to
                "Paragraf",
            "Zaman damgalı" to
                "Berstempel waktu",
            "Sesle takip" to
                "Ikuti audio",
            "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
                "Saat audio diputar, kalimat yang sedang dibaca disorot dan layar mengikutinya. Menggulir secara manual menjeda fitur ini.",
            "Tema" to
                "Tema",
            "Sistem" to
                "Sistem",
            "Açık" to
                "Terang",
            "Koyu" to
                "Gelap",
            "Canlı · ⚡ yazıya dökülüyor" to
                "Langsung · ⚡ mentranskripsi",
            "Canlı · %{0}" to
                "Langsung · {0}%",
            "Sesi duraklat" to
                "Jeda audio",
            "Sesi oynat" to
                "Putar audio",
            "Ses dalgası; dokunduğun yere atlar" to
                "Bentuk gelombang; ketuk untuk melompat",
            "Oynatıcıyı daralt" to
                "Ciutkan pemutar",
            "Oynatıcıyı genişlet" to
                "Luaskan pemutar",
            "−5 sn" to
                "−5 dtk",
            "5 saniye geri" to
                "Mundur 5 detik",
            "+5 sn" to
                "+5 dtk",
            "5 saniye ileri" to
                "Maju 5 detik",
            "Metin" to
                "Teks",
            "Çeviri" to
                "Terjemahan",
            "Metin birazdan burada belirecek…" to
                "Teks akan segera muncul di sini…",
            "Canlıya dön" to
                "Kembali ke langsung",
            "Okunan yere dön" to
                "Kembali ke posisi saat ini",
            "Buradan dinle" to
                "Dengarkan dari sini",
            "Çevir" to
                "Terjemahkan",
            "Google Çeviri'de aç" to
                "Buka di Google Terjemahan",
            "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
                "Teks panjang: disalin ke papan klip, tempelkan ke Google Terjemahan",
            "Google Çeviri'de aç ↗" to
                "Buka di Google Terjemahan ↗",
            "Metin Google'a gönderilir · internet gerekir" to
                "Teks dikirim ke Google · perlu internet",
            "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
                "Terjemahan di atas berjalan di perangkat Anda; setiap paket bahasa hanya diunduh sekali.",
            "⚠ Bir bölüm yazıya dökülemedi" to
                "⚠ Satu bagian tidak dapat ditranskripsi",
            "⚠ {0} bölüm yazıya dökülemedi" to
                "⚠ {0} bagian tidak dapat ditranskripsi",
            "⚠ Bir bölüm tam doğrulanamadı" to
                "⚠ Satu bagian tidak dapat diverifikasi sepenuhnya",
            "⚠ {0} bölümde sorun var" to
                "⚠ {0} bagian bermasalah",
            "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
                "Rentang ini tidak ada di teks karena kesalahan koneksi atau layanan.",
            "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
                "Satu baris di rentang ini tampak dimulai ulang tetapi belum dikonfirmasi; teks mungkin kurang.",
            "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
                "Beberapa rentang tidak dapat ditranskripsi dan beberapa belum dikonfirmasi; teks mungkin kurang.",
            "Bu bölümü yeniden dök" to
                "Transkripsi ulang bagian ini",
            "Diğer" to
                "Lainnya",
            "Metni düzenle" to
                "Edit teks",
            "Çeviriyi dosya olarak paylaş (.txt)" to
                "Bagikan terjemahan sebagai file (.txt)",
            "Metin dosyası olarak paylaş (.txt)" to
                "Bagikan sebagai file teks (.txt)",
            "Çeviri henüz hazır değil" to
                "Terjemahan belum siap",
            "Dosya oluşturulamadı (depolama dolu olabilir)" to
                "Tidak dapat membuat file (penyimpanan mungkin penuh)",
            "Çeviri altyazısı (.srt)" to
                "Subtitle terjemahan (.srt)",
            "Altyazı (.srt) · orijinal döküm" to
                "Subtitle (.srt) · transkrip asli",
            "Altyazı (.srt)" to
                "Subtitle (.srt)",
            "✓ Yazıya döküldü" to
                "✓ Ditranskripsi",
            "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
                "✓ ⚡ {0} dtk · beberapa bagian tidak dapat ditranskripsi",
            "✓ ⚡ {0} sn'de yazıya döküldü" to
                "✓ ⚡ Ditranskripsi dalam {0} dtk",
            "Geri" to
                "Kembali",
            "Notlarım" to
                "Catatan saya",
            "Notlarda ara…" to
                "Cari di catatan…",
            "Aramayı temizle" to
                "Hapus pencarian",
            "Henüz not yok." to
                "Belum ada catatan.",
            "\"{0}\" hiçbir notta geçmiyor." to
                "Tidak ada catatan yang memuat \"{0}\".",
            "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
                "{0}/{1} catatan. Saat batas tercapai, catatan terlama dihapus ketika ada yang baru; ekspor catatan penting.",
            "Notu aç" to
                "Buka catatan",
            "Notu sil" to
                "Hapus catatan",
            "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
                "• Wit.ai (Meta) — layanan ucapan ke teks, Ketentuan Layanan Wit.ai\n",
            "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
                "• Google ML Kit Terjemahan — Persyaratan Layanan Google APIs\n",
            "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
                "• Google Mobile Ads SDK, User Messaging Platform — Persyaratan Layanan Google\n",
            "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
                "• AndroidX, Jetpack Compose, Kotlin — Lisensi Apache 2.0",
            "Bildirim izni verilmedi; işlem bitince haber verilemez" to
                "Izin notifikasi ditolak; kami tidak bisa memberi tahu saat selesai",
            "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
                "Ukuran teks, jarak baris, dan ikuti audio: tombol \"Aa\" di layar catatan.",
            "Transkript" to
                "Transkrip",
            "Varsayılan konuşma dili" to
                "Bahasa ucapan default",
            "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
                "⚡ Audio ditranskripsi dalam hitungan detik (perlu internet). Audio yang dibagikan dari WhatsApp ditranskripsi dalam bahasa ini; jika berbeda, ubah bahasa dari menu catatan.",
            "Varsayılan çeviri dili" to
                "Bahasa terjemahan default",
            "İndirilen dil paketleri" to
                "Paket bahasa yang diunduh",
            "Yükleniyor…" to
                "Memuat…",
            "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
                "Belum ada paket bahasa. Paket diunduh otomatis saat terjemahan pertama (~30 MB).",
            "Sil" to
                "Hapus",
            "{0} paketi silindi" to
                "Paket {0} dihapus",
            "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
                "Terjemahan berjalan di ponsel Anda; teks Anda tidak dikirim ke server mana pun.",
            "Bildirimler" to
                "Notifikasi",
            "İşlem bitince haber ver" to
                "Beri tahu saya saat selesai",
            "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
                "Jika Anda keluar dari aplikasi, Anda mendapat notifikasi saat transkripsi panjang selesai.",
            "Gizlilik" to
                "Privasi",
            "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
                "Audio dikirim ke layanan Wit.ai milik Meta untuk ditranskripsi. Meta memproses data ini berdasarkan ketentuannya sendiri; menurut kebijakan Wit.ai, data suara disimpan hingga 90 hari dan dapat dianalisis untuk meningkatkan pengenalan ucapan. Kami tidak menjalankan server; catatan Anda dan salinan sementara audio hanya ada di ponsel Anda. Terjemahan berjalan di perangkat. Iklan ditampilkan oleh Google AdMob. Bagikan hanya audio yang berhak Anda transkripsi.",
            "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
                "",
            "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
                "",
            "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
                "",
            "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
                "",
            "Gizlilik politikası" to
                "Kebijakan privasi",
            "Tüm notları sil ({0})" to
                "Hapus semua catatan ({0})",
            "Reklam tercihleri" to
                "Preferensi iklan",
            "Hakkında" to
                "Tentang",
            "Sürüm" to
                "Versi",
            "Geliştirici araçları açıldı" to
                "Alat pengembang diaktifkan",
            "Geri bildirim gönder" to
                "Kirim masukan",
            " geri bildirim ({0})" to
                " masukan ({0})",
            "Açık kaynak lisansları" to
                "Lisensi sumber terbuka",
            "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
                "Aplikasi ini tidak dikembangkan, didukung, atau disponsori oleh WhatsApp maupun Meta. WhatsApp adalah merek dagang WhatsApp LLC. Transkripsi menggunakan layanan Wit.ai milik Meta.",
            "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
                "",
            "Uygulamayı puanla" to
                "Beri nilai aplikasi",
            "Tüm notlar silinsin mi?" to
                "Hapus semua catatan?",
            "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
                "Semua catatan, file yang diekspor, dan log pengukuran di ponsel ini akan dihapus. Transkripsi yang sedang berjalan dihentikan. Tindakan ini tidak dapat diurungkan.",
            "Kapat" to
                "Tutup",
            "Sesli mesaja uzun bas" to
                "Tekan lama pesan suara",
            "WhatsApp sohbetinde, mesaj seçilene kadar" to
                "Di chat WhatsApp, sampai pesan terpilih",
            "Paylaş simgesine dokun" to
                "Ketuk ikon Bagikan",
            "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
                "Di bilah atas (di beberapa ponsel, di menu ⋮)",
            "Listeden {0} uygulamasını seç" to
                "Pilih {0} dari daftar",
            "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
                "Teks langsung disiapkan. Lain kali aplikasi muncul di bagian atas daftar.",
            "{0} ses yazıya döküldü; hepsi notlarında" to
                "{0} audio ditranskripsi; semuanya ada di catatan Anda",
            "{0} ses yazıya döküldü, {1} ses dökülemedi" to
                "{0} audio ditranskripsi, {1} gagal",
            "{0} ile yazıya döküldü" to
                "Ditranskripsi dengan {0}",
            "Ses hazırlanıyor… ({0}/{1})" to
                "Menyiapkan audio… ({0}/{1})",
            "Konuşma başka dilde mi? Tek dokunuşla dene:" to
                "Ucapannya dalam bahasa lain? Coba dengan sekali ketuk:",
            "Paylaşırken imza ekle" to
                "Tambahkan tanda tangan saat berbagi",
            "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
                "Nama dan tautan aplikasi ditambahkan di bawah teks yang Anda bagikan.",
            "Reklamsız kullanım" to
                "Pakai tanpa iklan",
            "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
                "Iklan dimatikan. Sisa waktu: sekitar {0} jam.",
            "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
                "Tonton satu iklan singkat dan bebas iklan selama 24 jam.",
            "Reklam izle · 24 saat reklamsız" to
                "Tonton iklan · 24 jam tanpa iklan",
            "24 saat boyunca reklam yok. Teşekkürler!" to
                "Tanpa iklan selama 24 jam. Terima kasih!",
            "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
                "Tonton iklan sampai selesai untuk mendapatkan hadiah.",
            "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
                "Iklan tidak dapat dimuat saat ini. Coba lagi sebentar lagi.",
        )
    }

}
