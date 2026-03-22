# ----------------------------------------------------------------------------------
# Drive Backup Module - Consumer Rules
# These rules are automatically applied to any module that depends on :drive_backup.
# ----------------------------------------------------------------------------------

# ── Database (Room) ───────────────────────────────────────────────────────────────
# Room uses reflection to instantiate the database and DAO implementations.
-keep class * extends androidx.room.RoomDatabase

# Keep all Entity classes in this module to prevent field renaming.
# Renaming fields breaks database migrations and schema matching.
-keep class com.monetra.data.local.entity.** { *; }

# Keep DAO interfaces and their generated implementation classes.
# Without this, Room cannot find the implementation at runtime.
-keep interface com.monetra.data.local.dao.** { *; }
-keep class com.monetra.data.local.dao.**_Impl { *; }

# Preserve TypeConverters (needed for complex data types in Room)
-keep class com.monetra.data.local.Converters { *; }

# ── Google Drive API & GSON ────────────────────────────────────────────────────────
# The Google Drive REST API uses GSON for JSON serialization of its model classes.
# Fields must be kept so GSON can map JSON keys to class fields via reflection.
-keep class com.google.api.services.drive.model.** { *; }
-keep class com.google.api.client.json.gson.GsonFactory { *; }

# ── Dependency Injection (Hilt) ────────────────────────────────────────────────────
# Hilt generates bootstrapping code that R8 might accidentally remove if not kept.
-keep class com.monetra.drivebackup.di.** { *; }

# ── WorkManager ───────────────────────────────────────────────────────────────────
# Workers are instantiated by name via reflection by the WorkManager library.
-keep class com.monetra.drivebackup.internal.worker.BackupWorker {
    <init>(...);
}

# ── Kotlin Serialization ──────────────────────────────────────────────────────────
# If any models in this module use @Serializable, their serializers must be kept.
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}
-keep @kotlinx.serialization.Serializable class *
-keep class * {
    @kotlinx.serialization.Serializable *;
}
