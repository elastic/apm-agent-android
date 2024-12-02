plugins {
    id("elastic.android-library")
}

android {
    namespace = "co.elastic.otel.instrumentation.crash"
}

dependencies {
    implementation(project(":instrumentation:api"))
    implementation(libs.opentelemetry.semconv)
}