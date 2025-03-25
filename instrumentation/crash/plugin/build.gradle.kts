plugins {
    id("elastic.instrumentation-plugin")
}

elasticBuildConfig {
    libraryUri()
}

elasticInstrumentationPlugins {
    create {
        implementationClass = "co.elastic.otel.android.crash.CrashInstrumentationPlugin"
        displayName = "Elastic OTel Android instrumentation for tracking app crashes"
    }
}