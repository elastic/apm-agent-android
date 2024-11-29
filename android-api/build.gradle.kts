plugins {
    id("android-library-conventions")
}

android {
    namespace = "co.elastic.otel.api"
}

dependencies {
    api(libs.opentelemetry.api)
}