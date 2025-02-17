pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("instrumentation") {
            from(files("./gradle/instrumentation.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}
rootProject.name = "APM Android Agent"
includeBuild("build-tools")
include(":android-api")
include(":android-sdk")
include(":agent-plugin")
include(":android-common")
includeFromDir("instrumentation")
includeFromDir("internal-tools", 2)

fun includeFromDir(dirName: String, maxDepth: Int = 3) {
    val instrumentationDir = File(rootDir, dirName)
    val separator = Regex("[/\\\\]")
    instrumentationDir.walk().maxDepth(maxDepth).forEach {
        if (it.name.equals("build.gradle.kts")) {
            include(
                ":$dirName:${
                    it.parentFile.toRelativeString(instrumentationDir).replace(separator, ":")
                }"
            )
        }
    }
}