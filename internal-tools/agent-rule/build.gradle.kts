plugins {
    id("elastic.android-test-library")
}

dependencies {
    implementation(project(":agent-sdk"))
    implementation(project(":internal-tools:test-common"))
    implementation(libs.opentelemetry.testing)
}