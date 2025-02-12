plugins {
    id("elastic.instrumentation-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.okhttp.bytebuddy"
}

dependencies {
    implementation(project(":instrumentation:okhttp:library"))
    implementation(libs.byteBuddy)
    implementation(instrumentation.okhttp)
}