// File: <projectRoot>/app/build.gradle.kts

plugins {
    // Áp dụng plugin Android Application (bắt buộc)
    id("com.android.application")
    // Áp dụng Google Services plugin để chạy Firebase (phải đặt sau android plugin)
    id("com.google.gms.google-services")
    // Nếu bạn dùng Kotlin trong module app, thêm dòng này. Nếu chỉ Java thì không cần.
    // kotlin("android")
}

android {
    namespace = "com.example.phuonglink_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.phuonglink_app"
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
    // Nếu bạn có dùng Kotlin, thêm:
    // kotlinOptions {
    //     jvmTarget = "11"
    // }
}

dependencies {
    // 1. Glide để load ảnh
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // 2. Firebase Authentication & Firestore
    implementation("com.google.firebase:firebase-auth:22.1.0")
    implementation("com.google.firebase:firebase-firestore:24.9.1")

    // 3. Material Components, CardView, AppCompat, ConstraintLayout
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 4. Các thư viện khác (giả sử bạn đã khai báo trong version catalog libs. nếu dùng)
    // implementation(libs.appcompat)
    // implementation(libs.material)
    // implementation(libs.activity)
    // implementation(libs.constraintlayout)

    // 5. Thư viện test (JUnit, Espresso…)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Sau khi chỉnh sửa xong, bấm “Sync Now” để Gradle tải về và build lại.
