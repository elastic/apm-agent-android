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
            }
        }
    }
}

apiValidation {
    ignoredClasses.add("co.elastic.otel.android.BuildConfig")
}

dependencies {
    api(project(":agent-api"))
    api(project(":instrumentation:api"))
    implementation(libs.opentelemetry.api.incubator)
    implementation(libs.stagemonitor.configuration)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.bundles.opentelemetry.semconv)
    implementation(libs.opentelemetry.diskBuffering)
    implementation(libs.opentelemetry.opamp)
    implementation(libs.androidx.annotations)
    implementation(libs.androidx.core)
    implementation(libs.dsl.json)
    implementation(libs.okhttp)
    testImplementation(project(":internal-tools:otel-test-common"))
    testImplementation(libs.wireMock)
    testImplementation(libs.opentelemetry.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.awaitility)
}