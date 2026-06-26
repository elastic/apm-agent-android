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
package co.elastic.otel.android.plugin.extensions

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

@Deprecated(
    """Use the per-build-type or per-flavor elasticOtel extension instead. For example:
import co.elastic.otel.android.plugin.extensions.ElasticExtension

android {
    buildTypes {
        debug {
            extensions.configure<ElasticExtension> {
                bytecodeInstrumentation.disabled.set(true)
            }
        }
    }
}""",
)
abstract class ElasticApmExtension @Inject constructor(objects: ObjectFactory) {
    @Suppress("DEPRECATION")
    val bytecodeInstrumentation: LegacyBytecodeInstrumentation =
        objects.newInstance(LegacyBytecodeInstrumentation::class.java)

    @Deprecated(
        """Use the per-buildType or per-flavor elasticOtel extension instead. For example:
import co.elastic.otel.android.plugin.extensions.ElasticExtension

android {
    buildTypes {
        debug {
            extensions.configure<ElasticExtension> {
                bytecodeInstrumentation.disabled.set(true)
            }
        }
    }
}""",
    )
    @Suppress("DEPRECATION")
    fun bytecodeInstrumentation(action: Action<LegacyBytecodeInstrumentation>) {
        action.execute(bytecodeInstrumentation)
    }
}
