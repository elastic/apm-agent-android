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

import co.elastic.otel.android.plugin.internal.mapping.parsing.ClassMapping
import co.elastic.otel.android.plugin.internal.mapping.parsing.MethodMapping
import co.elastic.otel.android.plugin.internal.mapping.parsing.R8Mapping
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedWriter
import java.io.StringWriter
import java.security.MessageDigest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NdjsonBulkWriterTest {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val docAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
    )

    @Test
    fun `one document per obfuscated class`() {
        val mapping = singleClass(
            obfuscated = "f8",
            original = "com.example.Crasher",
            sourceFile = "Crasher.kt",
            methods = mapOf(
                "b" to listOf(line("deepCrash", 1, 6, 28, 28)),
            ),
        )

        val doc = singleDocument(mapping)

        assertEquals(1, doc.schemaVersion)
        assertEquals("f8", doc.obfuscatedClass)
        assertEquals("com.example.Crasher", doc.originalClass)
        assertEquals("Crasher.kt", doc.sourceFile)
        assertEquals(setOf("b"), doc.methods.keys)
        val entries = doc.methods.getValue("b").mappings
        assertEquals(1, entries.size)
        val entry = entries[0]
        assertEquals(listOf(1, 6), range(entry, "obf_range"))
        assertEquals(listOf(28, 28), range(entry, "orig_range"))
        assertEquals("deepCrash", entry["method"])
        assertNull(entry["class"], "same-class entry omits class override")
        assertNull(entry["source_file"], "same-class entry omits source_file override")
        assertNull(doc.methods.getValue("b").raw["default_mappings"], "no default_mappings when all entries have ranges")

        val rawJson = nonEmptyLines(writeToString("idx", mapping))[1]
        assertTrue(rawJson.contains("\"obf_range\":[1,6]"), "ranges must serialize as plain integers")
    }

    @Test
    fun `multiple methods nest under methods object in same document`() {
        val mapping = singleClass(
            obfuscated = "a.A",
            original = "com.example.Foo",
            sourceFile = "Foo.kt",
            methods = mapOf(
                "a" to listOf(line("foo", 1, 5, 10, 10)),
                "b" to listOf(line("bar", 6, 10, 20, 20)),
            ),
        )

        val lines = nonEmptyLines(writeToString("idx", mapping))
        assertEquals(2, lines.size, "still one bulk action + one source doc per class")

        val doc = singleDocument(mapping)
        assertEquals(setOf("a", "b"), doc.methods.keys)
    }

    @Test
    fun `inline chain entries are separate elements ordered innermost-first`() {
        val mapping = singleClass(
            obfuscated = "d2",
            original = "androidx.collection.ArrayMap",
            sourceFile = "ArrayMap.java",
            methods = mapOf(
                "putAll" to listOf(
                    line("androidx.collection.SimpleArrayMap.ensureCapacity", 10, 14, 201, 201),
                    line("putAll", 10, 14, 100, 104),
                ),
            ),
        )

        val doc = singleDocument(mapping)
        val entries = doc.methods.getValue("putAll").mappings
        assertEquals(2, entries.size)
        assertEquals("ensureCapacity", entries[0]["method"])
        assertEquals("androidx.collection.SimpleArrayMap", entries[0]["class"])
        assertEquals("putAll", entries[1]["method"])
        assertNull(entries[1]["class"])
    }

    @Test
    fun `cross-class inlinee uses its own source_file when in mapping`() {
        val mapping = R8Mapping(
            listOf(
                ClassMapping(
                    obfuscatedName = "a.A",
                    originalName = "com.example.Outer",
                    methodMappings = mapOf(
                        "a" to MethodMapping(
                            "a",
                            listOf(
                                line("com.example.Inner.inlined", 5, 5, 10, 10),
                                line("caller", 5, 5, 20, 20),
                            ),
                        ),
                    ),
                    extras = listOf("""{"id":"sourceFile","fileName":"Outer.kt"}"""),
                ),
                ClassMapping(
                    obfuscatedName = "b.B",
                    originalName = "com.example.Inner",
                    methodMappings = emptyMap(),
                    extras = listOf("""{"id":"sourceFile","fileName":"Inner.kt"}"""),
                ),
            ),
        )

        val outerDoc = documentFor(mapping, "a.A")
        val inlineeEntry = outerDoc.methods.getValue("a").mappings[0]
        assertEquals("com.example.Inner", inlineeEntry["class"])
        assertEquals("Inner.kt", inlineeEntry["source_file"], "inlinee in mapping → use its own source file")

        val callerEntry = outerDoc.methods.getValue("a").mappings[1]
        assertNull(callerEntry["class"])
        assertNull(callerEntry["source_file"])
    }

    @Test
    fun `cross-class inlinee not in mapping omits source_file override`() {
        val mapping = singleClass(
            obfuscated = "a.A",
            original = "com.example.Outer",
            sourceFile = "Outer.kt",
            methods = mapOf(
                "a" to listOf(line("kotlin.collections.CollectionsKt.mutableListOf", 5, 5, 10, 10)),
            ),
        )

        val entry = singleDocument(mapping).methods.getValue("a").mappings[0]
        assertEquals("kotlin.collections.CollectionsKt", entry["class"])
        assertNull(entry["source_file"], "inlinee not in mapping → no source_file override; retracer infers")
    }

    @Test
    fun `entries without line numbers go to default_mappings`() {
        val mapping = singleClass(
            obfuscated = "a.A",
            original = "com.example.Foo",
            sourceFile = "Foo.kt",
            methods = mapOf(
                "a" to listOf(noRangeLine("foo")),
            ),
        )

        val method = singleDocument(mapping).methods.getValue("a")
        assertTrue(method.mappings.isEmpty())
        val defaults = method.defaultMappings()
        assertEquals(1, defaults.size)
        assertEquals("foo", defaults[0]["method"])
        assertNull(defaults[0]["class"], "same-class default omits class override")
        assertNull(defaults[0]["orig_range"], "no orig_range when source mapping has none")
    }

    @Test
    fun `default_mappings entries preserve original line range when present`() {
        val mapping = singleClass(
            obfuscated = "a.A",
            original = "com.example.Foo",
            sourceFile = "Foo.kt",
            methods = mapOf(
                "a" to listOf(noRangeLineWithOrig("foo", 42, 42)),
            ),
        )

        val defaults = singleDocument(mapping).methods.getValue("a").defaultMappings()
        assertEquals(1, defaults.size)
        assertEquals("foo", defaults[0]["method"])
        assertEquals(listOf(42, 42), range(defaults[0], "orig_range"))
    }

    @Test
    fun `mixed entries split between mappings and default_mappings`() {
        val mapping = singleClass(
            obfuscated = "a.A",
            original = "com.example.Foo",
            sourceFile = "Foo.kt",
            methods = mapOf(
                "a" to listOf(
                    line("bar", 1, 5, 10, 14),
                    noRangeLine("baz"),
                ),
            ),
        )

        val method = singleDocument(mapping).methods.getValue("a")
        assertEquals(1, method.mappings.size)
        assertEquals("bar", method.mappings[0]["method"])
        assertEquals(listOf("baz"), method.defaultMappings().map { it["method"] })
    }

    @Test
    fun `duplicate no-range entries are deduplicated`() {
        val mapping = singleClass(
            obfuscated = "a.A",
            original = "com.example.Foo",
            sourceFile = "Foo.kt",
            methods = mapOf(
                "a" to listOf(noRangeLine("foo"), noRangeLine("foo")),
            ),
        )

        val defaults = singleDocument(mapping).methods.getValue("a").defaultMappings()
        assertEquals(1, defaults.size)
        assertEquals("foo", defaults[0]["method"])
    }

    @Test
    fun `r8 extras are forwarded as native JSON objects`() {
        val mapping = R8Mapping(
            listOf(
                ClassMapping(
                    obfuscatedName = "a.A",
                    originalName = "com.example.Foo",
                    methodMappings = mapOf(
                        "a" to MethodMapping(
                            "a",
                            listOf(
                                MethodMapping.Line(
                                    value = "foo",
                                    obfuscatedLineNumberRange = MethodMapping.Line.Range("1", "5"),
                                    originalLineNumberRange = MethodMapping.Line.Range("10", "10"),
                                    extras = listOf(
                                        """{"id":"com.android.tools.r8.outline"}""",
                                        """{"id":"com.android.tools.r8.rewriteFrame","conditions":["throws(Ljava/lang/NullPointerException;)"],"actions":["removeInnerFrames(1)"]}""",
                                    ),
                                ),
                            ),
                        ),
                    ),
                    extras = emptyList(),
                ),
            ),
        )

        val entry = singleDocument(mapping).methods.getValue("a").mappings[0]
        @Suppress("UNCHECKED_CAST")
        val extras = entry["extras"] as List<Map<String, Any?>>
        assertEquals(2, extras.size)
        assertEquals("com.android.tools.r8.outline", extras[0]["id"])
        assertEquals("com.android.tools.r8.rewriteFrame", extras[1]["id"])
        assertEquals(listOf("throws(Ljava/lang/NullPointerException;)"), extras[1]["conditions"])
        assertEquals(listOf("removeInnerFrames(1)"), extras[1]["actions"])

        val rawJson = nonEmptyLines(writeToString("idx", mapping))[1]
        assertFalse(rawJson.contains("base64"), "extras must not be base64-encoded")
    }

    @Test
    fun `outlineCallsite positions preserve integer values`() {
        val mapping = R8Mapping(
            listOf(
                ClassMapping(
                    obfuscatedName = "s2",
                    originalName = "com.example.Outliner",
                    methodMappings = mapOf(
                        "a" to MethodMapping(
                            "a",
                            listOf(
                                MethodMapping.Line(
                                    value = "caller",
                                    obfuscatedLineNumberRange = MethodMapping.Line.Range("27", "27"),
                                    originalLineNumberRange = MethodMapping.Line.Range("0", "0"),
                                    extras = listOf("""{"id":"com.android.tools.r8.outlineCallsite","positions":{"1":4,"2":5},"outline":"Ls2;a(II)I"}"""),
                                ),
                            ),
                        ),
                    ),
                    extras = emptyList(),
                ),
            ),
        )

        val rawJson = nonEmptyLines(writeToString("idx", mapping))[1]
        assertTrue(rawJson.contains("\"positions\":{\"1\":4,\"2\":5}"), "positions must serialize as integers, not 4.0/5.0")
    }

    @Test
    fun `doc id is sha256 of obfuscated class`() {
        val mapping = singleClass(
            obfuscated = "x",
            original = "com.example.X",
            sourceFile = null,
            methods = mapOf("a" to listOf(line("foo", 1, 1, 1, 1))),
        )

        val lines = nonEmptyLines(writeToString(".android-r8-mappings-mybuild", mapping))
        val expectedId = sha256("x")
        assertTrue(lines[0].contains("\"_id\":\"$expectedId\""))
        assertTrue(expectedId.matches(Regex("[0-9a-f]{64}")), "doc id should be a 64-char hex string")
        assertFalse(lines[1].contains("build_id"), "doc must not contain build_id field")
    }

    @Test
    fun `bulk action targets the per-build index`() {
        val mapping = singleClass(
            obfuscated = "x",
            original = "com.example.X",
            sourceFile = null,
            methods = mapOf("a" to listOf(line("foo", 1, 1, 1, 1))),
        )

        val lines = nonEmptyLines(writeToString(".android-r8-mappings-abc123", mapping))
        assertTrue(lines[0].contains("\"_index\":\".android-r8-mappings-abc123\""))
    }

    @Test
    fun `class with no source file omits the field`() {
        val mapping = singleClass(
            obfuscated = "a.A",
            original = "com.example.Foo",
            sourceFile = null,
            methods = mapOf("a" to listOf(line("foo", 1, 1, 10, 10))),
        )

        val raw = singleDocumentRaw(mapping)
        assertNull(raw["source_file"])
    }

    @Test
    fun `empty mapping produces no output`() {
        val output = writeToString("idx", R8Mapping(emptyList()))
        assertEquals("", output)
    }

    @Test
    fun `concrete identity mapping (no orig range) is materialized as orig_range == obf_range`() {
        // R8 emits "44:44:method() -> a" for *concrete identity* mappings: the
        // original line equals the obfuscated line. The parser yields empty
        // strings for the original range; the writer must not crash and must
        // produce a faithful identity range so the retracer can interpolate it
        // like any other entry.
        val mapping = R8Mapping(
            listOf(
                ClassMapping(
                    obfuscatedName = "l1",
                    originalName = "androidx.startup.AppInitializer",
                    methodMappings = mapOf(
                        "<init>" to MethodMapping(
                            "<init>",
                            listOf(
                                MethodMapping.Line(
                                    value = "getLogger",
                                    obfuscatedLineNumberRange = MethodMapping.Line.Range("44", "44"),
                                    originalLineNumberRange = MethodMapping.Line.Range("", ""),
                                    extras = emptyList(),
                                ),
                            ),
                        ),
                    ),
                    extras = emptyList(),
                ),
            ),
        )

        val entry = singleDocument(mapping).methods.getValue("<init>").mappings[0]
        assertEquals(listOf(44, 44), range(entry, "obf_range"))
        assertEquals(listOf(44, 44), range(entry, "orig_range"), "identity mapping must materialize orig_range = obf_range")
    }

    @Test
    fun `concrete identity mapping (no orig range) preserves multi-line obf range`() {
        // "1:5:method() -> a" — identity over the whole obf range.
        val mapping = R8Mapping(
            listOf(
                ClassMapping(
                    obfuscatedName = "l1",
                    originalName = "androidx.startup.AppInitializer",
                    methodMappings = mapOf(
                        "a" to MethodMapping(
                            "a",
                            listOf(
                                MethodMapping.Line(
                                    value = "doStuff",
                                    obfuscatedLineNumberRange = MethodMapping.Line.Range("1", "5"),
                                    originalLineNumberRange = MethodMapping.Line.Range("", ""),
                                    extras = emptyList(),
                                ),
                            ),
                        ),
                    ),
                    extras = emptyList(),
                ),
            ),
        )

        val entry = singleDocument(mapping).methods.getValue("a").mappings[0]
        assertEquals(listOf(1, 5), range(entry, "obf_range"))
        assertEquals(listOf(1, 5), range(entry, "orig_range"))
    }

    @Test
    fun `map_version from R8Mapping is forwarded onto every ClassDocument`() {
        // R8's file-level mapping comment carries a `version` (e.g. "2.2").
        // We propagate it to every per-class document so the consumer can
        // reject documents whose map-version it does not understand,
        // protecting it from breaking-change R8 releases.
        val mapping = R8Mapping(
            listOf(
                ClassMapping(
                    obfuscatedName = "a",
                    originalName = "com.example.A",
                    methodMappings = mapOf("a" to MethodMapping("a", listOf(line("foo", 1, 1, 10, 10)))),
                    extras = emptyList(),
                ),
                ClassMapping(
                    obfuscatedName = "b",
                    originalName = "com.example.B",
                    methodMappings = mapOf("b" to MethodMapping("b", listOf(line("bar", 1, 1, 20, 20)))),
                    extras = emptyList(),
                ),
            ),
            mapVersion = "2.2",
        )

        val output = nonEmptyLines(writeToString("idx", mapping))
        // Every source line carries the same version.
        assertEquals("2.2", documentFor(mapping, "a").raw["map_version"])
        assertEquals("2.2", documentFor(mapping, "b").raw["map_version"])
        // And the JSON serialization spells it as `map_version`, not the
        // Kotlin field name.
        assertTrue(output[1].contains("\"map_version\":\"2.2\""))
    }

    @Test
    fun `null optional fields are omitted from JSON output`() {
        // sourceFile = null → document-level source_file omitted
        // same-class entry → class, source_file, extras on the entry omitted
        // mapVersion = null → map_version omitted
        val mapping = singleClass(
            obfuscated = "a",
            original = "com.example.A",
            sourceFile = null,
            methods = mapOf("a" to listOf(line("foo", 1, 1, 10, 10))),
        )

        val rawJson = nonEmptyLines(writeToString("idx", mapping))[1]
        assertFalse(rawJson.contains(":null"), "null fields must be omitted, not written as null")
        assertFalse(rawJson.contains("map_version"), "absent map_version must not appear as a key at all")
        assertFalse(rawJson.contains("source_file"), "absent source_file must not appear as a key at all")
    }

    @Test
    fun `synthesized marker on an entry is forwarded in extras`() {
        val mapping = R8Mapping(
            listOf(
                ClassMapping(
                    obfuscatedName = "a",
                    originalName = "com.example.Foo",
                    methodMappings = mapOf(
                        "a" to MethodMapping(
                            "a",
                            listOf(
                                MethodMapping.Line(
                                    value = "syntheticMethod",
                                    obfuscatedLineNumberRange = MethodMapping.Line.Range("1", "1"),
                                    originalLineNumberRange = MethodMapping.Line.Range("10", "10"),
                                    extras = listOf("""{"id":"com.android.tools.r8.synthesized"}"""),
                                ),
                            ),
                        ),
                    ),
                    extras = emptyList(),
                ),
            ),
        )

        val entry = singleDocument(mapping).methods.getValue("a").mappings[0]
        @Suppress("UNCHECKED_CAST")
        val extras = entry["extras"] as List<Map<String, Any?>>
        assertEquals("com.android.tools.r8.synthesized", extras[0]["id"])
    }

    private fun writeToString(indexName: String, mapping: R8Mapping): String {
        val stringWriter = StringWriter()
        BufferedWriter(stringWriter).use { NdjsonBulkWriter.write(it, indexName, mapping) }
        return stringWriter.toString()
    }

    private fun nonEmptyLines(output: String): List<String> = output.lines().filter { it.isNotEmpty() }

    private fun singleDocument(mapping: R8Mapping): ParsedDocument {
        val raw = singleDocumentRaw(mapping)
        return ParsedDocument(raw)
    }

    private fun singleDocumentRaw(mapping: R8Mapping): Map<String, Any?> {
        val lines = nonEmptyLines(writeToString("idx", mapping))
        assertEquals(2, lines.size, "expected one bulk action + one source line")
        return docAdapter.fromJson(lines[1]) ?: error("Cannot parse document")
    }

    private fun documentFor(mapping: R8Mapping, obfuscatedClass: String): ParsedDocument {
        val lines = nonEmptyLines(writeToString("idx", mapping))
        // Documents come in pairs (action, source). Find the source line whose obfuscated_class matches.
        val sourceLines = lines.filterIndexed { index, _ -> index % 2 == 1 }
        for (line in sourceLines) {
            val parsed = docAdapter.fromJson(line) ?: continue
            if (parsed["obfuscated_class"] == obfuscatedClass) return ParsedDocument(parsed)
        }
        error("No document found for obfuscated_class=$obfuscatedClass")
    }

    private fun singleClass(
        obfuscated: String,
        original: String,
        sourceFile: String?,
        methods: Map<String, List<MethodMapping.Line>>,
    ): R8Mapping {
        val classExtras = if (sourceFile != null) {
            listOf("""{"id":"sourceFile","fileName":"$sourceFile"}""")
        } else {
            emptyList()
        }
        return R8Mapping(
            listOf(
                ClassMapping(
                    obfuscatedName = obfuscated,
                    originalName = original,
                    methodMappings = methods.mapValues { (name, lines) -> MethodMapping(name, lines) },
                    extras = classExtras,
                ),
            ),
        )
    }

    private fun line(
        value: String,
        obfStart: Int,
        obfEnd: Int,
        origStart: Int,
        origEnd: Int,
    ): MethodMapping.Line {
        return MethodMapping.Line(
            value = value,
            obfuscatedLineNumberRange = MethodMapping.Line.Range(obfStart.toString(), obfEnd.toString()),
            originalLineNumberRange = MethodMapping.Line.Range(origStart.toString(), origEnd.toString()),
            extras = emptyList(),
        )
    }

    private fun noRangeLine(value: String): MethodMapping.Line {
        return MethodMapping.Line(
            value = value,
            obfuscatedLineNumberRange = MethodMapping.Line.Range("", ""),
            originalLineNumberRange = MethodMapping.Line.Range("", ""),
            extras = emptyList(),
        )
    }

    /**
     * Mirrors R8 entries shaped like `void method1():42:42 -> a`: no
     * obfuscated range, but with an original range that the schema must
     * carry so the retracer can surface it on rangeless frames.
     */
    private fun noRangeLineWithOrig(
        value: String,
        origStart: Int,
        origEnd: Int,
    ): MethodMapping.Line {
        return MethodMapping.Line(
            value = value,
            obfuscatedLineNumberRange = MethodMapping.Line.Range("", ""),
            originalLineNumberRange = MethodMapping.Line.Range(origStart.toString(), origEnd.toString()),
            extras = emptyList(),
        )
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    /**
     * Moshi's generic `Any` adapter parses every JSON number as a `Double`
     * — even though the writer emits integers — so test code reads ranges
     * back through this normalizer to compare against `Int` literals.
     */
    private fun range(entry: Map<String, Any?>, key: String): List<Int> {
        @Suppress("UNCHECKED_CAST")
        val list = entry[key] as List<Number>
        return list.map { it.toInt() }
    }

    private inner class ParsedDocument(val raw: Map<String, Any?>) {
        val schemaVersion: Int get() = (raw["schema_version"] as Number).toInt()
        val obfuscatedClass: String get() = raw["obfuscated_class"] as String
        val originalClass: String get() = raw["original_class"] as String
        val sourceFile: String? get() = raw["source_file"] as String?

        val methods: Map<String, MethodView>
            get() {
                @Suppress("UNCHECKED_CAST")
                val rawMethods = raw["methods"] as Map<String, Map<String, Any?>>
                return rawMethods.mapValues { (_, body) -> MethodView(body) }
            }
    }

    private inner class MethodView(val raw: Map<String, Any?>) {
        @Suppress("UNCHECKED_CAST")
        val mappings: List<Map<String, Any?>> get() = raw["mappings"] as? List<Map<String, Any?>> ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        fun defaultMappings(): List<Map<String, Any?>> =
            raw["default_mappings"] as? List<Map<String, Any?>> ?: emptyList()
    }

}
