plugins {
    id("elastic.android-test-library")
}

dependencies {
    implementation(project(":test-tools:agent-rule"))
    implementation(libs.robolectric)
}