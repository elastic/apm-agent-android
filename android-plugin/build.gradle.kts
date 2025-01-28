plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    alias(libs.plugins.buildconfig)
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
}

licensesConfig {
    manualMappingFile = rootProject.file("manual_licenses_map.txt")
}

gradlePlugin {
    plugins {
        create("androidOtelPlugin") {
            id = "co.elastic.otel.android"
            implementationClass = "co.elastic.otel.android.plugin.ApmAndroidAgentPlugin"
            displayName = "Elastic OTel Android Agent"
            description = project.description
            tags.addAll("Android", "APM", "Elastic", "ELK", "opentelemetry")
        }
    }
}