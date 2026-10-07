// Plugins are applied per-module (see app/build.gradle.kts and
// baseline/build.gradle.kts) rather than declared here with `apply false`.
// This keeps the Android Gradle Plugin off the classpath when only the
// platform-agnostic :baseline module is configured (SKIP_APP=1), so the
// networking/API baseline can be compiled and tested without an Android SDK
// or access to the Google Maven repository.
