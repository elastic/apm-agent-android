plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    alias(libs.plugins.buildconfig)
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
}

buildConfig {
    packageName("${group}.generated")
    buildConfigField("String", "LIBRARY_URI", "\"$group:launchtime-library:$version\"")
}

dependencies {
    api(project(":android-api"))
}

gradlePlugin {
    plugins {
        create("androidLaunchTime") {
            id = "co.elastic.otel.android.instrumentation-launchtime"
            implementationClass = "co.elastic.otel.android.launchtime.LaunchTimePlugin"
            displayName = "Elastic OTel Android instrumentation for tracking app launch time"
            description = project.description
            tags.addAll("Android", "APM", "Elastic", "ELK", "opentelemetry")
        }
    }
}
