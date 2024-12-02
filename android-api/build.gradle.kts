plugins {
    id("elastic.android-library")
}

android {
    namespace = "co.elastic.otel.api"
}

dependencies {
    api(libs.opentelemetry.api)
    api(libs.opentelemetry.common)
}