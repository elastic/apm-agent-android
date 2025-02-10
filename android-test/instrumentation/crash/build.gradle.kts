plugins {
    id("elastic.instrumentation-test-app")
    id("co.elastic.otel.android.agent")
    id("co.elastic.otel.android.instrumentation-crash")
}

android {
    namespace = "co.elastic.otel.android.test"
}