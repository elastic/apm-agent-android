plugins {
    id("elastic.android-library")
}

android {
    namespace = "co.elastic.otel.android"
    buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField("String", "APM_AGENT_VERSION", "\"${project.version}\"")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("agent_version", project.version)
                it.systemProperty("elastic.test", "true")
            }
        }
    }
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
}

dependencies {
    api(project(":android-api"))
    implementation(libs.stagemonitor.configuration)
    implementation(project(":android-common"))
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.bundles.opentelemetry.semconv)
    implementation(libs.opentelemetry.diskBuffering)
    implementation(libs.androidx.annotations)
    implementation(libs.androidx.core)
    implementation(libs.dsl.json)
    testImplementation(libs.wireMock)
    testImplementation(libs.opentelemetry.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.awaitility)
}