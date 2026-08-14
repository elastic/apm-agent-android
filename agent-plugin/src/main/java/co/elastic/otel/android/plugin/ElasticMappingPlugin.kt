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

import co.elastic.otel.android.plugin.extensions.ElasticVariantExtension
import co.elastic.otel.android.plugin.internal.DslUtils
import co.elastic.otel.android.plugin.internal.ElasticCommonPlugin
import co.elastic.otel.android.plugin.internal.mapping.upload.GenerateElasticsearchBulkRequestBody
import co.elastic.otel.android.plugin.internal.mapping.upload.UploadMappingToElasticsearch
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import java.nio.charset.StandardCharsets
import java.util.Locale
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val INDEX_PREFIX = ".android-r8-mappings-"
private val INVALID_INDEX_NAME_CHARACTERS = setOf('\\', '/', '*', '?', '"', '<', '>', '|', ',', '#', ':')

class ElasticMappingPlugin : Plugin<Project> {
    private lateinit var project: Project

    override fun apply(target: Project) {
        this.project = target
        project.pluginManager.apply(ElasticCommonPlugin::class.java)

        project.pluginManager.withPlugin("com.android.application") {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                registerMappingUploadTasks(variant, DslUtils.elasticExtension(variant))
            }
        }
    }

    private fun registerMappingUploadTasks(
        variant: ApplicationVariant,
        extension: ElasticVariantExtension,
    ) {
        val variantName = variant.name
        val indexName = extension.buildId.map(::mappingIndexName)

        val generateTask = project.tasks.register(
            "${variantName}GenerateElasticsearchBulkRequestBody",
            GenerateElasticsearchBulkRequestBody::class.java,
        ) { task ->
            task.description = "Generates Elasticsearch bulk request body from the R8 mapping file for the $variantName variant."
            task.group = "elastic"
            task.mappingFile.set(variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE))
            task.indexName.set(indexName)
            task.requestBodyFile.set(
                project.layout.buildDirectory.file("intermediates/elastic/mapping/$variantName/requestbody.ndjson"),
            )
        }

        project.tasks.register(
            "${variantName}UploadMappingToElasticsearch",
            UploadMappingToElasticsearch::class.java,
        ) { task ->
            task.description = "Uploads R8 mapping to Elasticsearch for the $variantName variant."
            task.group = "elastic"
            task.requestBodyFile.set(generateTask.flatMap { it.requestBodyFile })
            task.endpoint.set(extension.mapping.elasticsearch.endpoint)
            task.apiKey.set(extension.mapping.elasticsearch.apiKey)
            task.indexName.set(indexName)
        }
    }

}

internal fun mappingIndexName(buildId: String): String {
    val indexName = "$INDEX_PREFIX$buildId"
    val invalidReason = when {
        buildId.isEmpty() -> "the build ID must not be empty"
        indexName != indexName.lowercase(Locale.ROOT) -> "the resulting index name must be lowercase"
        indexName.any { it.isWhitespace() || it in INVALID_INDEX_NAME_CHARACTERS } ->
            "the resulting index name contains a forbidden character"
        indexName.toByteArray(StandardCharsets.UTF_8).size > 255 ->
            "the resulting index name exceeds Elasticsearch's 255-byte limit"
        else -> return indexName
    }
    throw GradleException(
        "Invalid build ID for R8 mapping uploads: $invalidReason. " +
            "Configure elasticOtel.buildId with an Elasticsearch-compatible value.",
    )
}
