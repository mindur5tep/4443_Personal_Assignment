plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.personal_assignment"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.personal_assignment"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.espresso.core)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.circleimageview)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation (libs.android.test.runner)
    androidTestImplementation(libs.android.test.rules)
    androidTestImplementation(libs.espresso.contrib)
    annotationProcessor(libs.room.compiler)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.test.rules)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.porcupine)
    implementation(libs.activity.ktx)
}