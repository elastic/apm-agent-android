plugins {
    id("elastic.android-test-library")
}

dependencies {
    api(project(":internal-tools:agent-rule"))
    implementation(libs.robolectric)
}