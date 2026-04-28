plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.foodenhancer.app"
    compileSdk = Versions.compileSdk

    defaultConfig {
        applicationId = "com.foodenhancer.app"
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = 1
        versionName = "1.0-demo"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeCompiler
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ml"))
    implementation(project(":core-network"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature-camera"))
    implementation(project(":feature-editor"))

    // Hilt
    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Versions.hilt}")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:${Versions.composeBom}")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // AndroidX
    implementation("androidx.core:core-ktx:${Versions.coreKtx}")
    implementation("androidx.activity:activity-compose:${Versions.activityCompose}")

    // Navigation
    implementation("androidx.navigation:navigation-compose:${Versions.navigationCompose}")
    implementation("androidx.hilt:hilt-navigation-compose:${Versions.hiltNavigationCompose}")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}")

    // CameraX
    implementation("androidx.camera:camera-core:${Versions.cameraX}")
    implementation("androidx.camera:camera-camera2:${Versions.cameraX}")
    implementation("androidx.camera:camera-lifecycle:${Versions.cameraX}")
    implementation("androidx.camera:camera-view:${Versions.cameraX}")

    // Coil
    implementation("io.coil-kt:coil-compose:${Versions.coil}")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
}
