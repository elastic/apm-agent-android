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

import org.stagemonitor.configuration.ConfigurationOption;
import org.stagemonitor.configuration.ConfigurationOptionProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class Configuration extends ConfigurationOptionProvider {

    protected ConfigurationOption<Boolean> createBooleanOption(String key, boolean defaultValue) {
        return ConfigurationOption.booleanOption()
                .key(key)
                .dynamic(true)
                .buildWithDefault(defaultValue);
    }

    protected ConfigurationOption<String> createStringOption(String key, String defaultValue) {
        return ConfigurationOption.stringOption()
                .key(key)
                .dynamic(true)
                .buildWithDefault(defaultValue);
    }

    @Override
    public final List<ConfigurationOption<?>> getConfigurationOptions() {
        List<ConfigurationOption<?>> options = new ArrayList<>();
        visitOptions(options);
        return options;
    }

    protected void visitOptions(List<ConfigurationOption<?>> options) {

    }
}
