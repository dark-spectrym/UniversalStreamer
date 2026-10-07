pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "UniversalStreamer"

// The platform-agnostic networking / API baseline. Pure Kotlin/JVM, no Android
// dependencies, so it can be compiled and unit-tested without an Android SDK.
include(":baseline")

// The Android application. Requires an Android SDK to assemble. Set SKIP_APP=1 in
// the environment to configure the build without it (used to verify :baseline in
// CI machines that have no SDK installed).
if (System.getenv("SKIP_APP") != "1") {
    include(":app")
}
