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
package co.elastic.apm.android.sdk.internal.features.centralconfig;

import android.net.Uri;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import co.elastic.apm.android.sdk.connectivity.Connectivity;

public class CentralConfigurationFetcher {
    private final Connectivity connectivity;
    private final String serviceName;
    private final String serviceEnvironment;
    private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
    private final byte[] buffer = new byte[4096];

    public CentralConfigurationFetcher(Connectivity connectivity,
                                       String serviceName,
                                       String serviceEnvironment) {
        this.connectivity = connectivity;
        this.serviceName = serviceName;
        this.serviceEnvironment = serviceEnvironment;
    }

    public Map<String, String> fetch() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) getUrl().openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        if (connectivity.authConfiguration() != null) {
            connection.setRequestProperty("Authorization", connectivity.authConfiguration().asAuthorizationHeaderValue());
        }
        try {
            final JsonReader<Object> reader = dslJson.newReader(connection.getInputStream(), buffer);
            reader.startObject();
            return MapConverter.deserialize(reader);
        } finally {
            connection.disconnect();
        }
    }

    private URL getUrl() throws MalformedURLException {
        Uri uri = Uri.parse(connectivity.endpoint()).buildUpon()
                .appendQueryParameter("service.name", serviceName)
                .appendQueryParameter("service.environment", serviceEnvironment)
                .build();
        return new URL(uri.toString());
    }
}
