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
package co.elastic.apm.android.sdk.configuration.logging.impl;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.configuration.logging.LogLevel;
import co.elastic.apm.android.sdk.configuration.logging.LoggingPolicy;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;
import co.elastic.apm.android.sdk.internal.utilities.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;

public class DefaultLoggingPolicy implements LoggingPolicy {

    private final Provider<Boolean> appIsDebuggable;
    private final Provider<Boolean> agentIsInitialized;

    public static DefaultLoggingPolicy create() {
        final Provider<Boolean> agentIsInitialized = ElasticApmAgent::isInitialized;
        return new DefaultLoggingPolicy(LazyProvider.of(() -> {
            if (!agentIsInitialized.get()) {
                return false;
            }
            AppInfoService service = ServiceManager.get().getService(Service.Names.APP_INFO);
            return service.isInDebugMode();
        }), agentIsInitialized);
    }

    public DefaultLoggingPolicy(Provider<Boolean> appIsDebuggable, Provider<Boolean> agentIsInitialized) {
        this.appIsDebuggable = appIsDebuggable;
        this.agentIsInitialized = agentIsInitialized;
    }

    @Override
    public boolean isEnabled() {
        return agentIsInitialized.get();
    }

    @Override
    public LogLevel getMinimumLevel() {
        return (appIsDebuggable.get()) ? LogLevel.DEBUG : LogLevel.INFO;
    }
}
