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

import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UploadMappingToElasticsearchTest {

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
}
