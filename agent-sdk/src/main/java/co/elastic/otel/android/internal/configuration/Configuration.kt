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
import org.stagemonitor.configuration.ConfigurationOptionProvider

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal abstract class Configuration : ConfigurationOptionProvider() {
    private val options by lazy { mutableListOf<ConfigurationOption<*>>() }

    protected fun createBooleanOption(key: String): ConfigurationOption<Optional<Boolean>> {
        return register(ConfigurationOption.booleanOption(key))
    }

    protected fun createDoubleOption(key: String): ConfigurationOption<Optional<Double>> {
        return register(ConfigurationOption.doubleOption(key))
    }

    private fun <T> register(option: ConfigurationOption<T>): ConfigurationOption<T> {
        options.add(option)
        return option
    }

    override fun getConfigurationOptions(): List<org.stagemonitor.configuration.ConfigurationOption<*>> {
        val stageMonitorOptions =
            mutableListOf<org.stagemonitor.configuration.ConfigurationOption<*>>()
        for (option in options) {
            stageMonitorOptions.add(option.wrapped)
        }
        return stageMonitorOptions
    }
}
