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

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.connectivity.Connectivity;

public class CentralConfigurationFetcher {
    private static final int REQUEST_OK = 200;
    private static final int CONFIGURATION_NOT_MODIFIED = 304;
    private static final int REQUEST_FORBIDDEN = 403;
    private static final int CONFIGURATION_NOT_FOUND = 404;
    private static final int SERVICE_UNAVAILABLE = 503;
    private final Logger logger = Elog.getLogger(CentralConfigurationFetcher.class);
    private final Connectivity connectivity;
    private final String serviceName;
    private final String serviceEnvironment;
    private final ConfigurationFileProvider fileProvider;

    public CentralConfigurationFetcher(Connectivity connectivity,
                                       String serviceName,
                                       String serviceEnvironment,
                                       ConfigurationFileProvider fileProvider) {
        this.connectivity = connectivity;
        this.serviceName = serviceName;
        this.serviceEnvironment = serviceEnvironment;
        this.fileProvider = fileProvider;
    }

    public boolean fetch() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) getUrl().openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        if (connectivity.authConfiguration() != null) {
            connection.setRequestProperty("Authorization", connectivity.authConfiguration().asAuthorizationHeaderValue());
        }
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == REQUEST_OK) {
                saveConfiguration(connection.getInputStream());
                return true;
            } else {
                handleUnsuccessfulResponse(responseCode);
            }
            return false;
        } finally {
            connection.disconnect();
        }
    }

    private void handleUnsuccessfulResponse(int responseCode) {
        switch (responseCode) {
            case CONFIGURATION_NOT_MODIFIED:
                logger.debug("Configuration did not change");
                break;
            case CONFIGURATION_NOT_FOUND:
                logger.debug("This APM Server does not support central configuration. Update to APM Server 7.3+");
                break;
            case REQUEST_FORBIDDEN:
                logger.debug("Central configuration is disabled. Set kibana.enabled: true in your APM Server configuration.");
                break;
            case SERVICE_UNAVAILABLE:
                throw new IllegalStateException("Remote configuration is not available. Check the connection between APM Server and Kibana.");
            default:
                throw new IllegalStateException("Unexpected status " + responseCode + " while fetching configuration");
        }
    }

    private void saveConfiguration(InputStream inputStream) throws IOException {
        Files.copy(inputStream, fileProvider.getConfigurationFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private URL getUrl() throws MalformedURLException {
        Uri uri = Uri.parse(connectivity.endpoint()).buildUpon()
                .appendQueryParameter("service.name", serviceName)
                .appendQueryParameter("service.environment", serviceEnvironment)
                .build();
        return new URL(uri.toString());
    }
}