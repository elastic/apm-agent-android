import org.gradle.api.publish.PublishingExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("co.elastic.otel.publishing")
}

val baselineRepository = layout.buildDirectory.dir("baseline-maven")
subprojects {
    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "baseline"
                    url = baselineRepository.get().asFile.toURI()
                }
            }
        }
    }
}
