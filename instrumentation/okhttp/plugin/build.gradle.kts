plugins {
    id("elastic.instrumentation-plugin")
}

elasticBuildConfig {
    projectUri("LIBRARY_URI", "okhttp-library")
}

elasticInstrumentationPlugins {
    create("okhttp") {
        implementationClass = "co.elastic.otel.android.okhttp.OkHttpInstrumentationPlugin"
        displayName = "Elastic OTel Android instrumentation for tracking OkHttp requests"
    }
}