pluginManagement {
    val agentProperties = java.util.Properties()
    file("../gradle.properties").inputStream().use {
        agentProperties.load(it)
    }
    plugins {
        // The baseline: the oldest Android Gradle plugin that supports compile SDK 36, and the
        // Kotlin Gradle plugin at exactly the published Kotlin floor.
        id("com.android.application") version "8.10.0"
        id("org.jetbrains.kotlin.android") version
            agentProperties.getProperty("elastic.dependencies.kotlin.floor")
        id("co.elastic.otel.android.agent") version agentProperties.getProperty("version")
    }
    gradle.rootProject {
        extra["kotlinFloorVersion"] = agentProperties.getProperty("elastic.dependencies.kotlin.floor")
        extra["okhttpFloorVersion"] = agentProperties.getProperty("elastic.dependencies.okhttp.floor")
    }
}

val baselineRepository = file("../build/baseline-maven")

fun RepositoryHandler.baselineRepository() {
    exclusiveContent {
        forRepository {
            maven {
                name = "baseline"
                url = baselineRepository.toURI()
            }
        }
        filter {
            includeGroupByRegex("co\\.elastic\\.otel\\.android.*")
        }
    }
}

pluginManagement.repositories {
    baselineRepository()
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        baselineRepository()
        google()
        mavenCentral()
    }
}

rootProject.name = "edot-android-baseline-test"
include(":app")
