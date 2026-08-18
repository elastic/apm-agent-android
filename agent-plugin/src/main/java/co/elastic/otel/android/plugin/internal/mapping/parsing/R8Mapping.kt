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

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 *
 * Top-level parsed representation of an R8 `mapping.txt`.
 *
 * [mapVersion] carries the value declared by R8 at the top of the file:
 *
 *     # {"id":"com.android.tools.r8.mapping","version":"2.2"}
 *
 * It is `null` for legacy R8 mappings that pre-date the version comment
 * (R8 < 8 / map-version 1.x) where R8 itself defaults to map-version 1.0.
 */
internal data class R8Mapping(
    val classMappings: List<ClassMapping>,
    val mapVersion: String? = null,
)
