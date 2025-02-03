plugins {
    id("elastic.android-library")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.api"
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
}

dependencies {
    api(project(":android-api"))
}