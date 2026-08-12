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
 */
/**
 * One entry in `methods.<obf>.default_mappings[]`. Used for R8 entries
 * that have no obfuscated line range — for example
 * `void evaluateSampleRate() -> a` (no original range either) or
 * `void method1():42:42 -> a` (rangeless on the obfuscated side, but
 * with an original line range that the retracer must surface).
 */
internal data class DefaultMappingEntry(
    val method: String,
    @Json(name = "class") val originalClass: String?,
    @Json(name = "source_file") val sourceFile: String?,
    @Json(name = "orig_range") val origRange: List<Int>?,
)
