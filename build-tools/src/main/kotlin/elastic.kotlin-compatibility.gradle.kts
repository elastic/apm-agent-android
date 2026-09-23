/*
 * Kotlin compatibility line for every Kotlin compilation in this build.
 *
 * The Kotlin floor is the published kotlin-stdlib version in gradle.properties. By policy the
 * compatibility line (apiVersion, languageVersion, emitted metadata version) is that version's
 * MAJOR.MINOR, so it is derived here once and never stored separately.
 */

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val kotlinFloorVersion = providers.gradleProperty("elastic.dependencies.kotlin.floor").get()
val kotlinCompatibility = KotlinVersion.fromVersion(kotlinFloorVersion.substringBeforeLast("."))
// Metadata versions are MAJOR.MINOR.PATCH; the compatibility line fixes patch at 0.
val kotlinMetadataVersion = "${kotlinCompatibility.version}.0"

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        apiVersion.set(kotlinCompatibility)
        languageVersion.set(kotlinCompatibility)
        freeCompilerArgs.addAll(
            "-jvm-default=no-compatibility",
            // Kotlin -X flags carry no stability promise; baseline-test proves this one works.
            "-Xmetadata-version=$kotlinMetadataVersion"
        )
    }
}
