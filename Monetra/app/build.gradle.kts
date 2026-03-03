plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)   // Compose compiler plugin (registers kotlin extension)
    alias(libs.plugins.ksp)              // code generation for Hilt + Room
    alias(libs.plugins.hilt.android)     // Hilt — generates component & module glue
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace  = "com.monetra"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId         = "com.monetra"
        minSdk                = 30
        targetSdk             = 36
        versionCode           = 1
        versionName           = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,DEPENDENCIES}"
        }
    }
}
androidComponents.onVariants { variant ->
    variant.outputs.forEach { output ->
        if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
            val vName = output.versionName.get() ?: "1.0"
            output.outputFileName.set("Monetra-v$vName-${variant.name}.apk")
        }
    }
}
// ── KSP options ───────────────────────────────────────────────────────────────
// Room: export schema JSON files for migration history and testing.
// The folder is version-controlled so migrations can be verified.
ksp {
    arg("room.schemaLocation",  "$projectDir/schemas")
    arg("room.incremental",     "true")
    arg("room.generateKotlin",  "true")   // generate Kotlin sources (Room 2.6+)
}

dependencies {
    implementation(project(":drive_backup"))
    // ── Core ──────────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore.preferences)

    // ── Lifecycle ─────────────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.compose)  // collectAsStateWithLifecycle
    implementation(libs.androidx.work.runtime.ktx)

    // ── Compose ───────────────────────────────────────────────────────────
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)

    // ── Hilt ──────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)             // annotation processor via KSP
    implementation(libs.hilt.navigation.compose) // hiltViewModel() in composables
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // ── Room ──────────────────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)               // coroutines + Flow extensions
    ksp(libs.room.compiler)                     // generates DAO implementations

    // ── Test ──────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
