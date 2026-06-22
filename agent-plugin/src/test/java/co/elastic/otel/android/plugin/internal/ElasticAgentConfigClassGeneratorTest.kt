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

import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ElasticAgentConfigClassGeneratorTest {

    @Test
    fun `generated config class name matches the SDK reflection contract`() {
        assertEquals(
            "co.elastic.otel.android.internal.generated.ElasticAgentConfig",
            ElasticAgentConfigClassGenerator.GENERATED_CLASS_NAME,
        )
        assertEquals("BUILD_ID", ElasticAgentConfigClassGenerator.BUILD_ID_FIELD_NAME)
    }

    @Test
    fun `generated config class exposes build id at runtime`(@TempDir tempDir: Path) {
        ElasticAgentConfigClassGenerator.generate(tempDir.toFile(), "build-123")

        URLClassLoader(arrayOf(tempDir.toUri().toURL()), null).use { classLoader ->
            val configClass = classLoader.loadClass(ElasticAgentConfigClassGenerator.GENERATED_CLASS_NAME)
            val buildIdField = configClass.getField(ElasticAgentConfigClassGenerator.BUILD_ID_FIELD_NAME)

            assertTrue(Modifier.isPublic(buildIdField.modifiers))
            assertTrue(Modifier.isStatic(buildIdField.modifiers))
            assertTrue(Modifier.isFinal(buildIdField.modifiers))
            assertEquals("build-123", buildIdField.get(null))
        }
    }

    @Test
    fun `consumer proguard rules keep generated config class`() {
        val repoRoot = File(System.getProperty("user.dir")).resolve("../").canonicalFile
        val rules = repoRoot.resolve("shared-rules.pro").readText()

        assertTrue(
            rules.contains("-keep class co.elastic.otel.android.internal.generated.ElasticAgentConfig"),
        )
        assertTrue(
            rules.contains("public static final java.lang.String BUILD_ID;"),
        )
    }
}
