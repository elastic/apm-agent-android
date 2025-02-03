plugins {
    id("elastic.instrumentation-plugin")
}

elasticBuildConfig {
    projectUri("LIBRARY_URI", "launchtime-library")
}

elasticInstrumentationPlugins {
    create("launchtime") {
        implementationClass = "co.elastic.otel.android.launchtime.LaunchTimePlugin"
        displayName = "Elastic OTel Android instrumentation for tracking app launch time"
    }
}