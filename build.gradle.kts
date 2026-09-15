import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("co.elastic.otel.publishing")
}

// Lists the Gradle plugins this build publishes to the Gradle Plugin Portal,
// one "<project path> <plugin id>" line each. .ci/release.sh uses it to tell
// an "already published" failure from a real one.
tasks.register("listPublishedGradlePlugins") {
    group = "release"
    description = "Prints one '<project path> <plugin id>' line per plugin published to the Gradle Plugin Portal."
    doLast {
        subprojects
            .filter { it.plugins.hasPlugin("com.gradle.plugin-publish") }
            .forEach { subproject ->
                subproject.extensions.getByType<GradlePluginDevelopmentExtension>().plugins.forEach { plugin ->
                    println("${subproject.path} ${plugin.id}")
                }
            }
    }
}
