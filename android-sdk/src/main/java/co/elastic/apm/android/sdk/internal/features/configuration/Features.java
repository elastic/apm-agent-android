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
package co.elastic.apm.android.sdk.internal.features.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import co.elastic.apm.android.sdk.internal.api.FeatureConfiguration;

public class Features {
    private static Features INSTANCE;
    private final Map<Class<? extends FeatureConfiguration>, FeatureConfiguration> configurationsMap;

    public static Features get() {
        return INSTANCE;
    }

    public static Features.Builder builder() {
        return new Features.Builder();
    }

    private Features(List<? extends FeatureConfiguration> configurations) {
        this.configurationsMap = new HashMap<>();
        configurations.forEach((Consumer<FeatureConfiguration>) featureConfiguration -> configurationsMap.put(featureConfiguration.getClass(), featureConfiguration));
        INSTANCE = this;
    }

    @SuppressWarnings("unchecked")
    public <T extends FeatureConfiguration> T getConfiguration(Class<? extends FeatureConfiguration> configurationClass) {
        if (!configurationsMap.containsKey(configurationClass)) {
            throw new IllegalArgumentException("No configuration found for '" + configurationClass.getName() + "'");
        }
        return (T) configurationsMap.get(configurationClass);
    }

    public static class Builder {
        private final List<FeatureConfiguration> features = new ArrayList<>();

        private Builder() {
        }

        public Builder register(FeatureConfiguration featureConfiguration) {
            features.add(featureConfiguration);
            return this;
        }

        public Features build() {
            return new Features(features);
        }
    }
}
