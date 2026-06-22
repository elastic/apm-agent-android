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

import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.generated.BuildConfig
import co.elastic.otel.android.plugin.extensions.ElasticApmExtension
import co.elastic.otel.android.plugin.extensions.ElasticVariantExtension
import co.elastic.otel.android.plugin.internal.ApplicationVariantListener
import co.elastic.otel.android.plugin.internal.ElasticCommonPlugin
import co.elastic.otel.android.plugin.internal.GenerateElasticAgentConfigClass
import co.elastic.otel.android.plugin.internal.elasticExtension
import co.elastic.otel.android.plugin.internal.logging.GradleLoggerFactory
import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.ScopedArtifacts
import net.bytebuddy.build.gradle.android.ByteBuddyAndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class ElasticAgentPlugin : Plugin<Project> {
    private lateinit var project: Project
    private val applicationVariantListeners = mutableSetOf<ApplicationVariantListener>()
    private val configuredVariants = mutableListOf<Pair<ApplicationVariant, ElasticVariantExtension>>()

    override fun apply(target: Project) {
        this.project = target
        Elog.init(GradleLoggerFactory())
        project.pluginManager.apply(ElasticCommonPlugin::class.java)
        val androidExtension = project.extensions.getByType(ApplicationExtension::class.java)
        addByteBuddyPlugin(androidExtension)
        addSdkDependency()
        @Suppress("DEPRECATION")
        project.extensions.create("elasticAgent", ElasticApmExtension::class.java)
        val androidComponents =
            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val variantExtension = variant.elasticExtension()
            configureBuildIdClass(variant, variantExtension)
            configuredVariants.add(variant to variantExtension)
            applicationVariantListeners.forEach { listener ->
                listener.onApplicationVariant(variant, variantExtension)
            }
        }
    }

    fun addApplicationVariantListener(listener: ApplicationVariantListener) {
        applicationVariantListeners.add(listener)
        configuredVariants.forEach { (variant, elastic) -> listener.onApplicationVariant(variant, elastic) }
    }

    private fun addByteBuddyPlugin(androidExtension: ApplicationExtension) {
        project.pluginManager.apply(ByteBuddyAndroidPlugin::class.java)
        androidExtension.buildTypes.all {
            project.configurations.maybeCreate(it.name + "ByteBuddy")
        }
    }

    private fun addSdkDependency() {
        project.dependencies.add("implementation", BuildConfig.SDK_DEPENDENCY_URI)
    }

    private fun configureBuildIdClass(
        variant: ApplicationVariant,
        elasticExtension: ElasticVariantExtension,
    ) {
        val variantName = variant.name
        val taskProvider = project.tasks.register(
            "${variantName}GenerateElasticAgentConfigClass",
            GenerateElasticAgentConfigClass::class.java,
        ) { task ->
            task.buildId.set(elasticExtension.buildId)
            task.outputDirectory.set(
                project.layout.buildDirectory.dir("intermediates/elastic/agent/$variantName/classes"),
            )
        }

        variant.artifacts
            .forScope(ScopedArtifacts.Scope.PROJECT)
            .use(taskProvider)
            .toAppend(
                ScopedArtifact.CLASSES,
                GenerateElasticAgentConfigClass::outputDirectory,
            )
    }
}