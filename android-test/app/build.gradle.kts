plugins {
    id("elastic.android-app")
    id("co.elastic.otel.android.agent")
}

android {
    namespace = "co.elastic.apm.android.test"
}

dependencies {
    implementation(libs.appCompat)
    implementation(libs.espresso.idlingResource)
    implementation(libs.fragmentTesting)
    coreLibraryDesugaring(libs.coreLib)
    testImplementation(libs.openTelemetry.exporter)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito)
    testImplementation(libs.mockWebServer)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.mockWebServer)
}