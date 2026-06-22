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
package co.elastic.otel.android.plugin.internal

import co.elastic.otel.android.plugin.extensions.ElasticExtension
import co.elastic.otel.android.plugin.extensions.ElasticVariantExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.DslExtension
import com.android.build.api.variant.VariantOutputConfiguration
import java.security.MessageDigest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class ElasticCommonPlugin : Plugin<Project> {
    private lateinit var project: Project

    override fun apply(target: Project) {
        project = target
        target.pluginManager.withPlugin("com.android.application") {
            val androidComponents =
                target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.registerExtension(
                DslExtension.Builder(EXTENSION_NAME)
                    .extendProjectWith(ElasticExtension::class.java)
                    .extendBuildTypeWith(ElasticExtension::class.java)
                    .extendProductFlavorWith(ElasticExtension::class.java)
                    .build(),
            ) { config ->
                val projectExtension = config.projectExtension(ElasticExtension::class.java)
                val buildTypeExtension = config.buildTypeExtension(ElasticExtension::class.java)
                val flavorExtensions = config.productFlavorsExtensions(ElasticExtension::class.java)
                target.objects.newInstance(ElasticVariantExtension::class.java).also { variantExtension ->
                    configureVariantExtension(
                        variantExtension = variantExtension,
                        projectExtension = projectExtension,
                        flavorExtensions = flavorExtensions,
                        buildTypeExtension = buildTypeExtension,
                        variant = config.variant,
                    )
                }
            }
        }
    }

    private fun configureVariantExtension(
        variantExtension: ElasticVariantExtension,
        projectExtension: ElasticExtension,
        flavorExtensions: List<ElasticExtension>,
        buildTypeExtension: ElasticExtension,
        variant: ApplicationVariant,
    ) {
        variantExtension.buildId.convention(
            DslUtils.mergeDslValue(
                projectExtension.buildId,
                flavorExtensions.map { it.buildId },
                buildTypeExtension.buildId,
            )
                .orElse(defaultBuildId(variant)),
        )
    }

    private fun defaultBuildId(variant: ApplicationVariant): Provider<String> {
        val output = variant.outputs.firstOrNull { it.outputType == VariantOutputConfiguration.OutputType.SINGLE }
            ?: variant.outputs.first()
        return variant.applicationId.zip(output.versionName) { appId, versionName ->
            sha256("$appId-$versionName-${output.versionCode.get()}")
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val EXTENSION_NAME = "elasticOtel"
    }
}
