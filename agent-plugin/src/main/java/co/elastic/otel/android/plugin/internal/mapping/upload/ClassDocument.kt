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

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 *
 * One ES document per obfuscated class. Methods are nested under [methods],
 * keyed by obfuscated method name.
 *
 * `mapVersion` carries the value declared by R8 at the top of the source
 * `mapping.txt` (e.g. `"2.2"`). It is `null` for legacy mappings whose
 * file-level version comment is absent (R8 < 8 / map-version 1.x — R8
 * itself defaults to 1.0 in that case). The retracer uses it as a
 * forward-compatibility kill-switch: documents whose `map_version` is
 * newer than the retracer understands are refused, and their frames pass
 * through unchanged instead of being deobfuscated with stale semantics.
 */
internal data class ClassDocument(
    @Json(name = "schema_version") val schemaVersion: Int = SCHEMA_VERSION,
    @Json(name = "map_version") val mapVersion: String?,
    @Json(name = "obfuscated_class") val obfuscatedClass: String,
    @Json(name = "original_class") val originalClass: String,
    @Json(name = "source_file") val sourceFile: String?,
    val methods: Map<String, MethodDocument>,
) {
    companion object {
        const val SCHEMA_VERSION = 1
    }
}
