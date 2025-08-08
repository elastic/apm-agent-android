import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val properties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    properties.load(it)
}

val javaVersionStr = properties.getProperty("elastic.java.compatibility") as String
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
    packaging.resources {
        excludes += "META-INF/LICENSE*"
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersionStr)
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    testImplementation(libs.findBundle("mocking").get())
    testImplementation(libs.findLibrary("junit4").get())
    testImplementation(libs.findLibrary("assertj").get())
    androidTestImplementation("co.elastic.otel.android:test-common")
    androidTestImplementation("co.elastic.otel.android:androidtest-agent-rule")
}