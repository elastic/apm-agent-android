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
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ElasticPluginFunctionalTest {

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `bytebuddy dependencies are added only when bytecode instrumentation is enabled`() {
        writeAndroidProject(
            """
            import co.elastic.otel.android.plugin.extensions.ElasticExtension

            plugins {
                id("com.android.application") version "$ANDROID_PLUGIN_VERSION"
                id("co.elastic.otel.android.agent")
            }

            val listenerClass = Class.forName("co.elastic.otel.android.plugin.internal.ApplicationVariantListener")
            val attacherClass = Class.forName("co.elastic.otel.android.plugin.internal.ByteBuddyDependencyAttacher")
            plugins.withId("co.elastic.otel.android.agent") {
                val attacher = attacherClass
                    .getConstructor(org.gradle.api.Project::class.java, String::class.java)
                    .newInstance(project, "co.elastic.test:bytebuddy-plugin:1.0")
                plugins.getPlugin("co.elastic.otel.android.agent")
                    .javaClass
                    .getMethod("addApplicationVariantListener", listenerClass)
                    .invoke(plugins.getPlugin("co.elastic.otel.android.agent"), attacher)
            }

            android {
                namespace = "co.elastic.test"
                compileSdk = 36

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

            androidComponents {
                onVariants { variant ->
                    val extensionClass = Class.forName("co.elastic.otel.android.plugin.extensions.ElasticVariantExtension") as Class<Any>
                    val extension = variant.getExtension(extensionClass)!!
                    val bytecodeInstrumentation = extension.javaClass.getMethod("getBytecodeInstrumentation").invoke(extension)
                    val disabled = bytecodeInstrumentation.javaClass.getMethod("getDisabled").invoke(bytecodeInstrumentation) as org.gradle.api.provider.Property<*>
                    println("variantDisabled=" + variant.name + ":" + disabled.get())
                }
            }

            tasks.register("printByteBuddyDependencies") {
                doLast {
                    listOf("demoDebug", "demoRelease", "demoStaging", "fullDebug", "fullRelease", "fullStaging").forEach { variantName ->
                        val dependencies = configurations.findByName("${'$'}{variantName}ByteBuddy")?.dependencies
                            ?.joinToString { "${'$'}{it.group}:${'$'}{it.name}:${'$'}{it.version}" }
                            .orEmpty()
                        println("byteBuddyDependencies=${'$'}variantName:${'$'}dependencies")
                    }
                }
            }
            """.trimIndent(),
        )

        val output = gradleRunner("printByteBuddyDependencies").build().output

        assertTrue(output.contains("variantDisabled=fullRelease:false"))
        assertTrue(output.contains("variantDisabled=fullDebug:true"))
        assertTrue(output.contains("variantDisabled=demoDebug:true"))
        assertTrue(output.contains("variantDisabled=demoRelease:true"))
        assertTrue(output.contains("variantDisabled=demoStaging:true"))
        assertTrue(output.contains("variantDisabled=fullStaging:true"))

        assertTrue(output.contains("byteBuddyDependencies=fullRelease:co.elastic.test:bytebuddy-plugin:1.0"))
        assertTrue(output.contains("byteBuddyDependencies=fullDebug:"))
        assertTrue(output.contains("byteBuddyDependencies=fullStaging:"))
        assertTrue(output.contains("byteBuddyDependencies=demoDebug:"))
        assertTrue(output.contains("byteBuddyDependencies=demoRelease:"))
        assertTrue(output.contains("byteBuddyDependencies=demoStaging:"))
    }

    @Test
    fun `elasticOtel DSL is compatible with AGP 8_0_0`() {
        writeAndroidProject(
            """
            import co.elastic.otel.android.plugin.extensions.ElasticExtension

            plugins {
                id("com.android.application") version "8.0.0"
                id("co.elastic.otel.android.agent")
            }

            android {
                namespace = "co.elastic.test"
                compileSdk = 33

                defaultConfig {
                    applicationId = "co.elastic.test"
                    minSdk = 23
                    versionCode = 1
                    versionName = "1.0"
                }

                buildTypes {
                    release {
                        extensions.configure<ElasticExtension> {
                            buildId.set("release-override")
                        }
                    }
                }

                elasticOtel {
                    buildId.set("project-level")
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

        val result = gradleRunner("printBuildIds").build()

        assertTrue(result.output.contains("releaseBuildId=release-override"))
        assertTrue(result.output.contains("debugBuildId=project-level"))
    }

    private fun writeAndroidProject(buildFile: String) {
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

    private fun gradleRunner(vararg arguments: String): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments(*arguments, "--stacktrace")
            .withPluginClasspath(testRuntimeClasspath())
            .forwardOutput()
    }

    private fun testRuntimeClasspath(): List<File> {
        return System.getProperty("java.class.path")
            .split(File.pathSeparator)
            .map(::File)
    }

    companion object {
        private const val ANDROID_PLUGIN_VERSION = "9.2.1"
    }
}
