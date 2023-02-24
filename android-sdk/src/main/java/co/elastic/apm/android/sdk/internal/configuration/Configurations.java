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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.elastic.apm.android.common.internal.logging.Elog;

@SuppressWarnings("unchecked")
public final class Configurations {
    private static Configurations INSTANCE;
    private final Map<Class<? extends Configuration>, Configuration> configurations;

    static Configurations get() {
        return INSTANCE;
    }

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    public static Configurations.Builder builder() {
        return new Configurations.Builder();
    }

    private Configurations(Map<Class<? extends Configuration>, Configuration> configurations) {
        this.configurations = configurations;
    }

    public static <T extends Configuration> T get(Class<? extends Configuration> configurationClass) {
        return get().getConfiguration(configurationClass);
    }

    public static <T> List<T> findByType(Class<T> type) {
        if (!isInitialized()) {
            Elog.getLogger().info("Configurations has not been initialized");
            return Collections.emptyList();
        }
        List<T> found = new ArrayList<>();

        for (Configuration configuration : Configurations.get().configurations.values()) {
            if (type.isAssignableFrom(configuration.getClass())) {
                found.add((T) configuration);
            }
        }

        return found;
    }

    public static boolean hasConfiguration(Class<? extends Configuration> configurationClass) {
        return get().configurations.containsKey(configurationClass);
    }

    public <T extends Configuration> T getConfiguration(Class<? extends Configuration> configurationClass) {
        if (!configurations.containsKey(configurationClass)) {
            throw new IllegalArgumentException("No configuration found for '" + configurationClass.getName() + "'");
        }
        return (T) configurations.get(configurationClass);
    }

    public static void resetForTest() {
        INSTANCE = null;
    }

    public static class Builder {
        private final Map<Class<? extends Configuration>, Configuration> configurations = new HashMap<>();

        private Builder() {
        }

        public Builder register(Configuration configuration) {
            Class<? extends Configuration> configurationClass = configuration.getClass();
            if (configurations.containsKey(configurationClass)) {
                throw new IllegalStateException("The configuration '" + configurationClass.getName() + "' is already registered");
            }
            configurations.put(configurationClass, configuration);
            return this;
        }

        public Configurations buildAndRegisterGlobal() {
            INSTANCE = new Configurations(configurations);
            return INSTANCE;
        }
    }
}
