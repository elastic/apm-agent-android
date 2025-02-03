plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    id("com.github.gmazzo.buildconfig")
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

gradlePlugin {
    plugins {
        create("elasticAndroidAgent") {
            id = "co.elastic.otel.android.agent"
            implementationClass = "co.elastic.otel.android.plugin.ElasticAgentPlugin"
            displayName = "Elastic OTel Android Agent"
            description = project.description
            tags.addAll("Android", "APM", "Elastic", "ELK", "opentelemetry")
        }
    }
}