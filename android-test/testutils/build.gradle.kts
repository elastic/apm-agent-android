import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val properties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    properties.load(it)
}

android {
    namespace = "co.elastic.otel.android.test"
    compileSdk = (properties.getProperty("elastic.android.compileSdk") as String).toInt()

    val javaVersionStr = properties.getProperty("elastic.java.compatibility") as String
    val javaVersion = JavaVersion.toVersion(javaVersionStr)
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersionStr
    }
}

dependencies {
    implementation(libs.androidx.test.runner)
}