import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("elastic.kotlin-compatibility")
}

val javaVersionStr = project.property("elastic.java.compatibility") as String
android {
    compileSdk = (project.property("elastic.android.compileSdk") as String).toInt()

    namespace = "co.elastic.otel.android.test"

    defaultConfig {
        minSdk = (project.property("elastic.android.minSdk") as String).toInt()
    }

    val javaVersion = JavaVersion.toVersion(javaVersionStr)
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersionStr)
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    api(libs.findLibrary("junit4").get())
}
