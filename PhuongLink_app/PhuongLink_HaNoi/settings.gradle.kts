pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Áp Android Gradle plugin ở settings
        id("com.android.application") version "8.0.2" apply false
        // Áp Google Services plugin
        id("com.google.gms.google-services") version "4.3.15" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PhuongLink_HaNoi"
include(":app")
