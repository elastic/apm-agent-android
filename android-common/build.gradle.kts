plugins {
    id("elastic.java-library")
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
}

dependencies {
    api(libs.slf4j.api)
    implementation(libs.androidx.annotations)
}