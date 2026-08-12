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
package co.elastic.otel.android.plugin.internal.mapping.upload

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@DisableCachingByDefault(because = "Uploads to a remote Elasticsearch instance")
internal abstract class UploadMappingToElasticsearch : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val requestBodyFile: RegularFileProperty

    @get:Input
    abstract val endpoint: Property<String>

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val indexName: Property<String>

    private val client by lazy { OkHttpClient() }
    private val moshi by lazy { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }

    @TaskAction
    fun upload() {
        val file = requestBodyFile.get().asFile
        if (!file.exists() || file.length() == 0L) {
            logger.lifecycle("No bulk request body found. Skipping upload.")
            return
        }

        val indexName = indexName.get()
        createIndex(indexName)
        uploadBulk(file)
    }

    private fun createIndex(indexName: String) {
        val base = endpoint.get().trimEnd('/')
        logger.lifecycle("Creating mapping index: $indexName")

        val adapter = moshi.adapter(CreateIndexRequest::class.java)
        val body = adapter.toJson(CREATE_INDEX_REQUEST)

        val request = Request.Builder()
            .url("$base/$indexName")
            .put(body.toRequestBody(JSON_MEDIA_TYPE))
            .header("Authorization", "ApiKey ${apiKey.get()}")
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                logger.lifecycle("Created mapping index: $indexName")
            } else {
                val responseBody = response.body.string()
                if (responseBody.contains("resource_already_exists_exception")) {
                    logger.lifecycle("Mapping index already exists: $indexName")
                } else {
                    throw GradleException(
                        "Failed to create mapping index: ${response.code}\n$responseBody",
                    )
                }
            }
        }
    }

    private fun uploadBulk(file: File) {
        val base = endpoint.get().trimEnd('/')
        val url = "$base/_bulk?_source=false"
        logger.lifecycle("Uploading mapping to: $url")

        val request = Request.Builder()
            .url(url)
            .post(file.asRequestBody(NDJSON_MEDIA_TYPE))
            .header("Authorization", "ApiKey ${apiKey.get()}")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val responseBody = response.body.string()
                throw GradleException(
                    "Failed to upload mapping to Elasticsearch: ${response.code}\n$responseBody",
                )
            }
            logger.lifecycle("Successfully uploaded mapping to Elasticsearch (${response.code})")
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
        private val NDJSON_MEDIA_TYPE = "application/x-ndjson".toMediaType()

        private val CREATE_INDEX_REQUEST = CreateIndexRequest(
            mappings = CreateIndexRequest.Mappings(
                dynamic = "strict",
                properties = CreateIndexRequest.Mappings.Properties(
                    schemaVersion = FieldType(type = "integer"),
                    mapVersion = FieldType(type = "keyword"),
                    obfuscatedClass = FieldType(type = "keyword"),
                    originalClass = FieldType(type = "keyword"),
                    sourceFile = FieldType(type = "keyword"),
                    methods = FieldType(type = "object", enabled = false),
                ),
            ),
        )
    }

    /**
     * Body sent on `PUT /<index>` when the per-build index does not exist
     * yet:
     *
     * - `dynamic: strict` rejects unexpected top-level fields.
     * - `methods` is `enabled: false` so its arbitrary structure is stored
     *   in `_source` but not indexed; new R8 extras flow through without
     *   any mapping change.
     */
    internal data class CreateIndexRequest(
        val mappings: Mappings,
    ) {
        data class Mappings(
            val dynamic: String,
            val properties: Properties,
        ) {
            data class Properties(
                @Json(name = "schema_version") val schemaVersion: FieldType,
                @Json(name = "map_version") val mapVersion: FieldType,
                @Json(name = "obfuscated_class") val obfuscatedClass: FieldType,
                @Json(name = "original_class") val originalClass: FieldType,
                @Json(name = "source_file") val sourceFile: FieldType,
                val methods: FieldType,
            )
        }
    }

    internal data class FieldType(
        val type: String,
        val enabled: Boolean? = null,
    )
}
