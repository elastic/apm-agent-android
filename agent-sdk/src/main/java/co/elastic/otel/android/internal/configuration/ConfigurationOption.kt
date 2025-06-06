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
package co.elastic.otel.android.internal.configuration

import java.util.Optional
import org.stagemonitor.configuration.ConfigurationOption.ConfigurationOptionBuilder

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class ConfigurationOption<T>(internal val wrapped: org.stagemonitor.configuration.ConfigurationOption<T>) {

    fun get(): T {
        return wrapped.get()
    }

    companion object {
        fun booleanOption(key: String): ConfigurationOption<Optional<Boolean>> {
            return configureAndBuild(
                org.stagemonitor.configuration.ConfigurationOption.booleanOption(),
                key
            )
        }

        fun doubleOption(key: String): ConfigurationOption<Optional<Double>> {
            return configureAndBuild(
                org.stagemonitor.configuration.ConfigurationOption.doubleOption(),
                key
            )
        }

        private fun <T> configureAndBuild(
            builder: ConfigurationOptionBuilder<T>,
            key: String
        ): ConfigurationOption<Optional<T>> {
            return ConfigurationOption(
                builder.key(key)
                    .dynamic(true)
                    .buildOptional()
            )
        }
    }
}
