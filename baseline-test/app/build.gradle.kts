import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("co.elastic.otel.android.agent")
}

android {
    namespace = "co.elastic.otel.android.baseline"
    compileSdk = providers.gradleProperty("baseline.compileSdk")
        .map { it.toInt() }
        .getOrElse(36)

    defaultConfig {
        applicationId = "co.elastic.otel.android.baseline"
        minSdk = 26
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
