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

import java.nio.file.Path
import kotlin.io.path.writeText
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

class UploadMappingToElasticsearchTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `accepts a successful bulk response`() {
        assertDoesNotThrow {
            UploadMappingToElasticsearch.validateBulkResponse(
                """{"errors":false,"items":[{"index":{"_id":"class-id","status":201}}]}""",
            )
        }
    }

    @Test
    fun `reports bulk item failures`() {
        val exception = assertThrows<GradleException> {
            UploadMappingToElasticsearch.validateBulkResponse(
                """
                {
                  "errors": true,
                  "items": [
                    {
                      "index": {
                        "_id": "class-id",
                        "status": 400,
                        "error": {
                          "type": "mapper_parsing_exception",
                          "reason": "failed to parse field"
                        }
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )
        }

        assertTrue(exception.message!!.contains("index document class-id (status 400)"))
        assertTrue(exception.message!!.contains("mapper_parsing_exception: failed to parse field"))
    }

    @Test
    fun `rejects an invalid bulk response`() {
        val exception = assertThrows<GradleException> {
            UploadMappingToElasticsearch.validateBulkResponse("""{"items":[]}""")
        }

        assertTrue(exception.message!!.contains("Failed to parse Elasticsearch bulk response"))
    }

    @Test
    fun `uploads bounded batches without splitting action and document pairs`() {
        val pair = """{"index":{"_id":"class-id"}}""" + "\n" + """{"class":"example"}""" + "\n"
        val requestBody = tempDir.resolve("request.ndjson")
        requestBody.writeText(pair.repeat(3))
        val uploadedBodies = mutableListOf<String>()

        val batchCount = UploadMappingToElasticsearch.uploadBulkBatches(
            requestBody.toFile(),
            pair.toByteArray().size * 2,
        ) { body ->
            uploadedBodies.add(body.toString(Charsets.UTF_8))
            successfulResponse()
        }

        assertEquals(2, batchCount)
        assertEquals(listOf(pair.repeat(2), pair), uploadedBodies)
    }

    @Test
    fun `reports a failure from a later batch`() {
        val pair = """{"index":{"_id":"class-id"}}""" + "\n" + """{"class":"example"}""" + "\n"
        val requestBody = tempDir.resolve("request.ndjson")
        requestBody.writeText(pair.repeat(2))
        var batchNumber = 0

        val exception = assertThrows<GradleException> {
            UploadMappingToElasticsearch.uploadBulkBatches(
                requestBody.toFile(),
                pair.toByteArray().size,
            ) {
                batchNumber++
                if (batchNumber == 1) {
                    successfulResponse()
                } else {
                    UploadMappingToElasticsearch.BulkUploadResponse(
                        code = 200,
                        successful = true,
                        body = """
                            {
                              "errors": true,
                              "items": [
                                {
                                  "index": {
                                    "_id": "failed-class",
                                    "status": 400,
                                    "error": {
                                      "type": "mapper_parsing_exception",
                                      "reason": "failed to parse field"
                                    }
                                  }
                                }
                              ]
                            }
                        """.trimIndent(),
                    )
                }
            }
        }

        assertTrue(exception.message!!.contains("Failed to upload mapping batch 2"))
        assertTrue(exception.message!!.contains("mapper_parsing_exception"))
    }

    @Test
    fun `rejects a document larger than one batch`() {
        val pair = """{"index":{}}""" + "\n" + """{"class":"example"}""" + "\n"
        val requestBody = tempDir.resolve("request.ndjson")
        requestBody.writeText(pair)

        val exception = assertThrows<GradleException> {
            UploadMappingToElasticsearch.uploadBulkBatches(
                requestBody.toFile(),
                pair.toByteArray().size - 1,
            ) {
                successfulResponse()
            }
        }

        assertTrue(exception.message!!.contains("exceeding"))
    }

    private fun successfulResponse() = UploadMappingToElasticsearch.BulkUploadResponse(
        code = 200,
        successful = true,
        body = """{"errors":false,"items":[]}""",
    )
}
