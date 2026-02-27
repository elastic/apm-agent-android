import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("co.elastic.otel.android.agent")
    id("co.elastic.otel.android.instrumentation.okhttp")
}

val withDesugaring = providers.gradleProperty("withDesugaring").map { it.toBoolean() }.getOrElse(false)

android {
    namespace = "co.elastic.otel.android.integration"
    compileSdk = 36

    defaultConfig {
        applicationId = "co.elastic.otel.android.integration"
        minSdk = if (withDesugaring) 24 else 26
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["debug"]
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = withDesugaring
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    if (withDesugaring) {
        coreLibraryDesugaring(rootLibs.coreLib)
    }
}