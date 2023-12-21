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
package co.elastic.apm.android.sdk.internal.configuration.impl;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;

public final class GeneralConfiguration extends Configuration {
    private final AppInfoService appInfoService;
    private final String providedServiceName;
    private final String providedServiceVersion;
    private final String providedDeploymentEnvironment;

    public GeneralConfiguration(ElasticApmConfiguration configuration) {
        this.appInfoService = ServiceManager.get().getService(Service.Names.APP_INFO);
        providedServiceName = configuration.serviceName;
        providedServiceVersion = configuration.serviceVersion;
        providedDeploymentEnvironment = configuration.deploymentEnvironment;
    }

    public String getServiceName() {
        return (providedServiceName != null) ? providedServiceName : appInfoService.getAppName();
    }

    public String getServiceVersion() {
        return (providedServiceVersion != null) ? providedServiceVersion : appInfoService.getAppVersion();
    }

    public String getServiceEnvironment() {
        return (providedDeploymentEnvironment != null) ? providedDeploymentEnvironment : appInfoService.getAppBuildType();
    }
}
