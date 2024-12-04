import java.util.Properties

plugins {
    id("java-gradle-plugin")
    `kotlin-dsl`
}

val properties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    properties.load(it)
}

dependencies {
    implementation(rootLibs.apache.commons.text)
    implementation(rootLibs.commons.io)
    implementation(rootLibs.spotless.plugin)
    implementation(rootLibs.dokka)
    implementation(rootLibs.nexus.publish.plugin)
    implementation(rootLibs.gradle.publish.plugin)
    implementation(rootLibs.gradle.shadow.plugin)
    implementation(rootLibs.kotlin.plugin)
    implementation("com.android.tools.build:gradle:${properties.getProperty("androidGradlePlugin_version")}")
    testImplementation(rootLibs.junit4)
}

gradlePlugin {
    plugins {
        create("publishingPlugin") {
            id = "co.elastic.apm.publishing"
            implementationClass = "co.elastic.apm.compile.tools.publishing.ApmPublisherRootPlugin"
        }
        create("dependencyEmbedderPlugin") {
            id = "co.elastic.apm.dependency.embedder"
            implementationClass =
                "co.elastic.apm.compile.tools.embedding.EmbeddingDependenciesPlugin"
        }
    }
}