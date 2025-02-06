plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = (project.property("elastic.android.compileSdk") as String).toInt()

    namespace = "co.elastic.otel.android.test"

    defaultConfig {
        minSdk = (project.property("elastic.android.minSdk") as String).toInt()
    }

    val javaVersionStr = project.property("elastic.java.compatibility") as String
    val javaVersion = JavaVersion.toVersion(javaVersionStr)
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersionStr
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    implementation(libs.findLibrary("junit4").get())
}