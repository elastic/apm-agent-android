import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("co.elastic.otel.android.agent")
    id("co.elastic.otel.android.instrumentation.okhttp")
}

android {
    namespace = "co.elastic.otel.android.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "co.elastic.otel.android.sample"
        minSdk = 26
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "co.elastic.otel.android.sample.tools.SampleAppJunitRunner"
    }

    buildTypes {
        debug {
            if (project.hasProperty("elastic.testing.automated")) {
                logger.warn("Building debug with minify enabled for instrumentation tests")
                isMinifyEnabled = true
                isDebuggable = false
                testProguardFiles(
                    file("androidtest-rules.pro"),
                    rootProject.file("../shared-rules.pro")
                )
                proguardFiles(file("test-rules.pro"))
            }
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs["debug"]
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    val retrofitVersion = "3.0.0"
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    androidTestImplementation("co.elastic.otel.android:otel-test-common")
    androidTestImplementation(instrumentationLibs.bundles.androidTest)
    androidTestImplementation(instrumentationLibs.mockWebServer)
}