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
package co.elastic.otel.android.plugin.internal.mapping.parsing

import java.io.BufferedReader
import java.io.StringReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class R8MappingParserTest {

    private fun parse(input: String): R8Mapping {
        return R8MappingParser.parse(BufferedReader(StringReader(input)))
    }

    @Test
    fun `parse simple class with one method`() {
        val input = """
            com.example.MyClass -> a.b.C:
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        assertEquals(1, mapping.classMappings.size)

        val classMapping = mapping.classMappings[0]
        assertEquals("a.b.C", classMapping.obfuscatedName)
        assertEquals("com.example.MyClass", classMapping.originalName)
        assertEquals(1, classMapping.methodMappings.size)

        val methodMapping = classMapping.methodMappings["d"]!!
        assertEquals("d", methodMapping.obfuscatedName)
        assertEquals(1, methodMapping.lines.size)

        val line = methodMapping.lines[0]
        assertEquals("myMethod", line.value)
        assertEquals("0", line.obfuscatedLineNumberRange.start)
        assertEquals("5", line.obfuscatedLineNumberRange.end)
        assertEquals("42", line.originalLineNumberRange.start)
        assertEquals("42", line.originalLineNumberRange.end)
    }

    @Test
    fun `parse class with sourceFile comment`() {
        val input = """
            com.example.MyClass -> a.b.C:
            # {"id":"sourceFile","fileName":"MyClass.kt"}
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        val classMapping = mapping.classMappings[0]
        assertEquals(1, classMapping.extras.size)
        assertTrue(classMapping.extras[0].contains("sourceFile"))
        assertTrue(classMapping.extras[0].contains("MyClass.kt"))
    }

    @Test
    fun `plain comments are ignored`() {
        val input = """
            com.example.MyClass -> a.b.C:
            # not-json metadata
                0:5:void myMethod():42:42 -> d
                # also not json
        """.trimIndent()

        val mapping = parse(input)
        val classMapping = mapping.classMappings[0]
        assertTrue(classMapping.extras.isEmpty())
        assertTrue(classMapping.methodMappings.getValue("d").lines[0].extras.isEmpty())
    }

    @Test
    fun `non-object JSON comments are ignored`() {
        val input = """
            com.example.MyClass -> a.b.C:
            # ["not","an","r8","extra"]
                0:5:void myMethod():42:42 -> d
                # "not an r8 extra"
        """.trimIndent()

        val mapping = parse(input)
        val classMapping = mapping.classMappings[0]
        assertTrue(classMapping.extras.isEmpty())
        assertTrue(classMapping.methodMappings.getValue("d").lines[0].extras.isEmpty())
    }

    @Test
    fun `method comments attach to the preceding method line`() {
        val input = """
            com.example.MyClass -> a.b.C:
                1:6:int indexOf(java.lang.Object,int):94:94 -> e
                  # {"id":"com.android.tools.r8.residualsignature","signature":"(ILjava/lang/Object;)I"}
                7:15:int indexOf(java.lang.Object,int):100:100 -> e
        """.trimIndent()

        val lines = parse(input).classMappings[0].methodMappings.getValue("e").lines
        assertEquals(2, lines.size)
        assertEquals(
            listOf("""{"id":"com.android.tools.r8.residualsignature","signature":"(ILjava/lang/Object;)I"}"""),
            lines[0].extras,
        )
        assertTrue(lines[1].extras.isEmpty())
    }

    @Test
    fun `parse init and clinit methods`() {
        val input = """
            com.example.MyClass -> a.b.C:
                0:2:void <init>():10:10 -> <init>
                void <clinit>() -> <clinit>
        """.trimIndent()

        val mapping = parse(input)
        val classMapping = mapping.classMappings[0]
        assertEquals(2, classMapping.methodMappings.size)
        assertTrue(classMapping.methodMappings.containsKey("<init>"))
        assertTrue(classMapping.methodMappings.containsKey("<clinit>"))
    }

    @Test
    fun `parse multiple methods mapping to same obfuscated name`() {
        val input = """
            com.example.MyClass -> a.b.C:
                0:2:void methodA():10:10 -> a
                3:5:void methodB():20:25 -> a
        """.trimIndent()

        val mapping = parse(input)
        val methodMapping = mapping.classMappings[0].methodMappings["a"]!!
        assertEquals(2, methodMapping.lines.size)
        assertEquals("methodA", methodMapping.lines[0].value)
        assertEquals("methodB", methodMapping.lines[1].value)
    }

    @Test
    fun `fields are ignored and do not produce method mappings`() {
        val input = """
            com.example.MyClass -> a.b.C:
                java.lang.String name -> a
                int count -> b
        """.trimIndent()

        val mapping = parse(input)
        assertEquals(0, mapping.classMappings.size)
    }

    @Test
    fun `fields are ignored but methods are kept`() {
        val input = """
            com.example.MyClass -> a.b.C:
                java.lang.String name -> a
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        assertEquals(1, mapping.classMappings.size)
        assertEquals(1, mapping.classMappings[0].methodMappings.size)
        assertTrue(mapping.classMappings[0].methodMappings.containsKey("d"))
    }

    @Test
    fun `comments after fields are ignored and do not attach to the next method`() {
        val input = """
            com.example.MyClass -> a.b.C:
            # {"id":"sourceFile","fileName":"MyClass.kt"}
                java.lang.String name -> a
                  # {"id":"com.android.tools.r8.residualsignature","signature":"Ljava/lang/String;"}
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val classMapping = parse(input).classMappings[0]
        assertEquals(listOf("""{"id":"sourceFile","fileName":"MyClass.kt"}"""), classMapping.extras)
        assertTrue(classMapping.methodMappings.getValue("d").lines[0].extras.isEmpty())
    }

    @Test
    fun `empty input produces empty mapping`() {
        val mapping = parse("")
        assertTrue(mapping.classMappings.isEmpty())
    }

    @Test
    fun `header comments are ignored`() {
        val input = """
            # compiler: R8
            # compiler_version: 8.11.18
            # min_api: 26
            # {"id":"com.android.tools.r8.mapping","version":"2.2"}
            com.example.MyClass -> a.b.C:
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        assertEquals(1, mapping.classMappings.size)
    }

    @Test
    fun `mapVersion is captured from the file-level mapping comment`() {
        val input = """
            # compiler: R8
            # compiler_version: 8.11.18
            # {"id":"com.android.tools.r8.mapping","version":"2.2"}
            com.example.MyClass -> a.b.C:
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        assertEquals("2.2", mapping.mapVersion)
    }

    @Test
    fun `mapVersion is null when the file has no mapping version comment`() {
        // R8 < 8 / map-version 1.x predates the file-level version comment;
        // R8 itself defaults to map-version 1.0 in that case. We surface the
        // absence as null so the consumer can apply its own policy.
        val input = """
            com.example.MyClass -> a.b.C:
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        assertEquals(null, mapping.mapVersion)
    }

    @Test
    fun `mapVersion only matches the file-level mapping comment, not class-level extras`() {
        // A class whose `sourceFile` extras happens to contain the substring
        // `"version"` must NOT be misidentified as the file-level version.
        // We protect against that by requiring the parser to be still
        // pre-class when capturing the version.
        val input = """
            com.example.MyClass -> a.b.C:
            # {"id":"sourceFile","fileName":"version-7.kt"}
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        assertEquals(null, mapping.mapVersion)
    }

    @Test
    fun `mapVersion ignores invalid JSON comments`() {
        val input = """
            # {"id":"com.android.tools.r8.mapping","version":"2.2"
            com.example.MyClass -> a.b.C:
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        assertEquals(null, mapping.mapVersion)
    }

    @Test
    fun `parse multiple classes`() {
        val input = """
            com.example.ClassA -> a.A:
                0:5:void foo():10:10 -> a
            com.example.ClassB -> a.B:
                0:3:int bar():20:20 -> b
        """.trimIndent()

        val mapping = parse(input)
        assertEquals(2, mapping.classMappings.size)
        assertEquals("a.A", mapping.classMappings[0].obfuscatedName)
        assertEquals("a.B", mapping.classMappings[1].obfuscatedName)
    }

    @Test
    fun `parse method with original line range`() {
        val input = """
            com.example.MyClass -> a.b.C:
                0:10:void myMethod():42:50 -> d
        """.trimIndent()

        val mapping = parse(input)
        val line = mapping.classMappings[0].methodMappings["d"]!!.lines[0]
        assertEquals("42", line.originalLineNumberRange.start)
        assertEquals("50", line.originalLineNumberRange.end)
    }

    @Test
    fun `parse method without line numbers`() {
        val input = """
            com.example.MyClass -> a.b.C:
                void myMethod() -> d
        """.trimIndent()

        val mapping = parse(input)
        val line = mapping.classMappings[0].methodMappings["d"]!!.lines[0]
        assertEquals("", line.obfuscatedLineNumberRange.start)
        assertEquals("", line.obfuscatedLineNumberRange.end)
    }

    @Test
    fun `parse inner class with dollar sign`() {
        val input = """
            com.example.Outer${'$'}Inner -> a.b.C${'$'}D:
                0:5:void myMethod():42:42 -> d
        """.trimIndent()

        val mapping = parse(input)
        assertEquals("a.b.C\$D", mapping.classMappings[0].obfuscatedName)
        assertEquals("com.example.Outer\$Inner", mapping.classMappings[0].originalName)
    }
}
