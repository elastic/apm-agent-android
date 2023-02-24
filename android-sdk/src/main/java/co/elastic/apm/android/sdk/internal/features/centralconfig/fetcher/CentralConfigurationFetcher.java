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
package co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher;

import android.net.Uri;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.impl.GeneralConfiguration;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;

public final class CentralConfigurationFetcher {
    private static final int REQUEST_OK = 200;
    private static final int CONFIGURATION_NOT_MODIFIED = 304;
    private static final int REQUEST_FORBIDDEN = 403;
    private static final int CONFIGURATION_NOT_FOUND = 404;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final String ETAG_PREFERENCE_NAME = "central_configuration_etag";
    private static final Pattern MAX_AGE = Pattern.compile("max-age\\s*=\\s*(\\d+)");
    private final Logger logger = Elog.getLogger();
    private final ConnectivityConfiguration connectivity;
    private final ConfigurationFileProvider fileProvider;
    private final PreferencesService preferences;

    public CentralConfigurationFetcher(ConnectivityConfiguration connectivity,
                                       ConfigurationFileProvider fileProvider,
                                       PreferencesService preferences) {
        this.connectivity = connectivity;
        this.fileProvider = fileProvider;
        this.preferences = preferences;
    }

    public FetchResult fetch() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) getUrl().openConnection();
        String eTag = getETag();
        connection.setRequestProperty("Content-Type", "application/json");
        if (eTag != null) {
            connection.setRequestProperty("If-None-Match", eTag);
        }
        if (connectivity.getAuthConfiguration() != null) {
            connection.setRequestProperty("Authorization", connectivity.getAuthConfiguration().asAuthorizationHeaderValue());
        }
        try {
            storeETag(connection.getHeaderField("ETag"));
            Integer maxAgeInSeconds = parseMaxAge(connection.getHeaderField("Cache-Control"));
            int responseCode = connection.getResponseCode();
            if (responseCode == REQUEST_OK) {
                saveConfiguration(connection.getInputStream());
                return new FetchResult(maxAgeInSeconds, true);
            } else {
                handleUnsuccessfulResponse(responseCode);
            }
            return new FetchResult(maxAgeInSeconds, false);
        } finally {
            connection.disconnect();
        }
    }

    private Integer parseMaxAge(String cacheControlHeader) {
        if (cacheControlHeader == null) {
            logger.debug("Cache control header not found");
            return null;
        }
        Matcher matcher = MAX_AGE.matcher(cacheControlHeader);
        if (!matcher.find()) {
            logger.debug("Cache control header has invalid format: {}", cacheControlHeader);
            return null;
        }
        return Integer.parseInt(matcher.group(1));
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

    private void storeETag(String eTag) {
        logger.debug("Storing ETag {}", eTag);
        preferences.store(ETAG_PREFERENCE_NAME, eTag);
    }

    private String getETag() {
        String eTag = preferences.retrieveString(ETAG_PREFERENCE_NAME);
        logger.debug("Retrieving ETag {}", eTag);
        return eTag;
    }

    private URL getUrl() throws MalformedURLException {
        GeneralConfiguration configuration = Configurations.get(GeneralConfiguration.class);
        Uri uri = Uri.parse(connectivity.getEndpoint()).buildUpon()
                .appendQueryParameter("service.name", configuration.getServiceName())
                .appendQueryParameter("service.environment", configuration.getServiceEnvironment())
                .build();
        logger.debug("Central config url: {}", uri);
        return new URL(uri.toString());
    }
}
