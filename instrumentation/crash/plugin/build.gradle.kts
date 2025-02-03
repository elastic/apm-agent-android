plugins {
    id("elastic.instrumentation-plugin")
}

elasticBuildConfig {
    projectUri("LIBRARY_URI", "crash-library")
}

elasticInstrumentationPlugins {
    create("crash") {
        implementationClass = "co.elastic.otel.android.crash.CrashInstrumentationPlugin"
        displayName = "Elastic OTel Android instrumentation for tracking app crashes"
    }
}