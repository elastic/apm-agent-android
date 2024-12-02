plugins {
    id("elastic.android-library")
}

android {
    namespace = "co.elastic.otel.instrumentation.api"
}

dependencies {
    api(project(":android-api"))
}