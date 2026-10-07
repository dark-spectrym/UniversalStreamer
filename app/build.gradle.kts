plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Firebase (Crashlytics/Analytics) is wired in the original app. To re-enable:
    //   1. drop a real google-services.json into app/
    //   2. uncomment the two plugins below and the firebase deps further down
    // alias(libs.plugins.google.services)
    // alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.streamdev.aiostreamer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.streamdev.aiostreamer"
        minSdk = 21
        targetSdk = 34
        versionCode = 645
        versionName = "6.4.5"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
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
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/{AL2.0,LGPL2.1}",
            "META-INF/DEPENDENCIES",
            "META-INF/INDEX.LIST",
        )
    }
}

dependencies {
    // The platform-agnostic API baseline (see :baseline).
    implementation(project(":baseline"))

    // Kotlin / coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    // AndroidX core UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.documentfile)

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

    // Image loading
    implementation(libs.glide)

    // Ads
    implementation(libs.play.services.ads)

    // Firebase (see plugin note above)
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.analytics)
    // implementation(libs.firebase.crashlytics)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    testImplementation(libs.junit)
}
