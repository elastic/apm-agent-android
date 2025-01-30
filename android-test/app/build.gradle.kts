plugins {
    alias(rootLibs.plugins.androidApp)
    alias(rootLibs.plugins.kotlin.android)
    id("co.elastic.otel.android.agent")
}

android {
    namespace = "co.elastic.apm.android.test"
    compileSdk = rootProject.extra["androidCompileSdk"] as Int
    val agentVersion = rootProject.extra["agentVersion"] as String
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("agentVersion", agentVersion)
            }
        }
        animationsDisabled = true
    }
    defaultConfig {
        applicationId = "co.elastic.apm.android.test"
        minSdk = rootProject.extra["androidMinSdk"] as Int
        targetSdk = rootProject.extra["androidCompileSdk"] as Int
        versionCode = 5
        versionName = "1.0"

        testInstrumentationRunner = "co.elastic.apm.android.test.base.TestRunner"
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
    val jvmCompatibility = rootProject.extra["jvmCompatibility"] as JavaVersion
    compileOptions {
        sourceCompatibility = jvmCompatibility
        targetCompatibility = jvmCompatibility
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = jvmCompatibility.toString()
    }
}

dependencies {
    implementation(libs.appCompat)
    implementation(libs.espresso.idlingResource)
    implementation(libs.fragmentTesting)
    coreLibraryDesugaring(libs.coreLib)
    testImplementation(libs.openTelemetry.exporter)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito)
    testImplementation(libs.mockWebServer)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.mockWebServer)
}