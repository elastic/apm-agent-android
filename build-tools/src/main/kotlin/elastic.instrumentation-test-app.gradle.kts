import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("elastic.android-app")
}

val properties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    properties.load(it)
}

val javaVersionString = properties.getProperty("elastic.java.compatibility")
val javaVersion = JavaVersion.toVersion(javaVersionString)
android {
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
        jvmTarget = JvmTarget.fromTarget(javaVersionString)
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
