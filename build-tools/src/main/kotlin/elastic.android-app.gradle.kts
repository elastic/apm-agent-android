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