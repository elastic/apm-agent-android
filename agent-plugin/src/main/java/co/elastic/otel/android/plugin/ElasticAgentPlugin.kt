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
@file:Suppress("DEPRECATION")

package co.elastic.otel.android.plugin

import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.generated.BuildConfig
import co.elastic.otel.android.plugin.extensions.ElasticApmExtension
import co.elastic.otel.android.plugin.extensions.ElasticExtension
import co.elastic.otel.android.plugin.extensions.ElasticVariantExtension
import co.elastic.otel.android.plugin.internal.DslUtils
import co.elastic.otel.android.plugin.internal.ElasticCommonPlugin
import co.elastic.otel.android.plugin.internal.GenerateElasticAgentConfigClass
import co.elastic.otel.android.plugin.internal.logging.GradleLoggerFactory
import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.ApplicationVariantBuilder
import com.android.build.api.variant.ScopedArtifacts
import net.bytebuddy.build.gradle.android.ByteBuddyAndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider

class ElasticAgentPlugin : Plugin<Project> {
    private lateinit var project: Project
    private val byteBuddyDependencies = mutableSetOf<String>()

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
        androidComponents.beforeVariants { variantBuilder ->
            if (byteBuddyDependencies.isEmpty() || isBytecodeInstrumentationDisabled(androidExtension, variantBuilder)) {
                return@beforeVariants
            }
            byteBuddyDependencies.forEach { dependencyUri ->
                project.configurations.maybeCreate("${variantBuilder.name}ByteBuddy").dependencies.add(
                    project.dependencies.create(dependencyUri),
                )
            }
        }
        androidComponents.onVariants { variant ->
            val variantExtension = DslUtils.elasticExtension(variant)
            configureBuildIdClass(variant, variantExtension)
        }
    }

    fun addByteBuddyDependency(dependencyUri: String) {
        byteBuddyDependencies.add(dependencyUri)
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

    @Suppress("DEPRECATION")
    private fun isBytecodeInstrumentationDisabled(
        androidExtension: ApplicationExtension,
        variantBuilder: ApplicationVariantBuilder,
    ): Boolean {
        val buildTypeName = variantBuilder.buildType
            ?: error("Build type for variant '${variantBuilder.name}' was null")
        val projectExtension = (androidExtension as ExtensionAware)
            .extensions
            .getByType(ElasticExtension::class.java)
        val buildTypeExtension = androidExtension.buildTypes
            .getByName(buildTypeName)
            .extensions
            .getByType(ElasticExtension::class.java)
        val flavorExtensions = variantBuilder.productFlavors.map { (_, flavorName) ->
            androidExtension.productFlavors
                .getByName(flavorName)
                .extensions
                .getByType(ElasticExtension::class.java)
        }
        return DslUtils.mergeDslValue(
            projectExtension.bytecodeInstrumentation.disabled,
            flavorExtensions.map { it.bytecodeInstrumentation.disabled },
            buildTypeDisabledProviderWithLegacyFallback(buildTypeExtension, buildTypeName),
        ).orElse(false)
            .get()
    }

    private fun buildTypeDisabledProviderWithLegacyFallback(
        buildTypeExtension: ElasticExtension,
        buildTypeName: String,
    ): Provider<Boolean> {
        val legacyDisabled = project.extensions.findByType(ElasticApmExtension::class.java)
            ?.bytecodeInstrumentation?.disableForBuildTypes
            ?.map { buildTypeName in it }
            ?.orElse(false)
            ?.get()
            ?: false
        if (!legacyDisabled) {
            return buildTypeExtension.bytecodeInstrumentation.disabled
        }
        return buildTypeExtension.bytecodeInstrumentation.disabled.orElse(true)
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