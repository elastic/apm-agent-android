plugins {
    id("elastic.android-test-library")
}

dependencies {
    implementation(project(":android-sdk"))
    implementation(libs.opentelemetry.testing)
}