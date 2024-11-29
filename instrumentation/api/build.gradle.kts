plugins {
    id("android-library-conventions")
}

android {
    namespace = "co.elastic.otel.instrumentation.api"
}

dependencies {
    implementation(project(":android-api"))
}