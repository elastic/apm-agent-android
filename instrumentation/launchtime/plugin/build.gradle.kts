plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
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
