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
package co.elastic.apm.android.sdk.internal.injection;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.impl.GeneralConfiguration;
import co.elastic.apm.android.sdk.internal.features.centralconfig.initializer.CentralConfigurationInitializer;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;

public class DefaultAgentDependenciesInjector implements AgentDependenciesInjector {
    private final Context appContext;

    public DefaultAgentDependenciesInjector(Context appContext) {
        this.appContext = appContext;
    }

    @Override
    public NtpManager getNtpManager() {
        return new NtpManager(appContext);
    }

    @Override
    public CentralConfigurationInitializer getCentralConfigurationInitializer() {
        return new CentralConfigurationInitializer(appContext);
    }

    @Override
    public List<Configuration> getDefaultConfigurations(ElasticApmConfiguration configuration, Connectivity connectivity) {
        List<Configuration> configurations = new ArrayList<>();
        configurations.add(new GeneralConfiguration(configuration));
        configurations.add(new ConnectivityConfiguration(connectivity));
        return configurations;
    }
}
