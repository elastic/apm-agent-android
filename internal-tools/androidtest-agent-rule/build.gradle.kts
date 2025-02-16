plugins {
    id("elastic.android-test-library")
}

dependencies {
    api(project(":internal-tools:agent-rule"))
    api(instrumentation.bundles.androidTest)
    api(libs.opentelemetry.testing)
    api(libs.assertj)
}