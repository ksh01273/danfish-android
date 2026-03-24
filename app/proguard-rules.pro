# ── DanFish ProGuard Rules ──

# WebView JavaScript Interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# WebView
-keepclassmembers class android.webkit.WebView {
    public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, int);
}

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Kotlin
-keep class kotlin.** { *; }
-dontwarn kotlin.**