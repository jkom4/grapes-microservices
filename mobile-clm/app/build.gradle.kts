// app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt") // Utilisez-le directement
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "grapes.microservices"
    // Consider using compileSdk 34 if 35 causes issues, as 35 might still be in preview/beta
    compileSdk = 35 // Or 35 if you are sure it's stable and needed

    defaultConfig {
        applicationId = "grapes.microservices"
        minSdk = 24
        // targetSdk should usually match compileSdk
        targetSdk = 34 // Or 35
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"  // Example version; adjust as needed
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Use the BOM correctly
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Use BOM here too
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest) // Often only needed for specific test scenarios

    // --- Koin ---
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.navigation)
    implementation(libs.koin.androidx.compose) // Pour l'intégration avec la navigation Compose
    testImplementation(libs.koin.test.junit4) // Pour les tests unitaires

    // --- Url Image ---
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil.network.okhttp)

    // --- Image Primary Color extractor
    implementation(libs.androidx.palette.ktx)
}