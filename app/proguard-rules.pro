# Prevent obfuscation of models and JSON serialization
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-dontwarn okio.**
-dontwarn okhttp3.**
