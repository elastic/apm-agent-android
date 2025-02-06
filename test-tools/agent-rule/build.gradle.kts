plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "co.elastic.otel.android.test"
    compileSdk = (project.property("elastic.android.compileSdk") as String).toInt()

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

dependencies {
    implementation(project(":android-sdk"))
    implementation(libs.opentelemetry.testing)
    implementation(libs.junit4)
}