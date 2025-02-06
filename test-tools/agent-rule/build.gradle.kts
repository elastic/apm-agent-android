plugins {
    id("elastic.android-test-library")
}

dependencies {
    implementation(project(":android-sdk"))
    implementation(project(":test-tools:test-common"))
    implementation(libs.opentelemetry.testing)
}