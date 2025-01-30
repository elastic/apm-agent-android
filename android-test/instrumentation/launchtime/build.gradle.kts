plugins {
    id("elastic.android-app")
    id("co.elastic.otel.android.agent")
    id("co.elastic.otel.android.instrumentation-launchtime")
}

android {
    namespace = "co.elastic.otel.android.test"
}