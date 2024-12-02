plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

dependencies {
    implementation(libs.byteBuddy)
    implementation(libs.byteBuddy.plugin)
    implementation(project(":android-common"))
    compileOnly("com.android.tools.build:gradle:${project.property("androidGradlePlugin_version")}")
}

buildConfig {
    packageName("${group}.generated")
    buildConfigField("String", "SDK_DEPENDENCY_URI", "\"$group:android-sdk:$version\"")
    buildConfigField("String", "SDK_KTX_DEPENDENCY_URI", "\"$group:android-sdk-ktx:$version\"")
    buildConfigField(
        "String",
        "OTEL_OKHTTP_LIBRARY_URI",
        "\"${libs.opentelemetry.android.okhttpLib.get()}\""
    )
    buildConfigField(
        "String",
        "OTEL_OKHTTP_AGENT_URI",
        "\"${libs.opentelemetry.android.okhttpAgent.get()}\""
    )
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
}

gradlePlugin {
    plugins {
        create("apmPlugin") {
            id = "co.elastic.apm.android"
            implementationClass = "co.elastic.apm.android.plugin.ApmAndroidAgentPlugin"
            displayName = "Elastic APM Android Agent"
            description = project.description
            tags.addAll("Android", "APM", "Elastic", "ELK", "opentelemetry")
        }
    }
}