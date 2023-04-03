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
package co.elastic.apm.android.sdk.connectivity;

import co.elastic.apm.android.sdk.connectivity.auth.AuthConfiguration;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;

public interface Connectivity {

    static Connectivity simple(String endpoint) {
        return new DefaultConnectivity(endpoint, null);
    }

    static Connectivity withSecretToken(String endpoint, String secretToken) {
        return new DefaultConnectivity(endpoint, AuthConfiguration.secretToken(secretToken));
    }

    static Connectivity withApiKey(String endpoint, String apiKey) {
        return new DefaultConnectivity(endpoint, AuthConfiguration.apiKey(apiKey));
    }

    static Connectivity getDefault() {
        ApmMetadataService service = ServiceManager.get().getService(Service.Names.METADATA);
        String serverUrl = service.getServerUrl();
        String apiKey = service.getApiKey();
        String secretToken = service.getSecretToken();
        if (apiKey != null) {
            return Connectivity.withApiKey(serverUrl, apiKey);
        } else if (secretToken != null) {
            return Connectivity.withSecretToken(serverUrl, secretToken);
        } else {
            return Connectivity.simple(serverUrl);
        }
    }

    String endpoint();

    AuthConfiguration authConfiguration();
}
