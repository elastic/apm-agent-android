configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "com.squareup.okhttp3" && requested.name == "okhttp-jvm") {
                // This is needed as opentelemetry-java is forcing the usage of okhttp-jvm, which is
                // not the right one for android projects. More info here: https://github.com/open-telemetry/opentelemetry-java/issues/7491
                useTarget("com.squareup.okhttp3:okhttp:${requested.version}")
                because("choosing okhttp over okhttp-jvm")
            }
        }
    }
}