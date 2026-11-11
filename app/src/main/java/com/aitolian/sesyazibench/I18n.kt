package com.aitolian.sesyazibench

import java.util.Locale

/*
 * Arayüz dili. Kaynak metinler kodda Türkçe durur; telefon dili Türkçe değilse o dilin
 * tablosundan (I18nTables), tablosu olmayan dillerde İngilizce tablodan gösterilir.
 * Tabloda olmayan metin İngilizceye, o da yoksa Türkçeye düşer (çökme yok). Yer tutucular: {0}, {1}, …
 */

/** Telefon diline göre metin. Örn. tx("Silindi: {0}", ad) */
fun tx(source: String, vararg args: Any?): String {
    val lang = Locale.getDefault().language
    var out = if (lang == "tr") source else (I18n.table(lang)[source] ?: I18n.EN[source] ?: source)
    args.forEachIndexed { i, a -> out = out.replace("{" + i + "}", a.toString()) }
    return out
}

internal object I18n {
    /** Tablosu olan diller (Türkçe kaynak dildir, İngilizce [EN]). */
    val LANGS = listOf("de", "fr", "es", "it", "pt", "ru", "ar", "hi", "id")

    fun table(lang: String): Map<String, String> = when (lang) {
        "de" -> I18nTables.DE
        "fr" -> I18nTables.FR
        "es" -> I18nTables.ES
        "it" -> I18nTables.IT
        "pt" -> I18nTables.PT
        "ru" -> I18nTables.RU
        "ar" -> I18nTables.AR
        "hi" -> I18nTables.HI
        "id", "in" -> I18nTables.ID // Endonezce: eski Java kodu "in"
        else -> EN
    }

