plugins {
    id("elastic.instrumentation-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.launchtime"
}

dependencies {
    implementation(instrumentation.androidx.lifecycle.process)
}