package co.elastic.otel.android.compilation.tools.dependencies

import org.gradle.api.Project

open class DependencyFloorsExtension(private val project: Project) {

    fun testUnitTestsAtFloors() {
        val useFloors = project.providers.gradleProperty("elastic.dependencies.useFloors")
            .map { it.toBoolean() }
            .getOrElse(false)
        if (!useFloors) {
            return
        }

        val okhttpFloorVersion =
            project.providers.gradleProperty("elastic.dependencies.okhttp.floor").get()
        val kotlinFloorVersion =
            project.providers.gradleProperty("elastic.dependencies.kotlin.floor").get()
        project.configurations.matching {
            it.name.endsWith("UnitTestCompileClasspath") ||
                it.name.endsWith("UnitTestRuntimeClasspath")
        }.configureEach {
            resolutionStrategy.eachDependency {
                when {
                    requested.group == "com.squareup.okhttp3" && requested.name == "okhttp" -> {
                        useVersion(okhttpFloorVersion)
                        because("the published OkHttp floor is under unit test")
                    }

                    requested.group == "org.jetbrains.kotlin" &&
                        requested.name == "kotlin-stdlib" -> {
                        useVersion(kotlinFloorVersion)
                        because("the published Kotlin floor is under unit test")
                    }
                }
            }
        }
    }
}