    val EN: Map<String, String> = mapOf(
        "Ses hazırlanıyor…" to
            "Preparing audio…",
        "Dosya çok büyük (en fazla {0} MB)" to
            "File is too large (max {0} MB)",
        "Ses çok kısa (en az yarım saniye olmalı)" to
            "Audio is too short (at least half a second)",
        "Ses açılamadı" to
            "Couldn't open the audio",
        "Dosya bu telefon için çok büyük" to
            "This file is too large for this phone",
        "Ses açılamadı: {0}" to
            "Couldn't open the audio: {0}",
        "Bu notun sesi artık yok; sesi yeniden paylaşman gerekiyor" to
            "This note's audio is gone; share the audio again",
        "Hazırlanıyor…" to
            "Preparing…",
        "Yazıya dökülemedi: {0}" to
            "Couldn't transcribe: {0}",
        "İnternet bağlantısı zayıf ya da yok. Bağlantını kontrol edip tekrar dene." to
            "Weak or no internet connection. Check your connection and try again.",
        "Hizmet şu an yoğun. Birkaç saniye sonra tekrar dene." to
            "The service is busy right now. Try again in a few seconds.",
        "{0} için yazıya dökme hizmetine şu an ulaşılamıyor. Uygulamayı güncelleyip tekrar dene." to
            "The transcription service for {0} can't be reached right now. Update the app and try again.",
        "Yazıya dökme hizmeti şu an yanıt vermiyor. Biraz sonra tekrar dene." to
            "The transcription service isn't responding. Try again shortly.",
        "Yazıya dökülemedi. Tekrar dene." to
            "Couldn't transcribe. Try again.",
        "İnternet bağlantısı gerekiyor. Bağlanıp tekrar dene." to
            "An internet connection is required. Connect and try again.",
        "{0} için yazıya dökme şu an desteklenmiyor. Konuşma dilini değiştir." to
            "Transcription isn't available for {0} right now. Change the speech language.",
        "[⚠ {0}–{1} arası yazıya dökülemedi]" to
            "[⚠ {0}–{1} could not be transcribed]",
        "Konuşma algılanamadı. Konuşma dili {0} mi? Değilse dili değiştirip tekrar dene." to
            "No speech detected. Is the speech language {0}? If not, change the language and try again.",
        "Metin hazır ama telefona kaydedilemedi (depolama dolu olabilir)" to
            "Text is ready but couldn't be saved (storage may be full)",
        "Bazı bölümler yazıya dökülemedi; notta işaretlendi" to
            "Some parts couldn't be transcribed; they're marked in the note",
        "Bir bölüm tam doğrulanamadı; notun altında işaretlendi" to
            "One part couldn't be fully verified; it's marked below the note",
        "Düzenlenmiş notta bölüm yeniden dökülemez" to
            "Parts of an edited note can't be transcribed again",
        "Önce mevcut işlem bitsin" to
            "Wait for the current task to finish",
        "Bu dil için hızlı mod yok" to
            "Transcription isn't available for this language",
        "Bunun için internet bağlantısı gerekiyor" to
            "This needs an internet connection",
        "Bölüm yeniden dökülüyor ({0}–{1})…" to
            "Transcribing part again ({0}–{1})…",
        "Bölüm yeniden dökülemedi; mevcut metin korundu" to
            "Couldn't transcribe the part again; existing text kept",
        "Yeniden döküldü ama bölüm yine tam doğrulanamadı" to
            "Transcribed again, but the part still couldn't be fully verified",
        "Bölüm yeniden döküldü" to
            "Part transcribed again",
        "Geliştirici modu kapatıldı" to
            "Developer mode turned off",
        "Boş metin kaydedilemez. Notu silmek için menüyü kullan." to
            "Empty text can't be saved. Use the menu to delete the note.",
        "Bu not silinmiş" to
            "This note was deleted",
        "Kaydedildi" to
            "Saved",
        "Kaydedilemedi (depolama dolu olabilir). Taslağın açık." to
            "Couldn't save (storage may be full). Your draft is still open.",
        "Önce mevcut işlem bitsin ya da iptal et" to
            "Wait for the current task to finish, or cancel it",
        "Not silinemedi (depolama hatası)" to
            "Couldn't delete the note (storage error)",
        "Geri alınamadı (depolama hatası)" to
            "Couldn't undo (storage error)",
        "Notlar silinemedi (depolama hatası)" to
            "Couldn't delete notes (storage error)",
        "Uygulamadaki tüm notlar ve dosyalar silindi" to
            "All notes and files in the app were deleted",
        "Kaynak dil tanınmadı" to
            "Source language not recognized",
        "{0} için çeviri desteklenmiyor" to
            "Translation isn't supported for {0}",
        "Çeviri yapılamadı: {0}" to
            "Couldn't translate: {0}",
        "internet bağlantısını kontrol et" to
            "check your internet connection",
        "Ses çalınamadı" to
            "Couldn't play the audio",
        "Dosya boş" to
            "The file is empty",
        "Döküm tamamlandı" to
            "Transcription finished",
        "Metnin hazır" to
            "Your text is ready",
        "Açmak için dokun" to
            "Tap to open",
        "{0} ile yazıya dök" to
            "Transcribe with {0}",
        "Reklam" to
            "Ad",
        "Dosya açılamadı" to
            "Couldn't open the file",
        "Bu dosya okunamadı (desteklenmeyen biçim)" to
            "This file couldn't be read (unsupported format)",
        "Bu dosyada ses bulunamadı" to
            "No audio found in this file",
        "Şimdilik en fazla 30 dakikalık ses destekleniyor" to
            "Audio up to 30 minutes is supported for now",
        "Bu ses biçimi ({0}) telefonunda desteklenmiyor" to
            "This audio format ({0}) isn't supported on your phone",
        "Ses çözülemedi (çözücü yanıt vermiyor)" to
            "Couldn't decode the audio (decoder not responding)",
        "Metin dosyasını paylaş" to
            "Share text file",
        "Altyazı dosyasını paylaş" to
            "Share subtitle file",
        "{0} çeviri için desteklenmiyor" to
            "{0} isn't supported for translation",
        "Dil paketi indiriliyor (tek seferlik, ~30 MB)…" to
            "Downloading language pack (one time, ~30 MB)…",
        "Çevriliyor {0}/{1}…" to
            "Translating {0}/{1}…",
        "Paylaş" to
            "Share",
        "WhatsApp bulunamadı, dosyadan seçebilirsin" to
            "WhatsApp not found; you can pick a file instead",
        "Ana sayfaya dön" to
            "Back to home",
        "Ayarlar" to
            "Settings",
        "Dinleyemiyorsan,\noku." to
            "Can't listen?\nRead it.",
        "WhatsApp'ta sesli mesaja uzun bas → Paylaş → {0}. Metin saniyeler içinde hazır." to
            "In WhatsApp, long-press a voice message → Share → {0}. The text is ready in seconds.",
        "Sesli mesajı {0} ile paylaş, saniyeler içinde oku." to
            "Share a voice message to {0} and read it in seconds.",
        "WhatsApp'ı aç" to
            "Open WhatsApp",
        "Ses ya da video dosyası seç" to
            "Choose an audio or video file",
        "Ses / video dosyası seç" to
            "Choose audio / video file",
        "⚡ Birazdan yazıya dökülüyor" to
            "⚡ Transcribing in a moment",
        "Yazıya dökülüyor…" to
            "Transcribing…",
        "⚡ Saniyeler içinde · {0}" to
            "⚡ In seconds · {0}",
        "Bir sorun oldu" to
            "Something went wrong",
        "İptal" to
            "Cancel",
        "Tekrar dene" to
            "Try again",
        "Konuşma dili: {0} ▾" to
            "Speech language: {0} ▾",
        "Notlarda ara" to
            "Search notes",
        "Silindi: {0}" to
            "Deleted: {0}",
        "Geri al" to
            "Undo",
        "Son notlar" to
            "Recent notes",
        "Tümü ({0})" to
            "All ({0})",
        "Silmek için nota basılı tut" to
            "Long-press a note to delete it",
        "Kılavuzu kapat" to
            "Hide guide",
        "Kılavuzu aç" to
            "Show guide",
        "Nasıl kullanılır?" to
            "How to use",
        "Döküm bitince düzenleyebilirsin" to
            "You can edit once transcription finishes",
        "Not metni düzenleme alanı" to
            "Note text editor",
        " · düzenlendi" to
            " · edited",
        " · zamanlı görünüm düzenlenmemiş metinde kullanılabilir" to
            " · timestamped view is available for unedited text",
        "Vazgeç" to
            "Cancel",
        "Kaydet" to
            "Save",
        "Kopyala" to
            "Copy",
        "Kopyalandı" to
            "Copied",
        "Değişiklikler kaydedilmedi" to
            "Unsaved changes",
        "Düzenlemeyi kaydetmeden çıkarsan yaptığın değişiklikler kaybolur." to
            "If you leave without saving, your changes will be lost.",
        "Kaydetme" to
            "Don't save",
        "{0} · {1} · {2} kelime" to
            "{0} · {1} · {2} words",
        "Okuma ayarları (yazı boyutu, satır aralığı, tema)" to
            "Reading settings (text size, line spacing, theme)",
        "Diğer seçenekler" to
            "More options",
        "⚡ Yeniden dök ({0})" to
            "⚡ Transcribe again ({0})",
        "Dil: {0} ile yeniden dök" to
            "Transcribe again in {0}",
        "Bu notu sil" to
            "Delete this note",
        "Okuma ayarları" to
            "Reading settings",
        "Yazı boyutu" to
            "Text size",
        "Yazıyı küçült" to
            "Smaller text",
        "Örnek metin · {0}" to
            "Sample text · {0}",
        "Yazıyı büyüt" to
            "Larger text",
        "Satır aralığı" to
            "Line spacing",
        "Sıkı" to
            "Tight",
        "Normal" to
            "Normal",
        "Geniş" to
            "Wide",
        "Görünüm" to
            "Display",
        "Paragraf" to
            "Paragraph",
        "Zaman damgalı" to
            "Timestamped",
        "Sesle takip" to
            "Follow audio",
        "Ses çalarken okunan cümle vurgulanır ve ekran onu izler. Elle kaydırınca takip durur." to
            "While audio plays, the current sentence is highlighted and the screen follows it. Scrolling by hand pauses following.",
        "Tema" to
            "Theme",
        "Sistem" to
            "System",
        "Açık" to
            "Light",
        "Koyu" to
            "Dark",
        "Canlı · ⚡ yazıya dökülüyor" to
            "Live · ⚡ transcribing",
        "Canlı · %{0}" to
            "Live · {0}%",
        "Sesi duraklat" to
            "Pause audio",
        "Sesi oynat" to
            "Play audio",
        "Ses dalgası; dokunduğun yere atlar" to
            "Waveform; tap to jump",
        "Oynatıcıyı daralt" to
            "Collapse player",
        "Oynatıcıyı genişlet" to
            "Expand player",
        "−5 sn" to
            "−5 s",
        "5 saniye geri" to
            "Back 5 seconds",
        "+5 sn" to
            "+5 s",
        "5 saniye ileri" to
            "Forward 5 seconds",
        "Metin" to
            "Text",
        "Çeviri" to
            "Translation",
        "Metin birazdan burada belirecek…" to
            "The text will appear here shortly…",
        "Canlıya dön" to
            "Back to live",
        "Okunan yere dön" to
            "Back to current position",
        "Buradan dinle" to
            "Listen from here",
        "Çevir" to
            "Translate",
        "Google Çeviri'de aç" to
            "Open in Google Translate",
        "Metin uzun: panoya kopyalandı, Google Çeviri'ye yapıştır" to
            "Long text: copied to clipboard, paste it into Google Translate",
        "Google Çeviri'de aç ↗" to
            "Open in Google Translate ↗",
        "Metin Google'a gönderilir · internet gerekir" to
            "Text is sent to Google · internet required",
        "Yukarıdaki çeviri cihazda yapılır; her dil paketi yalnızca ilk seferde indirilir." to
            "The translation above runs on your device; each language pack is downloaded only once.",
        "⚠ Bir bölüm yazıya dökülemedi" to
            "⚠ One part couldn't be transcribed",
        "⚠ {0} bölüm yazıya dökülemedi" to
            "⚠ {0} parts couldn't be transcribed",
        "⚠ Bir bölüm tam doğrulanamadı" to
            "⚠ One part couldn't be fully verified",
        "⚠ {0} bölümde sorun var" to
            "⚠ {0} parts have issues",
        "Bağlantı ya da hizmet hatası nedeniyle bu aralık metinde yok." to
            "This range is missing from the text because of a connection or service error.",
        "Bu aralıkta bir satır yeniden başlamış görünüyor ama kesinleşmedi; metin eksik olabilir." to
            "A line in this range seems to have restarted but wasn't confirmed; text may be missing.",
        "Bazı aralıklar yazıya dökülemedi, bazıları kesinleşmedi; metin eksik olabilir." to
            "Some ranges couldn't be transcribed and some weren't confirmed; text may be missing.",
        "Bu bölümü yeniden dök" to
            "Transcribe this part again",
        "Diğer" to
            "More",
        "Metni düzenle" to
            "Edit text",
        "Çeviriyi dosya olarak paylaş (.txt)" to
            "Share translation as a file (.txt)",
        "Metin dosyası olarak paylaş (.txt)" to
            "Share as a text file (.txt)",
        "Çeviri henüz hazır değil" to
            "Translation isn't ready yet",
        "Dosya oluşturulamadı (depolama dolu olabilir)" to
            "Couldn't create the file (storage may be full)",
        "Çeviri altyazısı (.srt)" to
            "Translation subtitles (.srt)",
        "Altyazı (.srt) · orijinal döküm" to
            "Subtitles (.srt) · original transcript",
        "Altyazı (.srt)" to
            "Subtitles (.srt)",
        "✓ Yazıya döküldü" to
            "✓ Transcribed",
        "✓ ⚡ {0} sn · bazı bölümler yazıya dökülemedi" to
            "✓ ⚡ {0} s · some parts couldn't be transcribed",
        "✓ ⚡ {0} sn'de yazıya döküldü" to
            "✓ ⚡ Transcribed in {0} s",
        "Geri" to
            "Back",
        "Notlarım" to
            "My notes",
        "Notlarda ara…" to
            "Search notes…",
        "Aramayı temizle" to
            "Clear search",
        "Henüz not yok." to
            "No notes yet.",
        "\"{0}\" hiçbir notta geçmiyor." to
            "No notes contain \"{0}\".",
        "{0}/{1} not. Sınır dolunca yeni not gelince en eskisi silinir; önemli notları dışa aktar." to
            "{0}/{1} notes. When the limit is reached, the oldest note is removed as new ones arrive; export important notes.",
        "Notu aç" to
            "Open note",
        "Notu sil" to
            "Delete note",
        "• Wit.ai (Meta) — konuşmayı yazıya dökme hizmeti, Wit.ai Hizmet Koşulları\n" to
            "• Wit.ai (Meta) — speech-to-text service, Wit.ai Terms of Service\n",
        "• Google ML Kit Çeviri — Google APIs Hizmet Şartları\n" to
            "• Google ML Kit Translation — Google APIs Terms of Service\n",
        "• Google Mobile Ads SDK, User Messaging Platform — Google Hizmet Şartları\n" to
            "• Google Mobile Ads SDK, User Messaging Platform — Google Terms of Service\n",
        "• AndroidX, Jetpack Compose, Kotlin — Apache Lisansı 2.0" to
            "• AndroidX, Jetpack Compose, Kotlin — Apache License 2.0",
        "Bildirim izni verilmedi; işlem bitince haber verilemez" to
            "Notification permission denied; can't notify you when it's done",
        "Yazı boyutu, satır aralığı ve sesle takip: not ekranındaki \"Aa\" düğmesi." to
            "Text size, line spacing and follow audio: the \"Aa\" button on the note screen.",
        "Transkript" to
            "Transcript",
        "Varsayılan konuşma dili" to
            "Default speech language",
        "⚡ Sesler saniyeler içinde yazıya dökülür (internet gerekir). WhatsApp'tan paylaştığın ses bu dilde dökülür; farklıysa not menüsünden dili değiştir." to
            "⚡ Audio is transcribed in seconds (internet required). Audio you share from WhatsApp is transcribed in this language; if it's different, change the language from the note menu.",
        "Varsayılan çeviri dili" to
            "Default translation language",
        "İndirilen dil paketleri" to
            "Downloaded language packs",
        "Yükleniyor…" to
            "Loading…",
        "Henüz dil paketi yok. İlk çeviride otomatik indirilir (~30 MB)." to
            "No language packs yet. One is downloaded automatically on first translation (~30 MB).",
        "Sil" to
            "Delete",
        "{0} paketi silindi" to
            "{0} pack deleted",
        "Çeviri telefonunda yapılır; metnin hiçbir sunucuya gönderilmez." to
            "Translation runs on your phone; your text isn't sent to any server.",
        "Bildirimler" to
            "Notifications",
        "İşlem bitince haber ver" to
            "Notify me when it's done",
        "Uygulamadan çıktığında uzun dökümler bitince bildirim gelir." to
            "If you leave the app, you get a notification when a long transcription finishes.",
        "Gizlilik" to
            "Privacy",
        "Ses, yazıya dökülmek için Meta'nın Wit.ai hizmetine gönderilir. Meta bu verileri kendi " to
            "Audio is sent to Meta's Wit.ai service to be transcribed. Meta processes this data under its own ",
        "koşulları kapsamında işler; Wit.ai politikasına göre ses verileri en fazla 90 gün saklanır ve " to
            "terms; under the Wit.ai policy, voice data is kept for up to 90 days and ",
        "konuşma tanımayı geliştirmek için analiz edilebilir. Bizim bir sunucumuz yok; notların ve sesin " to
            "may be analyzed to improve speech recognition. We don't run a server; your notes and the ",
        "geçici kopyası yalnızca telefonunda tutulur. Çeviri cihazda yapılır. Reklamlar Google AdMob " to
            "temporary copy of the audio stay only on your phone. Translation runs on the device. Ads are shown by Google ",
        "tarafından gösterilir. Yalnızca yazıya dökme hakkın olan sesleri paylaş." to
            "AdMob. Only share audio you have the right to transcribe.",
        "Gizlilik politikası" to
            "Privacy policy",
        "Tüm notları sil ({0})" to
            "Delete all notes ({0})",
        "Reklam tercihleri" to
            "Ad preferences",
        "Hakkında" to
            "About",
        "Sürüm" to
            "Version",
        "Geliştirici araçları açıldı" to
            "Developer tools enabled",
        "Geri bildirim gönder" to
            "Send feedback",
        " geri bildirim ({0})" to
            " feedback ({0})",
        "Açık kaynak lisansları" to
            "Open source licenses",
        "Bu uygulama WhatsApp veya Meta tarafından geliştirilmemiş, desteklenmemiş veya onaylanmamıştır. " to
            "This app is not developed, endorsed or sponsored by WhatsApp or Meta. ",
        "WhatsApp, WhatsApp LLC'nin ticari markasıdır. Yazıya dökme Meta'nın Wit.ai hizmetini kullanır." to
            "WhatsApp is a trademark of WhatsApp LLC. Transcription uses Meta's Wit.ai service.",
        "Uygulamayı puanla" to
            "Rate the app",
        "Tüm notlar silinsin mi?" to
            "Delete all notes?",
        "Telefondaki tüm notlar, dışa aktarılan dosyalar ve ölçüm kayıtları silinir. Çalışan döküm durdurulur. Bu işlem geri alınamaz." to
            "All notes, exported files and timing logs on this phone will be deleted. Any running transcription is stopped. This can't be undone.",
        "Kapat" to
            "Close",
        "Sesli mesaja uzun bas" to
            "Long-press the voice message",
        "WhatsApp sohbetinde, mesaj seçilene kadar" to
            "In the WhatsApp chat, until the message is selected",
        "Paylaş simgesine dokun" to
            "Tap the Share icon",
        "Üst çubukta (bazı telefonlarda ⋮ menüsünde)" to
            "In the top bar (on some phones, in the ⋮ menu)",
        "Listeden {0} uygulamasını seç" to
            "Choose {0} from the list",
        "Metin hemen hazırlanır. Bir sonraki sefer listede ilk sıralarda olur." to
            "The text is prepared right away. Next time it appears near the top of the list.",
        "{0} ses yazıya döküldü; hepsi notlarında" to
            "{0} audio files transcribed; all are in your notes",
        "{0} ses yazıya döküldü, {1} ses dökülemedi" to
            "{0} audio files transcribed, {1} failed",
        "{0} ile yazıya döküldü" to
            "Transcribed with {0}",
        "Ses hazırlanıyor… ({0}/{1})" to
            "Preparing audio… ({0}/{1})",
        "Konuşma başka dilde mi? Tek dokunuşla dene:" to
            "Is the speech in another language? Try with one tap:",
        "Paylaşırken imza ekle" to
            "Add a signature when sharing",
        "Paylaştığın metnin altına uygulamanın adı ve bağlantısı eklenir." to
            "The app's name and link are added below the text you share.",
        "Reklamsız kullanım" to
            "Ad-free use",
        "Reklamlar kapalı. Kalan süre: yaklaşık {0} saat." to
            "Ads are off. Time left: about {0} hours.",
        "Kısa bir reklam izle, 24 saat boyunca hiç reklam görme." to
            "Watch one short ad and see no ads for 24 hours.",
        "Reklam izle · 24 saat reklamsız" to
            "Watch an ad · 24 hours ad-free",
        "24 saat boyunca reklam yok. Teşekkürler!" to
            "No ads for 24 hours. Thank you!",
        "Ödül için reklamı sonuna kadar izlemen gerekiyor." to
            "Watch the ad to the end to get the reward.",
        "Reklam şu an yüklenemedi. Biraz sonra tekrar dene." to
            "The ad couldn't load right now. Try again shortly.",
    )
}
