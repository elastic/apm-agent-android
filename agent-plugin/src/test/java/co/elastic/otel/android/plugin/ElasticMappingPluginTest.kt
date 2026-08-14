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

import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ElasticMappingPluginTest {

    @Test
    fun `creates an index name from a valid build id`() {
        assertEquals(
            ".android-r8-mappings-release-1.2.3",
            mappingIndexName("release-1.2.3"),
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "Release", "release build", "release/build", "release#1"])
    fun `rejects build ids that produce invalid Elasticsearch index names`(buildId: String) {
        val exception = assertThrows<GradleException> {
            mappingIndexName(buildId)
        }

        assertTrue(exception.message!!.contains("Invalid build ID for R8 mapping uploads"))
    }

    @Test
    fun `rejects build ids that exceed the Elasticsearch index name limit`() {
        val exception = assertThrows<GradleException> {
            mappingIndexName("a".repeat(256))
        }

        assertTrue(exception.message!!.contains("255-byte limit"))
    }
}
