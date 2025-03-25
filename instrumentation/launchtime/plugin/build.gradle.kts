plugins {
    id("elastic.instrumentation-plugin")
}

elasticBuildConfig {
    libraryUri()
}

elasticInstrumentationPlugins {
    create {
        implementationClass = "co.elastic.otel.android.launchtime.LaunchTimeInstrumentationPlugin"
        displayName = "Elastic OTel Android instrumentation for tracking app launch time"
    }
}