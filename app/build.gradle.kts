import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ktor_version: String by project
val nav_version: String by project

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) // recommended
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.projet_android_m2"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projet_android_m2"
        minSdk = 23
        targetSdk = 36
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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
    }
}