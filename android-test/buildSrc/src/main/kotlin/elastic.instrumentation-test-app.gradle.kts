import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val properties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    properties.load(it)
}

android {
    compileSdk = (properties.getProperty("elastic.android.compileSdk") as String).toInt()

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        animationsDisabled = true
    }

    val javaVersionStr = properties.getProperty("elastic.java.compatibility") as String
    val javaVersion = JavaVersion.toVersion(javaVersionStr)
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersionStr
    }
    packaging.resources {
        excludes += "META-INF/LICENSE*"
    }
}

val rootLibs = extensions.getByType<VersionCatalogsExtension>().named("rootLibs")
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    testImplementation(rootLibs.findBundle("mocking").get())
    testImplementation(rootLibs.findLibrary("junit4").get())
    testImplementation(rootLibs.findLibrary("assertj").get())
    androidTestImplementation(rootLibs.findLibrary("assertj").get())
    androidTestImplementation(rootLibs.findLibrary("junit4").get())
    androidTestImplementation(rootLibs.findLibrary("opentelemetry-testing").get())
    androidTestImplementation(libs.findBundle("androidTest").get())
}