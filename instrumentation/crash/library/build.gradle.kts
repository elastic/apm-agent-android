plugins {
    id("elastic.instrumentation-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.crash"
}

dependencies {
    implementation(instrumentation.opentelemetry.instrumentation.api)
    implementation(instrumentation.opentelemetry.instrumentation.api.incubator)
}