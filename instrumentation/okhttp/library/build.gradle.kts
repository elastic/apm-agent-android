plugins {
    id("elastic.instrumentation-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.okhttp"
}

dependencies {
    implementation(instrumentation.opentelemetry.instrumentation.api)
    compileOnly(instrumentation.okhttp)
}