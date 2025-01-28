plugins {
    id("elastic.android-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.launchtime"
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
}

dependencies {
    api(project(":instrumentation:api"))
    implementation(instrumentation.androidx.lifecycle.process)
}