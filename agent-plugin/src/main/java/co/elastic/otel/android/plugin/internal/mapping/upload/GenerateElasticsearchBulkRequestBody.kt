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

import co.elastic.otel.android.plugin.internal.mapping.parsing.R8MappingParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@CacheableTask
internal abstract class GenerateElasticsearchBulkRequestBody : DefaultTask() {

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mappingFile: RegularFileProperty

    @get:Input
    abstract val indexName: Property<String>

    @get:OutputFile
    abstract val requestBodyFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val mapping = mappingFile.orNull?.asFile
        if (mapping == null || !mapping.exists()) {
            logger.lifecycle("No mapping file found. Skipping bulk request body generation.")
            return
        }

        val indexName = indexName.get()
        logger.lifecycle("Generating Elasticsearch bulk request body (index: $indexName)")

        val r8Mapping = mapping.bufferedReader().use { R8MappingParser.parse(it) }
        requestBodyFile.get().asFile.bufferedWriter().use { writer ->
            NdjsonBulkWriter.write(writer, indexName, r8Mapping)
        }

        logger.lifecycle("Wrote bulk request body to: ${requestBodyFile.get().asFile.absolutePath}")
    }

}
