plugins {
    id("elastic.android-test-library")
}

dependencies {
    api(project(":test-tools:agent-rule"))
    implementation(libs.robolectric)
}