plugins {
    id("elastic.instrumentation-test-app")
    id("co.elastic.otel.android.agent")
    id("co.elastic.otel.android.instrumentation.oteladapter")
}

android {
    namespace = "co.elastic.otel.android.test.oteladapter"
}

val logInstrumentationVersion = "1.3.0-alpha"
dependencies {
    implementation("io.opentelemetry.android.instrumentation:android-log-library:$logInstrumentationVersion")
    byteBuddy("io.opentelemetry.android.instrumentation:android-log-agent:$logInstrumentationVersion")
}