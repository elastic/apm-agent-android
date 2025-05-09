plugins {
    id("elastic.instrumentation-plugin")
}

elasticBuildConfig {
    libraryUri()
}

elasticInstrumentationPlugins {
    create(extraTags = listOf("experimental")) {
        implementationClass = "co.elastic.otel.android.oteladapter.ExperimentalOtelAdapterPlugin"
        displayName =
            "Elastic OTel Android experimental instrumentation adapter for OTel Android instrumentations"
    }
}