pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("rootLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
        create("instrumentationLibs") {
            from(files("../gradle/instrumentation.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.apache.commons:commons-lang3:3.19.0")
        classpath("org.apache.commons:commons-compress:1.28.0")
        classpath("com.squareup.moshi:moshi-kotlin:1.15.2")
    }
}
rootProject.name = "Android APM Sample app"
includeBuild("..")
include(":app")
include(":backend")
include(":edot-collector")
