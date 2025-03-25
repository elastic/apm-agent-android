plugins {
    id("elastic.android-test-library")
}

dependencies {
    api(project(":agent-sdk"))
    api(libs.opentelemetry.testing)
}