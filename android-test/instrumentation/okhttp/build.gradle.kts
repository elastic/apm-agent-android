plugins {
    id("elastic.instrumentation-test-app")
    id("co.elastic.otel.android.agent")
    id("co.elastic.otel.android.instrumentation-okhttp")
}

android {
    namespace = "co.elastic.otel.android.test"
}

dependencies {
    androidTestImplementation(instrumentation.okhttp)
    androidTestImplementation(instrumentation.mockWebServer)
}