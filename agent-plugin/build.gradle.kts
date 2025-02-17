plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    id("com.github.gmazzo.buildconfig")
}

dependencies {
    api(project(":agent-common"))
    implementation(libs.byteBuddy)
    implementation(libs.byteBuddy.plugin)
    compileOnly(libs.android.plugin)
}

buildConfig {
    packageName("${group}.generated")
    buildConfigField("String", "SDK_DEPENDENCY_URI", "\"$group:agent-sdk:$version\"")
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