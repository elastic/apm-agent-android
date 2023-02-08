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

import java.util.HashMap;
import java.util.Map;

public class Features {
    private static Features INSTANCE;
    private final Map<Class<? extends FeatureConfiguration>, FeatureConfiguration> configurations;

    public static Features get() {
        return INSTANCE;
    }

    public static Features.Builder builder() {
        return new Features.Builder();
    }

    private Features(Map<Class<? extends FeatureConfiguration>, FeatureConfiguration> configurations) {
        this.configurations = configurations;
        INSTANCE = this;
    }

    public static boolean isEnabled(Class<? extends FeatureConfiguration> configurationClass) {
        return get().getConfiguration(configurationClass).isEnabled();
    }

    @SuppressWarnings("unchecked")
    public <T extends FeatureConfiguration> T getConfiguration(Class<? extends FeatureConfiguration> configurationClass) {
        if (!configurations.containsKey(configurationClass)) {
            throw new IllegalArgumentException("No configuration found for '" + configurationClass.getName() + "'");
        }
        return (T) configurations.get(configurationClass);
    }

    public static class Builder {
        private final Map<Class<? extends FeatureConfiguration>, FeatureConfiguration> features = new HashMap<>();

        private Builder() {
        }

        public Builder register(FeatureConfiguration featureConfiguration) {
            Class<? extends FeatureConfiguration> configurationClass = featureConfiguration.getClass();
            if (features.containsKey(configurationClass)) {
                throw new IllegalStateException("The feature '" + configurationClass.getName() + "' is already registered");
            }
            features.put(configurationClass, featureConfiguration);
            return this;
        }

        public Features build() {
            return new Features(features);
        }
    }
}
