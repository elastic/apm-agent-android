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
        minSdk = (properties.getProperty("elastic.android.minSdk") as String).toInt()
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = javaVersionStr
    }
}

tasks.withType(Test::class).configureEach {
    useJUnitPlatform()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("rootLibs")
dependencies {
    testImplementation(libs.findBundle("mocking").get())
    testImplementation(libs.findBundle("junit").get())
    testImplementation(libs.findLibrary("assertj").get())
    testRuntimeOnly(libs.findLibrary("junit5-vintage").get())
    coreLibraryDesugaring(libs.findLibrary("coreLib").get())
    androidTestImplementation(libs.findBundle("junit").get())
    androidTestImplementation(libs.findBundle("androidTest").get())
}