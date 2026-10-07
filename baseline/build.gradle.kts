import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.retrofit)
    api(libs.retrofit.converter.gson)
    api(libs.okhttp)
    implementation(libs.okhttp.logging)
    api(libs.gson)
    implementation(libs.coroutines.core)
    implementation(libs.jsoup)
    implementation(libs.bouncycastle)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.coroutines.core)
}

// Target JVM 17 bytecode (Android desugaring baseline) while compiling with
// whatever JDK (>=17) runs Gradle, rather than requiring a specific toolchain.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnit()
}
