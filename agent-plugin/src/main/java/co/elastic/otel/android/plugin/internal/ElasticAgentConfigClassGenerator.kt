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
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.FieldManifestation
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal object ElasticAgentConfigClassGenerator {
    const val GENERATED_CLASS_NAME = "co.elastic.otel.android.internal.generated.ElasticAgentConfig"
    const val BUILD_ID_FIELD_NAME = "BUILD_ID"

    fun generate(outputDirectory: File, buildId: String) {
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()

        ByteBuddy()
            .subclass(Any::class.java)
            .name(GENERATED_CLASS_NAME)
            .defineField(
                BUILD_ID_FIELD_NAME,
                String::class.java,
                Visibility.PUBLIC,
                Ownership.STATIC,
                FieldManifestation.FINAL,
            )
            .value(buildId)
            .make()
            .saveIn(outputDirectory)
    }
}
