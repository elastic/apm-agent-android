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
    api(libs.opentelemetry.sdk)
    api(libs.opentelemetry.android)
    api(libs.opentelemetry.api.incubator)
    api(libs.stagemonitor.configuration)
    api(libs.okhttp)
    implementation(project(":android-common"))
    implementation(libs.opentelemetry.android.instrumentation.api)
    implementation(libs.opentelemetry.android.instrumentation.lifecycle)
    implementation(libs.androidx.lifecycle)
    implementation(libs.weaklockfree)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.semconv)
    implementation(libs.opentelemetry.diskBuffering)
    implementation(libs.androidx.annotations)
    implementation(libs.dsl.json)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.wireMock)
    testImplementation(libs.opentelemetry.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.awaitility)
}