pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("rootLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}
includeBuild("..")
include(":app")
include(":android-test-common")
include(":plugin-test")

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
