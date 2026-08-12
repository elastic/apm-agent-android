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

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.JsonReader
import java.io.BufferedReader
import java.io.IOException
import okio.Buffer

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal object R8MappingParser {

    // Matches: "OriginalClass -> ObfuscatedClass:"
    private val TYPE_PATTERN = Regex("^(\\S+)\\s->\\s(\\S+):$")

    // Matches method lines like: "0:2:void <init>():282:282 -> <init>"
    // Groups: 1=obfStartLine, 2=obfEndLine, 3=methodName, 4=params, 5=origStartLine, 6=origEndLine, 7=obfuscatedName
    private val METHOD_PATTERN =
        Regex("^\\s+(?:(\\d+):(\\d+):)?\\S+\\s(\\S+)\\((.*)\\)(?::(\\d+)(?::(\\d+))?)?\\s->\\s(\\S+)$")

    // Matches field lines like: "    Type originalName -> obfuscatedName"
    private val FIELD_PATTERN = Regex("^\\s+\\S+\\s(\\S+)\\s->\\s(\\S+)$")

    // Matches comment lines like: "# {...}" or "  # {...}".
    private val COMMENT_PATTERN = Regex("^\\s*#\\s(.+)$")

    // Captures the value of `version` from the file-level mapping
    // comment R8 emits at the top of every mapping file:
    //   # {"id":"com.android.tools.r8.mapping","version":"2.2"}
    // The whole comment block must be parsed before any class header.
    private const val MAP_VERSION_COMMENT_ID = "com.android.tools.r8.mapping"
    private val MAP_VERSION_PATTERN = Regex("\"version\"\\s*:\\s*\"([^\"]+)\"")

    fun parse(reader: BufferedReader): R8Mapping {
        val state = ParseState()

        reader.forEachLine { line ->
            val typeMatch = TYPE_PATTERN.matchEntire(line)
            if (typeMatch != null) {
                state.flushType()
                state.currentOriginalName = typeMatch.groupValues[1]
                state.currentObfuscatedName = typeMatch.groupValues[2]
                state.currentCommentTarget = CommentTarget.TYPE
                return@forEachLine
            }

            val commentMatch = COMMENT_PATTERN.matchEntire(line)
            if (commentMatch != null) {
                val comment = commentMatch.groupValues[1].trim()
                if (!isJsonObject(comment)) {
                    return@forEachLine
                }
                // File-level mapping comment lives BEFORE any class header.
                // Once we are inside a class we treat all comments the
                // usual way (type or member extras).
                if (state.currentOriginalName == null) {
                    if (state.mapVersion == null && comment.contains("\"id\":\"$MAP_VERSION_COMMENT_ID\"")) {
                        state.mapVersion = MAP_VERSION_PATTERN.find(comment)?.groupValues?.get(1)
                    }
                    return@forEachLine
                }
                when (state.currentCommentTarget) {
                    CommentTarget.TYPE -> state.currentTypeExtras.add(comment)
                    CommentTarget.METHOD -> state.currentMethodLineExtras?.add(comment)
                    CommentTarget.FIELD -> Unit
                }
                return@forEachLine
            }

            if (state.currentOriginalName == null) {
                return@forEachLine
            }

            val methodMatch = METHOD_PATTERN.matchEntire(line)
            if (methodMatch != null) {
                val obfStartLine = methodMatch.groupValues[1]
                val obfEndLine = methodMatch.groupValues[2]
                val methodName = methodMatch.groupValues[3]
                val origStartLine = methodMatch.groupValues[5]
                val origEndLine = methodMatch.groupValues[6].ifEmpty { origStartLine }
                val obfMethodName = methodMatch.groupValues[7]
                val extras = mutableListOf<String>()

                val lineEntry = MethodMapping.Line(
                    value = methodName,
                    obfuscatedLineNumberRange = MethodMapping.Line.Range(obfStartLine, obfEndLine),
                    originalLineNumberRange = MethodMapping.Line.Range(origStartLine, origEndLine),
                    extras = extras,
                )
                state.currentMethodMappings.getOrPut(obfMethodName) { mutableListOf() }.add(lineEntry)
                state.currentMethodLineExtras = extras
                state.currentCommentTarget = CommentTarget.METHOD
                return@forEachLine
            }

            val fieldMatch = FIELD_PATTERN.matchEntire(line)
            if (fieldMatch != null) {
                state.currentMethodLineExtras = null
                state.currentCommentTarget = CommentTarget.FIELD
                return@forEachLine
            }
        }
        state.flushType()

        return R8Mapping(state.classMappings, state.mapVersion)
    }

    private fun isJsonObject(raw: String): Boolean {
        val buffer = Buffer().writeUtf8(raw)
        return try {
            val reader = JsonReader.of(buffer)
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
                return false
            }
            reader.skipValue()
            reader.peek() == JsonReader.Token.END_DOCUMENT
        } catch (_: JsonDataException) {
            false
        } catch (_: JsonEncodingException) {
            false
        } catch (_: IOException) {
            false
        }
    }

    private enum class CommentTarget {
        TYPE,
        METHOD,
        FIELD,
    }

    private class ParseState {
        val classMappings = mutableListOf<ClassMapping>()
        var mapVersion: String? = null
        var currentOriginalName: String? = null
        var currentObfuscatedName: String? = null
        var currentTypeExtras = mutableListOf<String>()
        var currentMethodMappings = mutableMapOf<String, MutableList<MethodMapping.Line>>()
        var currentMethodLineExtras: MutableList<String>? = null
        var currentCommentTarget = CommentTarget.TYPE

        fun flushType() {
            val origName = currentOriginalName ?: return
            val obfName = currentObfuscatedName ?: return
            // Include the class even when it has no methods, as long as it
            // carries class-level extras (typically `sourceFile`). Such
            // classes — `MyApp$Companion -> R8$$REMOVED$$CLASS$$17` — are
            // dropped by R8 from the output but their source-file metadata
            // is needed to resolve cross-class inlinees correctly. Classes
            // with neither methods nor extras carry no information and are
            // skipped.
            if (currentMethodMappings.isNotEmpty() || currentTypeExtras.isNotEmpty()) {
                val methodMappings = currentMethodMappings.mapValues { (name, lines) ->
                    MethodMapping(name, lines)
                }
                classMappings.add(ClassMapping(obfName, origName, methodMappings, currentTypeExtras))
            }
            currentOriginalName = null
            currentObfuscatedName = null
            currentTypeExtras = mutableListOf()
            currentMethodMappings = mutableMapOf()
            currentMethodLineExtras = null
            currentCommentTarget = CommentTarget.TYPE
        }
    }
}
