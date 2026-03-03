# Add project specific ProGuard rules here.
# For more details, see http://developer.android.com/guide/developing/tools/proguard.html

# Room

# Hilt
-keep class com.google.** { *; }
-keep interface dagger.** { *; }
-keep class dagger.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keep class kotlinx.coroutines.android.** { *; }

# Compose
-keep class androidx.compose.material.icons.** { *; }

# Monetra Models (keep domain/data models from being obfuscated to avoid Room/JSON issues)
-keep class com.monetra.domain.model.** { *; }
-keep class com.monetra.data.local.entity.** { *; }
-keep class com.monetra.data.local.dao.** { *; }

# Keep SourceFile and LineNumberTable for better crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile