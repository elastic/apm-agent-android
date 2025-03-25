plugins {
    id("elastic.instrumentation-plugin")
}

elasticBuildConfig {
    libraryUri()
    byteBuddyPluginUri()
}

elasticInstrumentationPlugins {
    create {
        implementationClass = "co.elastic.otel.android.okhttp.OkHttpInstrumentationPlugin"
        displayName = "Elastic OTel Android instrumentation for tracking OkHttp requests"
    }
}