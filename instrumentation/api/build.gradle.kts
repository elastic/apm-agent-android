plugins {
    id("elastic.android-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.api"
}

dependencies {
    api(project(":agent-api"))
}