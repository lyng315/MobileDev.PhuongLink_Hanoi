// Top-level build file where you can add configuration options common to all sub-projects/modules.

// 1. Thêm buildscript để đăng ký Google-Services Gradle plugin
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Đưa version plugin phù hợp (dialog gợi ý 4.4.2)
        classpath("com.google.gms:google-services:4.4.2")
    }
}

plugins {
    // Giữ nguyên alias Android application, nhưng áp false ở top-level
    alias(libs.plugins.android.application) apply false
}
