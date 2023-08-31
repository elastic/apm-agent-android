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

import org.stagemonitor.configuration.ConfigurationOptionProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class Configuration extends ConfigurationOptionProvider {

    protected ConfigurationOption<Boolean> createBooleanOption(String key, boolean defaultValue) {
        return ConfigurationOption.booleanOption(key, defaultValue);
    }

    protected ConfigurationOption<Double> createDoubleOption(String key, double defaultValue) {
        return ConfigurationOption.doubleOption(key, defaultValue);
    }

    @Override
    public final List<org.stagemonitor.configuration.ConfigurationOption<?>> getConfigurationOptions() {
        OptionsRegistry optionsRegistry = new OptionsRegistry();
        visitOptions(optionsRegistry);
        List<org.stagemonitor.configuration.ConfigurationOption<?>> stageMonitorOptions = new ArrayList<>();
        for (ConfigurationOption<?> option : optionsRegistry.getOptions()) {
            stageMonitorOptions.add(option.wrapped);
        }
        return stageMonitorOptions;
    }

    protected void visitOptions(OptionsRegistry options) {

    }
}
