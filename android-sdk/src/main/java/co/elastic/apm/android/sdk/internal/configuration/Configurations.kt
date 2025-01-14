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
package co.elastic.apm.android.sdk.internal.configuration

import co.elastic.apm.android.sdk.internal.configuration.kotlin.Configuration
import org.stagemonitor.configuration.ConfigurationRegistry
import org.stagemonitor.configuration.source.ConfigurationSource

class Configurations private constructor(private val configurationRegistry: ConfigurationRegistry) {

    fun <T : Configuration> getConfiguration(configurationClass: Class<T>): T? {
        return configurationRegistry.getConfig(configurationClass)
    }

    fun doReload() {
        configurationRegistry.reloadDynamicConfigurationOptions()
    }

    class Builder {
        private val registryBuilder: ConfigurationRegistry.Builder = ConfigurationRegistry.builder()

        fun addSource(source: ConfigurationSource): Builder {
            registryBuilder.addConfigSource(source)
            return this
        }

        fun register(configuration: Configuration): Builder {
            registryBuilder.addOptionProvider(configuration)
            return this
        }

        fun registerAll(configurations: Collection<Configuration>): Builder {
            for (configuration in configurations) {
                registryBuilder.addOptionProvider(configuration)
            }
            return this
        }

        fun buildAndRegisterGlobal(): Configurations {
            INSTANCE = Configurations(registryBuilder.build())
            return get()
        }
    }

    companion object {
        internal var INSTANCE: Configurations? = null

        @JvmStatic
        fun get(): Configurations {
            return INSTANCE!!
        }

        @JvmStatic
        fun isInitialized(): Boolean = INSTANCE != null

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }

        @JvmStatic
        fun <T : Configuration> get(configurationClass: Class<T>): T? {
            return get().getConfiguration(configurationClass)
        }

        @JvmStatic
        fun reload() {
            get().doReload()
        }

        @JvmStatic
        fun resetForTest() {
            INSTANCE = null
        }
    }
}
