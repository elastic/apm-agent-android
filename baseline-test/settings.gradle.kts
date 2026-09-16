pluginManagement {
    val agentProperties = java.util.Properties()
    file("../gradle.properties").inputStream().use {
        agentProperties.load(it)
    }
    val agentVersion = agentProperties.getProperty("version")
    plugins {
        id("co.elastic.otel.android.agent") version agentVersion
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
