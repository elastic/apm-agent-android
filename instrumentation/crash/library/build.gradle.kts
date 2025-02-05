plugins {
    id("elastic.instrumentation-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.crash"
}

dependencies {
    implementation(libs.bundles.opentelemetry.semconv)
    implementation(instrumentation.opentelemetry.api.incubator)
}