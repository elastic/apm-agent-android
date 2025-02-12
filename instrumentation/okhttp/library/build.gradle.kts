plugins {
    id("elastic.instrumentation-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.okhttp"
}

dependencies {
    implementation(instrumentation.opentelemetry.instrumentation.api)
    implementation(instrumentation.opentelemetry.instrumentation.api.incubator)
    implementation(instrumentation.opentelemetry.instrumentation.okhttp)
    compileOnly(instrumentation.okhttp)
}