plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.foodenhancer.core_ml"
    compileSdk = Versions.compileSdk

    defaultConfig {
        minSdk = Versions.minSdk
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
    implementation("org.tensorflow:tensorflow-lite:${Versions.tensorflowLite}")
    implementation("org.tensorflow:tensorflow-lite-gpu:${Versions.tensorflowLiteGpu}")
    implementation("org.tensorflow:tensorflow-lite-support:${Versions.tensorflowLiteSupport}")
}
