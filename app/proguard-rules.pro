# ----------------------------------------------------------------------------------
# Spend Sense - App-Level ProGuard / R8 Configuration
# ----------------------------------------------------------------------------------

# ── General Optimizations ────────────────────────────────────────────────────────
# Use the default android optimizations as a baseline (handled in build.gradle)

# Allow R8 to perform more aggressive optimizations
-allowaccessmodification
-repackageclasses ''

# ── Log Removal ──────────────────────────────────────────────────────────────────
# Remove all debug and verbose logs from the release build for security and size.
# Assumes Log.d, Log.v, and Log.i are primarily used for developmental debugging.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ── Dependency Injection (Hilt / Dagger) ───────────────────────────────────────────
# Hilt generates bootstrapping and component code accessed via reflection.
-keepattributes *Annotation*
-keep class dagger.hilt.android.internal.managers.** { *; }

# Keep Lifecycle classes frequently accessed by Hilt/System
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends androidx.activity.ComponentActivity
-keep class * extends androidx.viewmodel.ViewModel

# Keep Hilt generated modules and entry points
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.android.EntryPoint class *

# ── Kotlin Serialization & Navigation ──────────────────────────────────────────────
# Required for route keys in MonetraNavGraph and MainScreenContainer.
# Navigation 3 relies on @Serializable objects as navigation keys.
-keepattributes Signature, EnclosingMethod, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}

# Explicitly keep Serializers for navigation routes
-keep @kotlinx.serialization.Serializable class com.monetra.presentation.navigation.Route**
-keepclassmembers class com.monetra.presentation.navigation.Route** {
    public static ** serializer();
}

# ── Networking (Retrofit / OkHttp) ────────────────────────────────────────────────
# Baseline preservation for Retrofit interfaces. 
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keep interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.internal.**
-dontwarn retrofit2.Platform$Java8

# ── Jetpack Compose ───────────────────────────────────────────────────────────────
# Keep composer members for optimization reliability.
-keepclassmembers class * extends androidx.compose.runtime.Composer { *; }
-dontwarn androidx.compose.**

# ── GSON (Baseline) ───────────────────────────────────────────────────────────────
# Even if GSON is internal to Drive API, keeping tokens prevents runtime errors.
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.stream.JsonReader

# ── Reflection & Metadata (Kotlin) ────────────────────────────────────────────────
-keepattributes Metadata
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ── Risky Areas (Manual Check) ────────────────────────────────────────────────────
# Ensure any dynamic class loading via Class.forName() is handled here if added.