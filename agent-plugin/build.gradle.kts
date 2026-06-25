plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    id("com.github.gmazzo.buildconfig")
}

val agpTestVersions = listOf("8.7.0", "8.8.0", "9.2.1")

val agpTestConfigs = agpTestVersions.associateWith { version ->
    configurations.create("agp${version.replace(".", "")}") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

dependencies {
    api(project(":agent-common"))
    implementation(libs.byteBuddy)
    implementation(libs.byteBuddy.plugin)
    compileOnly(libs.android.plugin)
    testImplementation(gradleTestKit())
    agpTestVersions.forEach { version ->
        add("agp${version.replace(".", "")}", "com.android.tools.build:gradle:$version")
    }
}

tasks.named("test", Test::class.java) {
    agpTestConfigs.forEach { (version, config) ->
        inputs.files(config)
        doFirst {
            systemProperty("test.agp.classpath.$version", config.asPath)
        }
    }
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