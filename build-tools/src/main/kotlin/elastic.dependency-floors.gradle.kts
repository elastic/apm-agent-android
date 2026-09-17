/*
 * Dependency-floor feature map:
 * - This plugin owns the published dependency floors, the Android AAR compile
 *   SDK floor, and floor-test opt-ins. `gradle.properties` holds the floor
 *   values. `elastic.kotlin-compatibility` derives the Kotlin compatibility
 *   line and emitted metadata version from the Kotlin floor.
 * - Direct OkHttp declarations identify owning Android libraries. This plugin
 *   publishes their floor and exclusions without resolving dependency graphs.
 * - `elastic.latest-dependency-resolution` forces development builds to the
 *   catalog versions.
 * - The external test builds keep their settings/catalog wiring.
 *   `baseline-test` owns the consumer fixture: it reads the floors from the
 *   root `gradle.properties` for its Kotlin Gradle plugin version and for its
 *   `verifyBaselineFloors` task, which fails when the published graph
 *   resolves `kotlin-stdlib` or `okhttp` above its floor. The root build and
 *   the fixture settings each declare the baseline repository.
 * - CI owns the floor jobs; Renovate owns the excluded floor and fixture paths.
 */

import co.elastic.otel.android.compilation.tools.dependencies.DependencyFloorsExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.artifacts.ExternalModuleDependency

plugins {
    id("elastic.kotlin-compatibility")
    id("elastic.latest-dependency-resolution")
}

val kotlinFloorVersion = providers.gradleProperty("elastic.dependencies.kotlin.floor").get()
val okhttpFloorVersion = providers.gradleProperty("elastic.dependencies.okhttp.floor").get()

dependencies.add("api", "org.jetbrains.kotlin:kotlin-stdlib:$kotlinFloorVersion")

pluginManager.withPlugin("com.android.library") {
    configurations.named("implementation").configure {
        withDependencies {
            val externalDependencies = filterIsInstance<ExternalModuleDependency>()
            externalDependencies
                .firstOrNull {
                    it.group == "com.squareup.okhttp3" && it.name == "okhttp"
                }
                ?.let { okhttp ->
                    okhttp.version {
                        require(okhttpFloorVersion)
                    }
                    externalDependencies
                        .filter { it.group?.startsWith("io.opentelemetry") == true }
                        .forEach {
                            it.exclude(
                                mapOf(
                                    "group" to "com.squareup.okhttp3",
                                    "module" to "okhttp"
                                )
                            )
                        }
                }
        }
    }

    val compileSdk = providers.gradleProperty("elastic.android.compileSdk").get().toInt()
    extensions.configure<LibraryExtension> {
        defaultConfig {
            aarMetadata {
                minCompileSdk = compileSdk - 1
            }
        }
    }
}

extensions.create(
    "dependencyFloors",
    DependencyFloorsExtension::class.java,
    project
)
