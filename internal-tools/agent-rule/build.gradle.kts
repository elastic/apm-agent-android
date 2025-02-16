plugins {
    id("elastic.android-test-library")
}

dependencies {
    implementation(project(":android-sdk"))
    implementation(project(":internal-tools:test-common"))
    implementation(libs.opentelemetry.testing)
}