plugins {
    id("java-gradle-plugin")
    `kotlin-dsl`
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
    implementation(rootLibs.buildconfig.plugin)
    implementation(rootLibs.animalsniffer.plugin)
    implementation(rootLibs.android.plugin)
    testImplementation(rootLibs.junit4)
}

gradlePlugin {
    plugins {
        create("publishingPlugin") {
            id = "co.elastic.otel.publishing"
            implementationClass =
                "co.elastic.otel.android.compilation.tools.publishing.ApmPublisherRootPlugin"
        }
        create("dependencyEmbedderPlugin") {
            id = "co.elastic.otel.dependency.embedder"
            implementationClass =
                "co.elastic.otel.android.compilation.tools.embedding.EmbeddingDependenciesPlugin"
        }
    }
}