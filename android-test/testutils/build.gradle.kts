plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "co.elastic.otel.android.test"
    kotlinOptions {
        jvmTarget = "11"
    }
}
