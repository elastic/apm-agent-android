pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}
rootProject.name = "APM Android Agent"
includeBuild("build-tools")
include(":android-sdk")
include(":android-plugin")
include(":android-common")

val instrumentationDirName = "instrumentation"
val instrumentationDir = File(rootDir, instrumentationDirName)
val separator = Regex("[/\\\\]")
instrumentationDir.walk().maxDepth(3).forEach {
    if (it.name.equals("build.gradle.kts")) {
        include(
            ":$instrumentationDirName:${
                it.parentFile.toRelativeString(instrumentationDir).replace(separator, ":")
            }"
        )
    }
}
