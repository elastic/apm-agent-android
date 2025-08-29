plugins {
    id("elastic.instrumentation-test-app")
    id("co.elastic.otel.android.agent")
    id("co.elastic.otel.android.instrumentation.okhttp")
}

android {
    namespace = "co.elastic.otel.android.test.okhttp"
}

dependencies {
    androidTestImplementation(libs.okhttp)
    androidTestImplementation(instrumentation.mockWebServer)
}