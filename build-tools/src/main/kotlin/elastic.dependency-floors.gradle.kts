/*
 * Dependency-floor feature map:
 * - This plugin owns published dependency floors, Kotlin compatibility and
 *   metadata, the Android AAR compile SDK floor, and floor-test opt-ins.
 * - Direct OkHttp declarations identify owning Android libraries. This plugin
 *   publishes their floor and exclusions without resolving dependency graphs.
 * - The external test builds keep their settings/catalog wiring, baseline-test
 *   owns the consumer fixture, and the root build plus fixture settings each
 *   declare the baseline repository.
 * - CI owns the floor jobs; Renovate owns the excluded floor and fixture paths.
 */

import co.elastic.otel.android.compilation.tools.dependencies.DependencyFloorsExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("elastic.latest-dependency-resolution")
}

val kotlinCompatibility =
    KotlinVersion.fromVersion(providers.gradleProperty("elastic.kotlin.compatibility").get())
// Metadata versions are MAJOR.MINOR.PATCH; the compatibility floor fixes patch at 0.
val kotlinMetadataVersion = "${kotlinCompatibility.version}.0"
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
                    okhttp.exclude(
                        mapOf(
                            "group" to "org.jetbrains.kotlin",
                            "module" to "kotlin-stdlib"
                        )
                    )
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
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        apiVersion.set(kotlinCompatibility)
        languageVersion.set(kotlinCompatibility)
        // Kotlin -X flags carry no stability promise; baseline-test proves this one works.
        freeCompilerArgs.add("-Xmetadata-version=$kotlinMetadataVersion")
    }
}

pluginManager.withPlugin("com.android.library") {
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
