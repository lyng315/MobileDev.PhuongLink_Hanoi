plugins {
    // Android Application plugin
    alias(libs.plugins.android.application)

    // Google Services plugin (phải đặt ngay sau android plugin)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.phuonglink_hanoi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.phuonglink_hanoi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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
        // Nếu bạn dùng ViewBinding
        viewBinding = true
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    // Dùng Firebase BOM để quản lý version đồng bộ
    implementation(platform("com.google.firebase:firebase-bom:31.5.0"))
    implementation ("com.android.volley:volley:1.2.1")
    // Firebase Auth, Firestore, Storage (ko cần ktx nếu bạn dùng Java)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Firebase App Check interop (cho StorageRegistrar)
    implementation("com.google.firebase:firebase-appcheck-interop")
    implementation ("com.google.firebase:firebase-messaging") // Kiểm tra phiên bản mới nhất
    // Glide để load ảnh (avatar, placeholder…)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // AndroidX & Material
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Lifecycle (LiveData & ViewModel)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Navigation component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
