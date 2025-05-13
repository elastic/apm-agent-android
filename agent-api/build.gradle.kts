plugins {
    id("elastic.java-library")
}

dependencies {
    api(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.api.incubator)
}