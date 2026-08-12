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
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedWriter
import java.security.MessageDigest

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
/**
 * Emits the upload payload for a parsed R8 mapping as Elasticsearch bulk
 * NDJSON: one `index` action and one source document per **obfuscated
 * class**.
 *
 * This class is internal and is not for public use. Its APIs are unstable.
 */
internal object NdjsonBulkWriter {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val actionAdapter = moshi.adapter(BulkAction::class.java)
    private val documentAdapter = moshi.adapter(ClassDocument::class.java)
    private val extrasObjectAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
    )

    /**
     * Serialises [mapping] into ES `_bulk` NDJSON: two lines per class —
     * the action header and the source document.
     *
     * Input — a parsed R8 mapping. For example, given a `mapping.txt` like:
     * ```
     * # {"id":"com.android.tools.r8.mapping","version":"2.2"}
     * com.example.Foo -> a.a:
     * # {"id":"sourceFile","fileName":"Foo.kt"}
     *     1:1:void greet():12:12 -> a
     * ```
     *
     * Output — two NDJSON lines written to [writer]:
     * ```
     * {"index":{"_index":".android-r8-mappings-<buildId>","_id":"<sha256(a.a)>"}}
     * {"schema_version":1,"map_version":"2.2","obfuscated_class":"a.a","original_class":"com.example.Foo","source_file":"Foo.kt","methods":{"a":{"mappings":[{"obf_range":[1,1],"orig_range":[12,12],"method":"greet"}]}}}
     * ```
     *
     * Null optional fields (`class`, `source_file`, `extras` on entries;
     * `default_mappings` on methods; `map_version` and `source_file` on the
     * document) are omitted entirely rather than serialized as `null`, keeping
     * the payload compact.
     *
     * `_id` is `sha256(obfuscated_class)` so re-running the upload is an
     * idempotent upsert (same class name → same `_id` → ES replaces the
     * document in place; doc count does not grow).
     *
     * @param writer destination for the NDJSON payload (one bulk file per build).
     * @param indexName per-build mapping index (e.g.
     *     `.android-r8-mappings-<sha256(applicationId-versionName-versionCode)>`).
     * @param mapping parsed R8 mapping; its [R8Mapping.mapVersion] is echoed
     *     into every document so the retracer can gate by R8 map version.
     */
    fun write(writer: BufferedWriter, indexName: String, mapping: R8Mapping) {
        // Pre-index by *original* (un-obfuscated) class name so that
        // cross-class inline lookups in `resolveOriginalCall` are O(1).
        val classByOriginalName = mapping.classMappings.associateBy { it.originalName }
        for (classMapping in mapping.classMappings) {
            val document = buildClassDocument(classMapping, mapping.mapVersion, classByOriginalName)
            // Action header: `index` (not `create`) with explicit `_id`
            // gives upsert-by-id semantics — see KDoc above.
            writer.write(actionAdapter.toJson(BulkAction(BulkAction.Index(indexName, sha256(document.obfuscatedClass)))))
            writer.newLine()
            writer.write(documentAdapter.toJson(document))
            writer.newLine()
        }
    }

    /**
     * Folds a single parsed [ClassMapping] into a wire-shape [ClassDocument]
     * (one ES document).
     *
     * Input — a `ClassMapping` such as:
     * ```
     * ClassMapping(
     *     obfuscatedName  = "a.a",
     *     originalName    = "com.example.Foo",
     *     methodMappings  = mapOf("a" to MethodMapping(...)),
     *     extras          = listOf("{\"id\":\"sourceFile\",\"fileName\":\"Foo.kt\"}"),
     * )
     * ```
     *
     * Output — a `ClassDocument`:
     * ```
     * ClassDocument(
     *     schemaVersion   = 1,
     *     mapVersion      = "2.2",
     *     obfuscatedClass = "a.a",
     *     originalClass   = "com.example.Foo",
     *     sourceFile      = "Foo.kt",      // promoted from class extras
     *     methods         = { "a" -> MethodDocument(...) },
     * )
     * ```
     *
     * Why the source file is promoted to the document level: most lines in
     * a class share the same source file, so storing it once on the class
     * (and only overriding it per-entry on cross-class inlines) keeps both
     * the document and the retracer simple — the retracer first looks at
     * the entry, then falls back to the class, then to its own inference.
     */
    private fun buildClassDocument(
        classMapping: ClassMapping,
        mapVersion: String?,
        classByOriginalName: Map<String, ClassMapping>,
    ): ClassDocument {
        // Extract the class-level source file once; passed down so per-entry
        // overrides only fire when the inlinee's source file truly differs.
        val sourceFile = extractSourceFile(classMapping.extras)
        val methods = classMapping.methodMappings.entries.associate { (obfMethod, methodMapping) ->
            obfMethod to buildMethodDocument(classMapping, methodMapping, sourceFile, classByOriginalName)
        }
        return ClassDocument(
            mapVersion = mapVersion,
            obfuscatedClass = classMapping.obfuscatedName,
            originalClass = classMapping.originalName,
            sourceFile = sourceFile,
            methods = methods,
        )
    }

    /**
     * Folds a single obfuscated method's mapping lines into a
     * [MethodDocument], splitting them into ranged entries (lookup by
     * obfuscated line) and "default" entries (no obfuscated range — used as
     * a fallback when the obfuscated frame has no line number).
     *
     * Input — a `MethodMapping` like:
     * ```
     * MethodMapping(
     *     obfuscatedName = "a",
     *     lines = listOf(
     *         Line(value="greet", obf=Range("1","1"),  orig=Range("12","12"), extras=[]),  // ranged
     *         Line(value="log",   obf=Range("","" ),   orig=Range("",""),     extras=[]),  // no range
     *     ),
     * )
     * ```
     *
     * Output — a `MethodDocument`:
     * ```
     * MethodDocument(
     *     mappings = [
     *         MappingEntry(obfRange=[1,1], origRange=[12,12], method="greet",
     *                      originalClass=null, sourceFile=null, extras=null)
     *     ],
     *     defaultMappings = [
     *         DefaultMappingEntry(method="log", originalClass=null,
     *                             sourceFile=null, origRange=null)
     *     ],
     * )
     * ```
     *
     * Default-mappings is `null` (not `[]`) when empty, so the JSON omits
     * the field entirely for the common case where every line has a range.
     */
    private fun buildMethodDocument(
        enclosingClass: ClassMapping,
        methodMapping: MethodMapping,
        enclosingSourceFile: String?,
        classByOriginalName: Map<String, ClassMapping>,
    ): MethodDocument {
        val ranged = mutableListOf<MappingEntry>()
        // `linkedSetOf` keeps deterministic order AND deduplicates: R8 can
        // legitimately emit two identical no-range lines (e.g. inlined
        // synthetic accessors) and we only want one entry in the document.
        val noRange = linkedSetOf<DefaultMappingEntry>()

        for (line in methodMapping.lines) {
            val resolved = resolveOriginalCall(enclosingClass, line.value, enclosingSourceFile, classByOriginalName)
            // Obfuscated-side range presence is the discriminator: a ranged
            // entry can be looked up by `obf_line`; a rangeless entry can
            // only be surfaced as a fallback.
            if (line.obfuscatedLineNumberRange.start.isNotEmpty()) {
                ranged.add(buildMappingEntry(line, resolved))
            } else {
                noRange.add(
                    DefaultMappingEntry(
                        method = resolved.method,
                        originalClass = resolved.classOverride,
                        sourceFile = resolved.sourceFileOverride,
                        origRange = buildOrigRange(line.originalLineNumberRange),
                    ),
                )
            }
        }

        return MethodDocument(
            mappings = ranged,
            defaultMappings = if (noRange.isEmpty()) null else noRange.toList(),
        )
    }

    /**
     * Returns the original line range as `[start, end]` ints, or `null` if
     * the source mapping had no original range (e.g. `void foo() -> a`).
     * Used by `default_mappings` entries that the retracer surfaces when
     * the obfuscated frame has no matching ranged entry.
     *
     * Input → output examples:
     * ```
     * Range("12", "15")  -> [12, 15]
     * Range("12", "12")  -> [12, 12]   // single-line mapping
     * Range("",   ""  )  -> null       // R8 emitted no original range
     * ```
     */
    private fun buildOrigRange(range: MethodMapping.Line.Range): List<Int>? {
        if (range.start.isEmpty()) return null
        return listOf(range.start.toInt(), range.end.toInt())
    }

    /**
     * Builds one ranged [MappingEntry] from a single mapping-file line.
     *
     * Input — a `Line` plus the resolved call:
     * ```
     * Line(
     *     value = "greet",
     *     obfuscatedLineNumberRange = Range("3", "5"),
     *     originalLineNumberRange   = Range("12", "14"),
     *     extras = [],
     * )
     * ResolvedCall(method="greet", classOverride=null, sourceFileOverride=null)
     * ```
     *
     * Output:
     * ```
     * MappingEntry(
     *     obfRange      = [3, 5],
     *     origRange     = [12, 14],
     *     method        = "greet",
     *     originalClass = null,
     *     sourceFile    = null,
     *     extras        = null,
     * )
     * ```
     *
     * The "concrete identity" branch handles R8 lines like:
     * ```
     * 44:44:method() -> a
     * ```
     * where R8 omits the original range because *original line == obfuscated
     * line*. We materialise it as `orig_range == obf_range` so the retracer
     * can use a single interpolation path for every entry.
     */
    private fun buildMappingEntry(line: MethodMapping.Line, resolved: ResolvedCall): MappingEntry {
        val obfStart = line.obfuscatedLineNumberRange.start.toInt()
        val obfEnd = line.obfuscatedLineNumberRange.end.toInt()
        // Concrete identity fallback: when R8 omits the original range, we
        // mirror obf_range so the retracer always sees a complete range pair.
        val origStart = line.originalLineNumberRange.start.ifEmpty { line.obfuscatedLineNumberRange.start }.toInt()
        val origEnd = line.originalLineNumberRange.end.ifEmpty { line.obfuscatedLineNumberRange.end }.toInt()
        return MappingEntry(
            obfRange = listOf(obfStart, obfEnd),
            origRange = listOf(origStart, origEnd),
            method = resolved.method,
            originalClass = resolved.classOverride,
            sourceFile = resolved.sourceFileOverride,
            extras = parseExtras(line.extras),
        )
    }

    /**
     * Splits an R8 line value into its (class override, method, source-file
     * override) parts. The `class` and `source_file` fields are null when
     * they would just repeat the enclosing class document — the retracer
     * already falls back to the document-level values.
     *
     * Input → output examples (assuming the enclosing class document has
     * `original_class = "com.example.Foo"` and `source_file = "Foo.kt"`):
     *
     * ```
     * rawValue = "greet"
     *   -> ResolvedCall("greet", classOverride=null, sourceFileOverride=null)
     *      Same-class reference; document defaults apply.
     *
     * rawValue = "com.example.Foo.greet"
     *   -> ResolvedCall("greet", classOverride=null, sourceFileOverride=null)
     *      Self-reference: the inlinee class equals the enclosing class.
     *
     * rawValue = "com.example.Inlinee.helper"
     *   -> ResolvedCall("helper", classOverride="com.example.Inlinee",
     *                   sourceFileOverride="Inlinee.kt")
     *      Cross-class inline: emit overrides because both class and source
     *      file differ from the enclosing document.
     *
     * rawValue = "com.example.SameFileInlinee.helper"   // sourceFile = "Foo.kt"
     *   -> ResolvedCall("helper", classOverride="com.example.SameFileInlinee",
     *                   sourceFileOverride=null)
     *      Cross-class but same source file (multi-class .kt file): keep
     *      the class override, drop the source-file override.
     *
     * rawValue = "kotlin.collections.ArraysKt.first"
     *   -> ResolvedCall("first", classOverride="kotlin.collections.ArraysKt",
     *                   sourceFileOverride=null)
     *      Inlinee class is not in the mapping (e.g. stdlib): emit no
     *      source-file override; the retracer falls back to its own
     *      inference (typically `<ClassName>.kt`).
     * ```
     */
    private fun resolveOriginalCall(
        enclosingClass: ClassMapping,
        rawValue: String,
        enclosingSourceFile: String?,
        classByOriginalName: Map<String, ClassMapping>,
    ): ResolvedCall {
        // No dot → bare method name, same class. Fast path.
        if ('.' !in rawValue) {
            return ResolvedCall(method = rawValue, classOverride = null, sourceFileOverride = null)
        }

        // Split on the *last* dot: everything before is the FQ class, the
        // suffix is the method. R8 uses the same convention.
        val lastDot = rawValue.lastIndexOf('.')
        val inlineeClass = rawValue.substring(0, lastDot)
        val methodName = rawValue.substring(lastDot + 1)

        // Self-reference: the value spells out the enclosing class. No
        // overrides — the document-level fields already match.
        if (inlineeClass == enclosingClass.originalName) {
            return ResolvedCall(method = methodName, classOverride = null, sourceFileOverride = null)
        }

        // Look up the inlinee's own source-file extra. `null` here means
        // the inlinee class is not in this mapping (e.g. Kotlin stdlib).
        val inlineeSourceFile = classByOriginalName[inlineeClass]?.let { extractSourceFile(it.extras) }
        // Suppress the override when it would be redundant with the
        // enclosing document's source file (multiple classes per .kt).
        val sourceFileOverride = inlineeSourceFile?.takeIf { it != enclosingSourceFile }
        return ResolvedCall(method = methodName, classOverride = inlineeClass, sourceFileOverride = sourceFileOverride)
    }

    /**
     * Parses each R8 mapping comment string (the part after `# `) as JSON
     * and forwards it as a native object so future R8 extras flow through
     * the pipeline without any schema change.
     *
     * Input → output examples:
     * ```
     * []
     *   -> null
     *
     * ["{\"id\":\"com.android.tools.r8.synthesized\"}"]
     *   -> [ {id=com.android.tools.r8.synthesized} ]
     *
     * ["{\"id\":\"com.android.tools.r8.outlineCallsite\"," +
     *    "\"positions\":{\"4\":10,\"5\":11},\"outline\":\"a.b.c()V\"}"]
     *   -> [ {id=com.android.tools.r8.outlineCallsite,
     *         positions={4=10, 5=11},
     *         outline=a.b.c()V} ]
     * ```
     *
     * `null` (rather than an empty list) is emitted when there are no
     * extras so the JSON omits the field for the common case.
     */
    private fun parseExtras(extras: List<String>): List<Map<String, Any?>>? {
        if (extras.isEmpty()) return null
        return extras.mapNotNull { raw ->
            @Suppress("UNCHECKED_CAST")
            // Round-trip through `normalizeNumbers` so integer-valued
            // doubles produced by Moshi's `Any` adapter are restored to
            // longs (see `normalizeNumbers`).
            extrasObjectAdapter.fromJson(raw)?.let { normalizeNumbers(it) as Map<String, Any?> }
        }
    }

    /**
     * Recursively converts whole-valued [Double]s to [Long] anywhere inside
     * a parsed extras object.
     *
     * Why: Moshi's built-in `Any` adapter parses every JSON number as a
     * `Double`, which round-trips integer literals as e.g. `4.0`. R8 only
     * ever uses integers in extras (line numbers in
     * `outlineCallsite.positions`, frame counts in `rewriteFrame`), so we
     * collapse whole-valued doubles back to longs to keep the output
     * faithful to the source `mapping.txt`.
     *
     * Input → output examples:
     * ```
     * 4.0                         -> 4 (Long)
     * 4.5                         -> 4.5 (Double, kept as-is)
     * "abc"                       -> "abc" (untouched)
     * { positions: { "4": 10.0 } }-> { positions: { "4": 10 (Long) } }
     * [ 1.0, "x", 2.5 ]           -> [ 1 (Long), "x", 2.5 ]
     * ```
     */
    @Suppress("UNCHECKED_CAST")
    private fun normalizeNumbers(value: Any?): Any? = when (value) {
        is Map<*, *> -> (value as Map<String, Any?>).mapValues { normalizeNumbers(it.value) }
        is List<*> -> value.map { normalizeNumbers(it) }
        // Whole-valued doubles only — fractional values stay Double so we
        // never silently truncate non-integer numbers (defensive: R8 has
        // no such case today, but the JSON shape is open-ended).
        is Double -> if (value == value.toLong().toDouble()) value.toLong() else value
        else -> value
    }

    /**
     * Pulls the `fileName` value out of R8's `sourceFile` extra, if present.
     *
     * Input → output examples:
     * ```
     * []                                                                       -> null
     * ["{\"id\":\"com.android.tools.r8.synthesized\"}"]                        -> null
     * ["{\"id\":\"sourceFile\",\"fileName\":\"Foo.kt\"}"]                      -> "Foo.kt"
     * [
     *   "{\"id\":\"com.android.tools.r8.synthesized\"}",
     *   "{\"id\":\"sourceFile\",\"fileName\":\"Bar.java\"}"
     * ]                                                                        -> "Bar.java"
     * ```
     *
     * Implementation note: this is a deliberate *substring + regex* match
     * rather than a full JSON parse. R8 emits `sourceFile` extras with a
     * stable shape and we only need two literal fields, so the cheaper
     * scan is enough. The function is called once per class; `parseExtras`
     * does the full JSON round-trip for everything that lands on the wire.
     */
    private fun extractSourceFile(extras: List<String>): String? {
        for (extra in extras) {
            if (extra.contains("\"id\":\"sourceFile\"")) {
                val match = Regex("\"fileName\":\"([^\"]+)\"").find(extra)
                if (match != null) return match.groupValues[1]
            }
        }
        return null
    }

    /**
     * Lower-case hex SHA-256 of [input]. Used both for the per-class
     * document `_id` (input is the obfuscated class name) and indirectly
     * for the per-build index name (input is `<applicationId>-<versionName>-<versionCode>`,
     * computed by the upstream Gradle task).
     *
     * Hashing the obfuscated class name keeps `_id`s URL-safe, fixed-width
     * (64 chars), and deterministic — two uploads of the same build are
     * therefore upserts (same `_id` → ES replaces the document in place).
     *
     * Input → output examples:
     * ```
     * ""           -> "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
     * "a.a"        -> "a0cbc04abef7555dbeea304c481b00d6363dcb58a040b8f69dbf33b8e5b0a69f"
     * "com.example.Foo" -> "..."  // 64-char lowercase hex
     * ```
     */
    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private data class ResolvedCall(
        val method: String,
        val classOverride: String?,
        val sourceFileOverride: String?,
    )

    internal data class BulkAction(val index: Index) {
        data class Index(
            @Json(name = "_index") val index: String,
            @Json(name = "_id") val id: String,
        )
    }
}
