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
        applicationId = "com.aitolian.sesyazibench"
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
                arguments += listOf("-DCMAKE_BUILD_TYPE=Release", "-DANDROID_STL=c++_static")
            }
        }
    }

    buildTypes {
        release {
            // Benchmark derlemesi: native kod optimize, imza debug anahtarıyla (Play'e yüklenmez)
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // ML Kit çeviri — cihaz içi, dil paketi ilk kullanımda indirilir
    implementation("com.google.mlkit:translate:17.0.3")

    // AdMob + UMP rıza formu (şimdilik yalnızca test reklam ID'leri)
    implementation("com.google.android.gms:play-services-ads:24.4.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

    // ML Kit GenAI Speech Recognition (alpha) — cihaz içi
    implementation("com.google.mlkit:genai-speech-recognition:1.0.0-alpha1")
}
