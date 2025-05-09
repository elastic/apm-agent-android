plugins {
    id("elastic.instrumentation-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.oteladapter"
}

dependencies {
    implementation(instrumentation.opentelemetry.android.session)
    implementation(instrumentation.opentelemetry.android.instrumentation)
}