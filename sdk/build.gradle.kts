import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun adSetting(name: String, fallback: String): String = providers.environmentVariable(name)
    .orElse(localProperties.getProperty(name, fallback))
    .get().trim().ifEmpty { fallback }

fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\")
    .replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\""

val testAppId = "ca-app-pub-3940256099942544~3347511713"
val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"
val testNativeId = "ca-app-pub-3940256099942544/2247696110"

android {
    namespace = "com.bbr.jeddyapps.sdk"
    compileSdk { version = release(37) }
    defaultConfig { minSdk = 24 }
    buildTypes {
        debug {
            buildConfigField("String", "ADMOB_APP_ID", quoted(testAppId))
            buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", quoted(testInterstitialId))
            buildConfigField("String", "ADMOB_NATIVE_AD_UNIT_ID", quoted(testNativeId))
        }
        release {
            buildConfigField("String", "ADMOB_APP_ID", quoted(adSetting("ADMOB_APP_ID", testAppId)))
            buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", quoted(adSetting("ADMOB_INTERSTITIAL_AD_UNIT_ID", testInterstitialId)))
            buildConfigField("String", "ADMOB_NATIVE_AD_UNIT_ID", quoted(adSetting("ADMOB_NATIVE_AD_UNIT_ID", testNativeId)))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.google.mobile.ads)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
