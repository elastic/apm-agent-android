pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
        create("instrumentation") {
            from(files("../gradle/instrumentation.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}
includeBuild("..")

val instrumentationDirName = "instrumentation"
val instrumentationDir = File(rootDir, instrumentationDirName)
val separator = Regex("[/\\\\]")
instrumentationDir.walk().maxDepth(2).forEach {
    if (it.name.equals("build.gradle.kts")) {
        include(
            ":$instrumentationDirName:${
                it.parentFile.toRelativeString(instrumentationDir).replace(separator, ":")
            }"
        )
    }
}
