plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("co.elastic.otel.android.agent")
    id("co.elastic.otel.android.instrumentation-okhttp")
    id("co.elastic.otel.android.instrumentation-launchtime")
    id("co.elastic.otel.android.instrumentation-crash")
}

android {
    namespace = "co.elastic.otel.android.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "co.elastic.otel.android.sample"
        minSdk = 26
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs["debug"]
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    val retrofitVersion = "2.11.0"
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.7")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    androidTestImplementation("co.elastic.otel.android:otel-test-common")
    androidTestImplementation(instrumentationLibs.bundles.androidTest)
}