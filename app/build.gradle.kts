import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.aitolian.sesyazibench"
    compileSdk = 36
    ndkVersion = "27.2.12479018"

    defaultConfig {
        // Mağaza kimliği: Play'de yayınlandıktan sonra DEĞİŞTİRİLEMEZ. Kod paketi (namespace) ayrı; kullanıcı görmez.
        applicationId = "com.aitolian.muteread"
        minSdk = 26
        targetSdk = 36
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "0.1.${System.getenv("GITHUB_RUN_NUMBER") ?: "0"}"

        // Hızlı mod (Wit.ai) dil → anahtar eşlemesi: GitHub secret WIT_TOKENS (JSON).
        // Yoksa boş; uygulama yalnızca telefonda (Whisper) çalışır.
        val witTokens = (System.getenv("WIT_TOKENS") ?: project.findProperty("witTokens") as String? ?: "{}").replace("\r", " ").replace("\n", " ").trim()
        buildConfigField("String", "WIT_TOKENS", "\"" + witTokens.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")

        ndk {
            // Benchmark için yalnızca gerçek telefon mimarisi
            abiFilters += listOf("arm64-v8a")
        }
        externalNativeBuild {
            cmake {
                // 16 KB sayfa boyutu (NDK r27): tüm .so'lar (whisper/ggml varyantları dahil) 16 KB hizalı bağlanır.
                // CI, üretilen APK'daki her .so'nun LOAD hizalamasını ayrıca denetler.
                arguments += listOf("-DCMAKE_BUILD_TYPE=Release", "-DANDROID_STL=c++_static", "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
            }
        }
    }

    // Yükleme (upload) anahtarı: CI'da GitHub secret'lardan gelir; yoksa debug anahtarı
    // (o zaman çıktı Play'e yüklenemez, yalnız test içindir).
    val uploadStore = System.getenv("UPLOAD_KEYSTORE")?.let { file(it) }?.takeIf { it.exists() }
    signingConfigs {
        if (uploadStore != null) create("upload") {
            storeFile = uploadStore
            storePassword = System.getenv("UPLOAD_STORE_PASSWORD")
            keyAlias = System.getenv("UPLOAD_KEY_ALIAS")
            keyPassword = System.getenv("UPLOAD_KEY_PASSWORD")
        }
    }

    // Reklam kimlikleri: debug HER ZAMAN Google test kimlikleri; release, secret varsa
    // gerçek kimlikler, yoksa yine test kimlikleri.
    val testApp = "ca-app-pub-3940256099942544~3347511713"
    val testBanner = "ca-app-pub-3940256099942544/9214589741"
    val testInterstitial = "ca-app-pub-3940256099942544/1033173712"
    val testNative = "ca-app-pub-3940256099942544/2247696110"
    fun env(name: String, fallback: String) = System.getenv(name)?.trim()?.takeIf { it.isNotEmpty() } ?: fallback
    fun q(v: String) = "\"" + v.replace("\\", "").replace("\"", "") + "\""

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = testApp
            buildConfigField("String", "ADMOB_BANNER", q(testBanner))
            buildConfigField("String", "ADMOB_INTERSTITIAL", q(testInterstitial))
            buildConfigField("String", "ADMOB_NATIVE", q(testNative))
            buildConfigField("String", "ADMOB_TEST_DEVICES", q(""))
        }
        release {
            isMinifyEnabled = false
            signingConfig = if (uploadStore != null) signingConfigs.getByName("upload") else signingConfigs.getByName("debug")
            manifestPlaceholders["admobAppId"] = env("ADMOB_APP_ID", testApp)
            buildConfigField("String", "ADMOB_BANNER", q(env("ADMOB_BANNER_ID", testBanner)))
            buildConfigField("String", "ADMOB_INTERSTITIAL", q(env("ADMOB_INTERSTITIAL_ID", testInterstitial)))
            buildConfigField("String", "ADMOB_NATIVE", q(env("ADMOB_NATIVE_ID", testNative)))
            // Kendi test telefonlarında gerçek reklama tıklamamak için (virgülle ayrılmış cihaz kimlikleri)
            buildConfigField("String", "ADMOB_TEST_DEVICES", q(env("ADMOB_TEST_DEVICE_IDS", "")))
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // Birim testleri: Android çağrıları (SystemClock vb.) varsayılan değer döndürür
    testOptions { unitTests.isReturnDefaultValues = true }
    // ggml CPU varyantları dlopen ile yüklenir → .so dosyaları diske çıkarılmalı
    packaging { jniLibs { useLegacyPackaging = true } }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.06.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    // Tutarlı vektör ikon seti (yayın derlemesinde R8 kullanılmayanları atar)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    // Birim testleri (JVM): gerçek org.json (Android'deki taslak sürüm değil)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // ML Kit çeviri — cihaz içi, dil paketi ilk kullanımda indirilir
    implementation("com.google.mlkit:translate:17.0.3")

    // AdMob + UMP rıza formu (şimdilik yalnızca test reklam ID'leri)
    implementation("com.google.android.gms:play-services-ads:24.4.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

    // ML Kit GenAI Speech Recognition (alpha) — cihaz içi
    implementation("com.google.mlkit:genai-speech-recognition:1.0.0-alpha1")
}
