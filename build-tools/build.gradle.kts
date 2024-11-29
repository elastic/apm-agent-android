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
    implementation(libs.apache.commons.text)
    implementation(libs.commons.io)
    implementation(libs.spotless.plugin)
    implementation(libs.dokka)
    implementation(libs.nexus.publish.plugin)
    implementation(libs.gradle.publish.plugin)
    implementation(libs.gradle.shadow.plugin)
    implementation("com.android.tools.build:gradle:${properties.getProperty("androidGradlePlugin_version")}")
    testImplementation(libs.junit4)
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