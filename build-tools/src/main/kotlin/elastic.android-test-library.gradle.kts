import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("com.android.library")
    id("elastic.common-dependency-conventions")
}

val javaVersionStr = project.property("elastic.java.compatibility") as String
val minKotlinVersionStr = project.property("elastic.kotlin.compatibility") as String
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

val minKotlinVersion = KotlinVersion.fromVersion(minKotlinVersionStr)
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersionStr)
        apiVersion = minKotlinVersion
        languageVersion = minKotlinVersion
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    api(libs.findLibrary("junit4").get())
}