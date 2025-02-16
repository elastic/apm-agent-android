plugins {
    id("elastic.instrumentation-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.crash"
}

dependencies {
    implementation(libs.bundles.opentelemetry.semconv)
    implementation(instrumentation.opentelemetry.api.incubator)
    testImplementation(libs.robolectric)
    testImplementation(libs.opentelemetry.testing)
    testImplementation(project(":internal-tools:robolectric-agent-rule"))
}