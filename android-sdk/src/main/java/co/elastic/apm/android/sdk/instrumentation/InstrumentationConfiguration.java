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
package co.elastic.apm.android.sdk.instrumentation;

import androidx.annotation.NonNull;

import org.stagemonitor.configuration.ConfigurationOption;

import java.util.List;

import co.elastic.apm.android.sdk.internal.instrumentation.GroupInstrumentation;

public final class InstrumentationConfiguration extends GroupInstrumentation implements Instrumentation.Group {
    public final List<Instrumentation> instrumentations;

    public static InstrumentationConfigurationBuilder builder() {
        return InstrumentationConfigurationBuilder.allDisabled();
    }

    public static InstrumentationConfiguration allEnabled() {
        return InstrumentationConfigurationBuilder.allEnabled().build();
    }

    public static InstrumentationConfiguration allDisabled() {
        return InstrumentationConfigurationBuilder.allDisabled().build();
    }

    public InstrumentationConfiguration(List<Instrumentation> instrumentations) {
        super(true);
        this.instrumentations = instrumentations;
    }

    @Override
    protected String getEnabledKeyName() {
        return "enable_automatic_instrumentation";
    }

    @NonNull
    @Override
    protected Group getGroup() {
        return this;
    }

    @Override
    public Class<? extends Instrumentation> getType() {
        return null;
    }

    @Override
    protected List<ConfigurationOption<?>> getOptions() {
        return null;
    }
}
