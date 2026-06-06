plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.fintechaiassistantapp"
    compileSdk = 36

    // FIXED FOR AAR COMPATIBILITY

    defaultConfig {
        applicationId = "com.example.fintechaiassistantapp"

        minSdk = 24
        targetSdk = 34

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    // Core AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // =========================
    // ROOM DATABASE (UPDATED)
    // =========================
    val roomVersion = "2.7.0"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // =========================
    // LIFECYCLE (SAFE FOR VOICE + OCR)
    // =========================
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")

    // =========================
    // BIOMETRIC SECURITY
    // =========================
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // =========================
    // RECYCLER VIEW
    // =========================
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // =========================
    // SWIPE REFRESH
    // =========================
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // =========================
    // CARD VIEW
    // =========================
    implementation("androidx.cardview:cardview:1.0.0")

    // =========================
    // MPAndroidChart
    // =========================
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // =========================
    // OCR (PHASE 8)
    // =========================
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // =========================
    // NETWORKING (PHASE 11)
    // =========================
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // =========================
    // WORK MANAGER
    // =========================
    val workVersion = "2.10.0"
    implementation("androidx.work:work-runtime:$workVersion")

    // =========================
    // TESTING
    // =========================
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}