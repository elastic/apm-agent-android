plugins {
    id("elastic.java-library")
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
}

dependencies {
    api(libs.opentelemetry.sdk)
}