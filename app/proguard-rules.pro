# Keep Gson DTOs (reconstructed from the original @Keep-annotated datatypes).
-keep class com.streamdev.aiostreamer.baseline.model.** { *; }

# Retrofit / OkHttp / Gson standard rules.
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature, *Annotation*, Exceptions, InnerClasses
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# jsoup pulls in optional modules the app does not use.
-dontwarn org.jsoup.**
