plugins {
    id("android-library-conventions")
}

android {
    namespace = "co.elastic.otel.instrumentation.crash"
}

dependencies {
    implementation(project(":instrumentation:api"))
}