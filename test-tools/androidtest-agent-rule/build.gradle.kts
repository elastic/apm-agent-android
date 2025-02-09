plugins {
    id("elastic.android-test-library")
}

dependencies {
    api(project(":test-tools:agent-rule"))
    api(instrumentation.bundles.androidTest)
    api(libs.opentelemetry.testing)
    api(libs.assertj)
}