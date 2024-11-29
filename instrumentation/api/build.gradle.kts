plugins {
    id("android-library-conventions")
}

android {
    namespace = "co.elastic.otel.instrumentation.api"
}

dependencies {
    api(project(":android-api"))
}