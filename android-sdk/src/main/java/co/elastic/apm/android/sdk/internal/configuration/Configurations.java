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
package co.elastic.apm.android.sdk.internal.configuration;

import org.stagemonitor.configuration.ConfigurationRegistry;
import org.stagemonitor.configuration.source.ConfigurationSource;

import java.util.Collection;

@SuppressWarnings("unchecked")
public final class Configurations {
    private static Configurations INSTANCE;
    private final ConfigurationRegistry configurationRegistry;

    static Configurations get() {
        return INSTANCE;
    }

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    public static Configurations.Builder builder() {
        return new Configurations.Builder();
    }

    private Configurations(ConfigurationRegistry configurationRegistry) {
        this.configurationRegistry = configurationRegistry;
    }

    public static <T extends Configuration> T get(Class<T> configurationClass) {
        return get().getConfiguration(configurationClass);
    }

    public static void reload() {
        get().doReload();
    }

    public <T extends Configuration> T getConfiguration(Class<T> configurationClass) {
        return (T) configurationRegistry.getConfig(configurationClass);
    }

    public void doReload() {
        configurationRegistry.reloadDynamicConfigurationOptions();
    }

    public static void resetForTest() {
        INSTANCE = null;
    }

    public static class Builder {
        private final ConfigurationRegistry.Builder registryBuilder = ConfigurationRegistry.builder();

        private Builder() {
        }

        public Builder addSource(ConfigurationSource source) {
            registryBuilder.addConfigSource(source);
            return this;
        }

        public Builder register(Configuration configuration) {
            registryBuilder.addOptionProvider(configuration);
            return this;
        }

        public Builder registerAll(Collection<? extends Configuration> configurations) {
            for (Configuration configuration : configurations) {
                registryBuilder.addOptionProvider(configuration);
            }
            return this;
        }

        public Configurations buildAndRegisterGlobal() {
            INSTANCE = new Configurations(registryBuilder.build());
            return INSTANCE;
        }
    }
}
