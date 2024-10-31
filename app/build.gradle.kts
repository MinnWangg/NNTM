plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization") version "1.9.0"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }

    // Thêm packagingOptions để loại bỏ các tệp trùng lặp
    packaging {
        resources {
            excludes.addAll(listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            ))
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.anastr:speedviewlib:1.6.1")
    implementation("com.github.anastr:speedviewlib:latest-version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("com.google.android.material:material:1.9.0")

    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0-dev-1"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

//    implementation("com.google.api-client:google-api-client:1.32.1")
//    implementation("com.google.auth:google-auth-library-oauth2-http:1.12.0")
//    implementation("com.google.apis:google-api-services-sheets:v4-rev614-1.18.0-rc")
//    implementation("com.google.api-client:google-api-client-extensions:1.6.0-beta")
//
    implementation ("com.google.api-client:google-api-client-android:1.32.1")
    implementation ("com.google.apis:google-api-services-sheets:v4-rev612-1.25.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.1.0")

        implementation ("org.tensorflow:tensorflow-lite:2.6.0")  








    implementation("androidx.activity:activity-compose:1.6.1")
    implementation(libs.firebase.database)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
