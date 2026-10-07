import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Firebase (Crashlytics/Analytics) is wired in the original app. To re-enable:
    //   1. drop a real google-services.json into app/
    //   2. uncomment the two plugins below and the firebase deps further down
    // alias(libs.plugins.google.services)
    // alias(libs.plugins.firebase.crashlytics)
}

// Optional release signing. If a keystore.properties exists at the repo root we
// sign with that; otherwise the release build falls back to the debug keystore so
// `assembleRelease` ALWAYS produces an installable, sideloadable APK on any machine.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.streamdev.aiostreamer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.streamdev.aiostreamer"
        // minSdk 24 matches the clean v6.7.1 build (Android 7.0+). targetSdk 35 is the
        // current *stable* platform (Android 15); the shipped v6.7.1 targeted a 37
        // preview, which needs a preview SDK + newer AGP, so the revival pins to 35.
        minSdk = 24
        targetSdk = 35
        versionCode = 6719
        versionName = "6.7.1-alpha01"
        vectorDrawables.useSupportLibrary = true
        // Keep every resource configuration in the single universal APK (do not
        // strip locales/densities) so one file installs correctly on any device.
    }

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (keystorePropsFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    // No native code ships in this app, so it is ABI-universal by construction.
    // Disable ABI/density splits explicitly to guarantee a single universal APK.
    splits {
        abi { isEnable = false }
        density { isEnable = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Keep a sideload/release build from being blocked by lint on CI or a dev box.
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/{AL2.0,LGPL2.1}",
            "META-INF/DEPENDENCIES",
            "META-INF/INDEX.LIST",
        )
        // Align native libs on 16 KB page boundaries for Android 15 devices, in
        // case a future dependency adds .so files.
        jniLibs { useLegacyPackaging = false }
    }
}

dependencies {
    // The platform-agnostic API baseline (see :baseline).
    implementation(project(":baseline"))

    // Kotlin / coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.documentfile)

    // Jetpack Compose (Material3)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)

    // Image loading
    implementation(libs.coil.compose)

    // Secure lock (PIN / biometric)
    implementation(libs.androidx.biometric)

    // In-app browser / premium-site login WebViews
    implementation(libs.androidx.browser)
    implementation(libs.androidx.webkit)

    // Android TV (leanback)
    implementation(libs.androidx.leanback)

    // Media playback (ExoPlayer2) + Chromecast
    implementation(libs.androidx.media)
    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.ui)
    implementation(libs.exoplayer.hls)
    implementation(libs.exoplayer.dash)
    implementation(libs.exoplayer.cast)
    implementation(libs.play.services.cast.framework)

    // Ads
    implementation(libs.play.services.ads)

    // Firebase (see plugin note above)
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.analytics)
    // implementation(libs.firebase.crashlytics)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    testImplementation(libs.junit)
}
