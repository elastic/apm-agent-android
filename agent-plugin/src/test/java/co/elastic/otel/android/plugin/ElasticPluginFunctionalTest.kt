/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.otel.android.plugin

import java.io.File
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Properties
import java.util.stream.Stream
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class ElasticPluginFunctionalTest {

    @TempDir
    lateinit var projectDir: Path

    /**
     * Tests that the default build ID is the SHA-256 of "appId-versionName-versionCode"
     * when no overrides are configured, across all supported AGP versions.
     */
    @ParameterizedTest(name = "AGP {0}")
    @MethodSource("agpVersions")
    fun `elasticOtel DSL - default build id is SHA-256 of appId versionName versionCode`(agpVersion: String, compileSdk: Int, gradleVersion: String) {
        writeAndroidProject(
            agpVersion,
            """
            plugins {
                id("com.android.application")
                id("co.elastic.otel.android.agent")
            }

            android {
                namespace = "co.elastic.test"
                compileSdk = $compileSdk

                defaultConfig {
                    applicationId = "co.elastic.test"
                    minSdk = 23
                    versionCode = 1
                    versionName = "1.0"
                }
            }

            tasks.register("printBuildIds") {
                doLast {
                    fun property(taskName: String, getter: String): org.gradle.api.provider.Property<*> {
                        val task = tasks.named(taskName).get()
                        return task.javaClass.getMethod(getter).invoke(task) as org.gradle.api.provider.Property<*>
                    }
                    println("releaseBuildId=" + property("releaseGenerateElasticAgentConfigClass", "getBuildId").get())
                    println("debugBuildId=" + property("debugGenerateElasticAgentConfigClass", "getBuildId").get())
                }
            }
            """.trimIndent(),
        )

        val result = gradleRunner(agpVersion, gradleVersion, "printBuildIds").build()

        val expectedBuildId = sha256("co.elastic.test-1.0-1")
        assertTrue(result.output.contains("releaseBuildId=$expectedBuildId"), result.output)
        assertTrue(result.output.contains("debugBuildId=$expectedBuildId"), result.output)
    }

    /**
     * Tests the full DSL merge precedence: build type > flavor > project > default.
     * Verifies across all supported AGP versions that:
     * - A build type override beats everything
     * - A flavor override beats project and default
     * - A project-level override beats the default
     */
    @ParameterizedTest(name = "AGP {0}")
    @MethodSource("agpVersionsWithPerVariantDsl")
    fun `elasticOtel DSL - build ids merge precedence across project, flavor, and build type`(agpVersion: String, compileSdk: Int, gradleVersion: String) {
        writeAndroidProject(
            agpVersion,
            """
            import co.elastic.otel.android.plugin.extensions.ElasticExtension

            plugins {
                id("com.android.application")
                id("co.elastic.otel.android.agent")
            }

            android {
                namespace = "co.elastic.test"
                compileSdk = $compileSdk

                defaultConfig {
                    applicationId = "co.elastic.test"
                    minSdk = 23
                    versionCode = 1
                    versionName = "1.0"
                }

                flavorDimensions += "tier"
                productFlavors {
                    create("demo") {
                        dimension = "tier"
                        extensions.configure<ElasticExtension> {
                            buildId.set("demo-override")
                        }
                    }
                    create("full") {
                        dimension = "tier"
                    }
                }

                buildTypes {
                    release {
                        extensions.configure<ElasticExtension> {
                            buildId.set("release-override")
                        }
                    }
                }
            }

            elasticOtel {
                buildId.set("project-override")
            }

            tasks.register("printBuildIds") {
                doLast {
                    fun property(taskName: String, getter: String): org.gradle.api.provider.Property<*> {
                        val task = tasks.named(taskName).get()
                        return task.javaClass.getMethod(getter).invoke(task) as org.gradle.api.provider.Property<*>
                    }
                    println("fullReleaseBuildId=" + property("fullReleaseGenerateElasticAgentConfigClass", "getBuildId").get())
                    println("demoReleaseBuildId=" + property("demoReleaseGenerateElasticAgentConfigClass", "getBuildId").get())
                    println("demoDebugBuildId=" + property("demoDebugGenerateElasticAgentConfigClass", "getBuildId").get())
                    println("fullDebugBuildId=" + property("fullDebugGenerateElasticAgentConfigClass", "getBuildId").get())
                }
            }
            """.trimIndent(),
        )

        val result = gradleRunner(agpVersion, gradleVersion, "printBuildIds").build()

        // Build type wins over flavor and project
        assertTrue(result.output.contains("fullReleaseBuildId=release-override"), result.output)
        assertTrue(result.output.contains("demoReleaseBuildId=release-override"), result.output)
        // Flavor wins over project
        assertTrue(result.output.contains("demoDebugBuildId=demo-override"), result.output)
        // Project wins over default
        assertTrue(result.output.contains("fullDebugBuildId=project-override"), result.output)
    }

    /**
     * Tests that setting bytecodeInstrumentation.disabled=true at the project level
     * prevents bytebuddy transform tasks from being wired for all variants.
     */
    @ParameterizedTest(name = "AGP {0}")
    @MethodSource("agpVersions")
    fun `bytecodeInstrumentation - project level disabled prevents all bytebuddy transforms`(agpVersion: String, compileSdk: Int, gradleVersion: String) {
        writeAndroidProject(
            agpVersion,
            """
            plugins {
                id("com.android.application")
                id("co.elastic.otel.android.agent")
            }

            plugins.withId("co.elastic.otel.android.agent") {
                plugins.getPlugin("co.elastic.otel.android.agent")
                    .javaClass
                    .getMethod("addByteBuddyDependency", String::class.java)
                    .invoke(plugins.getPlugin("co.elastic.otel.android.agent"), "co.elastic.test:bytebuddy-plugin:1.0")
            }

            android {
                namespace = "co.elastic.test"
                compileSdk = $compileSdk

                defaultConfig {
                    applicationId = "co.elastic.test"
                    minSdk = 23
                    versionCode = 1
                    versionName = "1.0"
                }
            }

            elasticOtel {
                bytecodeInstrumentation.disabled.set(true)
            }
            """.trimIndent(),
        )

        val output = gradleRunner(agpVersion, gradleVersion, "assembleRelease", "assembleDebug", "--dry-run").build().output

        assertFalse(output.contains("BytebuddyTransform"), "Expected no BytebuddyTransform tasks but found some:\n$output")
    }

    /**
     * Tests that bytecodeInstrumentation.disabled at build type and flavor level controls
     * bytebuddy wiring per-variant. Also verifies backward compatibility with the legacy
     * elasticAgent.bytecodeInstrumentation.disableForBuildTypes API.
     */
    @ParameterizedTest(name = "AGP {0}")
    @MethodSource("agpVersionsWithPerVariantDsl")
    fun `bytecodeInstrumentation - build type and flavor level controls bytebuddy wiring`(agpVersion: String, compileSdk: Int, gradleVersion: String) {
        writeAndroidProject(
            agpVersion,
            """
            import co.elastic.otel.android.plugin.extensions.ElasticExtension

            plugins {
                id("com.android.application")
                id("co.elastic.otel.android.agent")
            }

            plugins.withId("co.elastic.otel.android.agent") {
                plugins.getPlugin("co.elastic.otel.android.agent")
                    .javaClass
                    .getMethod("addByteBuddyDependency", String::class.java)
                    .invoke(plugins.getPlugin("co.elastic.otel.android.agent"), "co.elastic.test:bytebuddy-plugin:1.0")
            }

            android {
                namespace = "co.elastic.test"
                compileSdk = $compileSdk

                defaultConfig {
                    applicationId = "co.elastic.test"
                    minSdk = 23
                    versionCode = 1
                    versionName = "1.0"
                }

                flavorDimensions += "tier"
                productFlavors {
                    create("demo") {
                        dimension = "tier"
                        extensions.configure<ElasticExtension> {
                            bytecodeInstrumentation.disabled.set(true)
                        }
                    }
                    create("full") {
                        dimension = "tier"
                    }
                }

                buildTypes {
                    debug {
                        extensions.configure<ElasticExtension> {
                            bytecodeInstrumentation.disabled.set(true)
                        }
                    }
                    release {
                    }
                    create("staging") {
                    }
                }
            }

            @Suppress("DEPRECATION")
            elasticAgent {
                @Suppress("DEPRECATION")
                bytecodeInstrumentation.disableForBuildTypes.set(listOf("staging"))
            }
            """.trimIndent(),
        )

        val output = gradleRunner(
            agpVersion,
            gradleVersion,
            "assembleFullRelease",
            "assembleFullDebug",
            "assembleFullStaging",
            "assembleDemoRelease",
            "--dry-run",
        ).build().output

        // Only fullRelease has bytebuddy enabled (not disabled by build type, flavor, or legacy API)
        assertTrue(output.contains(":fullReleaseBytebuddyTransform SKIPPED"), output)
        assertFalse(output.contains(":fullDebugBytebuddyTransform"), output)     // disabled by build type
        assertFalse(output.contains(":fullStagingBytebuddyTransform"), output)   // disabled by legacy API
        assertFalse(output.contains(":demoReleaseBytebuddyTransform"), output)   // disabled by flavor
    }

    private fun writeAndroidProject(agpVersion: String, buildFile: String) {
        // Each AGP version loads a large number of classes; without extra metaspace the Gradle
        // daemon running test builds runs OOM when sequential test invocations reuse the daemon.
        projectDir.resolve("gradle.properties").toFile().writeText(
            "org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m\n",
        )
        projectDir.resolve("settings.gradle.kts").toFile().writeText(
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }

            rootProject.name = "elastic-plugin-test"
            """.trimIndent(),
        )
        projectDir.resolve("build.gradle.kts").toFile().writeText(buildFile)
    }

    private fun gradleRunner(agpVersion: String, gradleVersion: String, vararg arguments: String): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments(*arguments, "--stacktrace")
            .withPluginClasspath(elasticPluginClasspath() + agpClasspath(agpVersion))
            .withGradleVersion(gradleVersion)
            .forwardOutput()
    }

    // Reads the elastic plugin's own classpath from the metadata file generated by java-gradle-plugin.
    // This contains only implementation/api deps — not compileOnly (i.e. AGP is excluded here).
    private fun elasticPluginClasspath(): List<File> {
        val resource = javaClass.classLoader.getResourceAsStream("plugin-under-test-metadata.properties")
            ?: error("plugin-under-test-metadata.properties not found; ensure java-gradle-plugin is applied")
        val props = Properties().also { it.load(resource) }
        return props.getProperty("implementation-classpath")
            .split(File.pathSeparator)
            .map(::File)
    }

    // Reads the resolved classpath for a specific AGP version, passed as a system property
    // by the test task configured in build.gradle.kts.
    private fun agpClasspath(agpVersion: String): List<File> {
        val paths = System.getProperty("test.agp.classpath.$agpVersion")
            ?: error("System property 'test.agp.classpath.$agpVersion' not set — check test task setup in build.gradle.kts")
        return paths.split(File.pathSeparator).map(::File)
    }

    companion object {
        /**
         * AGP versions under test, paired with their recommended Gradle version and compileSdk.
         * See https://developer.android.com/build/releases/about-agp#updating-gradle
         */
        @JvmStatic
        fun agpVersions(): Stream<Arguments> = Stream.of(
            arguments("8.0.0", 34, "8.4"),
            arguments("8.7.0", 35, "8.11.1"),
            arguments("8.8.0", 35, "8.11.1"),
            arguments("9.2.1", 36, "9.6.0"),
        )

        /**
         * AGP versions that support per-variant DSL extensions (extendBuildTypeWith /
         * extendProductFlavorWith). AGP 8.0.0 has the same class of bug as extendProjectWith
         * (issuetracker.google.com/issues/260100335) — extensions are not attached during the
         * android {} configuration phase, so extensions.configure<ElasticExtension> {} inside
         * productFlavors or buildTypes blocks throws UnknownDomainObjectException.
         */
        @JvmStatic
        fun agpVersionsWithPerVariantDsl(): Stream<Arguments> = Stream.of(
            arguments("8.7.0", 35, "8.11.1"),
            arguments("8.8.0", 35, "8.11.1"),
            arguments("9.2.1", 36, "9.6.0"),
        )

        private fun sha256(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(input.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        }
    }
}
