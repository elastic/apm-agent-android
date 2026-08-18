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
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
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

    @get:Internal
    abstract val apiKey: Property<String>

    @get:Input
    abstract val indexName: Property<String>

    private val client by lazy { OkHttpClient() }
    private val moshi by lazy { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }

    @TaskAction
    fun upload() {
        val file = requestBodyFile.get().asFile
        validateRequestBodyFile(file)

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
            .header(PRODUCT_ORIGIN_HEADER, PRODUCT_ORIGIN)
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

        val batchCount = uploadBulkBatches(file, MAX_BULK_BATCH_SIZE_BYTES) { requestBody ->
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody(NDJSON_MEDIA_TYPE))
                .header("Authorization", "ApiKey ${apiKey.get()}")
                .header(PRODUCT_ORIGIN_HEADER, PRODUCT_ORIGIN)
                .build()

            client.newCall(request).execute().use { response ->
                BulkUploadResponse(
                    code = response.code,
                    successful = response.isSuccessful,
                    body = response.body.string(),
                )
            }
        }
        logger.lifecycle("Successfully uploaded mapping to Elasticsearch ($batchCount batches)")
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
        private val NDJSON_MEDIA_TYPE = "application/x-ndjson".toMediaType()
        private const val MAX_BULK_BATCH_SIZE_BYTES = 50 * 1024 * 1024

        // Serverless requires an Elastic product origin to access dot-prefixed indices.
        private const val PRODUCT_ORIGIN_HEADER = "X-Elastic-Product-Origin"
        private const val PRODUCT_ORIGIN = "observability"

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

        private val BULK_RESPONSE_ADAPTER = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
            .adapter(BulkResponse::class.java)

        internal fun validateRequestBodyFile(file: File) {
            if (!file.exists()) {
                throw GradleException("R8 mapping bulk request body does not exist: ${file.absolutePath}")
            }
            if (file.length() == 0L) {
                throw GradleException(
                    "R8 mapping bulk request body is empty; no mapping documents were generated",
                )
            }
        }

        internal fun uploadBulkBatches(
            file: File,
            maxBatchSizeBytes: Int,
            uploadBatch: (ByteArray) -> BulkUploadResponse,
        ): Int {
            require(maxBatchSizeBytes > 0) { "Bulk batch size must be greater than zero" }

            var batchNumber = 0
            val batch = ByteArrayOutputStream(minOf(maxBatchSizeBytes, DEFAULT_BUFFER_SIZE))

            fun sendBatch() {
                if (batch.size() == 0) {
                    return
                }

                batchNumber++
                val response = uploadBatch(batch.toByteArray())
                if (!response.successful) {
                    throw GradleException(
                        "Failed to upload mapping batch $batchNumber to Elasticsearch: " +
                            "${response.code}\n${response.body}",
                    )
                }
                try {
                    validateBulkResponse(response.body)
                } catch (exception: GradleException) {
                    throw GradleException(
                        "Failed to upload mapping batch $batchNumber: ${exception.message}",
                        exception,
                    )
                }
                batch.reset()
            }

            file.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                while (true) {
                    val actionLine = reader.readLine() ?: break
                    val documentLine = reader.readLine()
                        ?: throw GradleException(
                            "Invalid bulk request body: action line is missing its document line",
                        )
                    val pair = "$actionLine\n$documentLine\n".toByteArray(StandardCharsets.UTF_8)
                    if (pair.size > maxBatchSizeBytes) {
                        throw GradleException(
                            "A mapping document requires ${pair.size} bytes, exceeding the " +
                                "$maxBatchSizeBytes-byte bulk batch limit",
                        )
                    }
                    if (batch.size() > 0 && batch.size() + pair.size > maxBatchSizeBytes) {
                        sendBatch()
                    }
                    batch.write(pair)
                }
            }
            sendBatch()
            return batchNumber
        }

        internal fun validateBulkResponse(responseBody: String) {
            val bulkResponse = try {
                BULK_RESPONSE_ADAPTER.fromJson(responseBody)
            } catch (exception: IOException) {
                throw GradleException("Failed to parse Elasticsearch bulk response", exception)
            } catch (exception: JsonDataException) {
                throw GradleException("Failed to parse Elasticsearch bulk response", exception)
            } ?: throw GradleException("Elasticsearch returned an empty bulk response")

            if (!bulkResponse.errors) {
                return
            }

            val itemErrors = bulkResponse.items.flatMap { operations ->
                operations.mapNotNull { (operation, item) ->
                    item.error?.let { error ->
                        "$operation document ${item.id ?: "<unknown>"} " +
                            "(status ${item.status}): ${error.type}: ${error.reason}"
                    }
                }
            }
            val details = if (itemErrors.isEmpty()) {
                responseBody
            } else {
                itemErrors.joinToString(separator = "\n", limit = 5)
            }
            throw GradleException(
                "Elasticsearch failed to index one or more mapping documents:\n$details",
            )
        }
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

    internal data class BulkResponse(
        val errors: Boolean,
        val items: List<Map<String, BulkItem>> = emptyList(),
    )

    internal data class BulkItem(
        @Json(name = "_id") val id: String? = null,
        val status: Int,
        val error: BulkError? = null,
    )

    internal data class BulkError(
        val type: String,
        val reason: String,
    )

    internal data class BulkUploadResponse(
        val code: Int,
        val successful: Boolean,
        val body: String,
    )
}
