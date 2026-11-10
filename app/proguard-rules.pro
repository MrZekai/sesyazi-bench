# MuteRead R8 kuralları.
# Kütüphaneler (AdMob, UMP, ML Kit, Play Review, Compose) kendi kurallarını getirir;
# uygulama kodu yansıma (reflection) kullanmaz, bu yüzden ek "keep" gerekmez.

# Çökme raporlarında satır numaraları okunabilsin (Play Console eşleme dosyasıyla çözer)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
