plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    id("com.github.gmazzo.buildconfig")
}

// AGP versions under test. Each version needs its own Gradle configuration so the JARs
// can be injected into the TestKit plugin classpath at test time.
val agpProjectVersion = libs.versions.android.get()
val agpTestVersions = listOf("8.0.0", "8.2.0", "8.7.0", "8.8.0", agpProjectVersion).distinct()

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
    systemProperty("test.agp.project.version", agpProjectVersion)
    systemProperty("test.agp.project.compileSdk", providers.gradleProperty("elastic.android.compileSdk").get())
    systemProperty("test.agp.project.gradleVersion", gradle.gradleVersion)
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