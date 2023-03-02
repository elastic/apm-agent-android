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

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;

import org.slf4j.Logger;
import org.stagemonitor.configuration.source.AbstractConfigurationSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.CentralConfigurationFetcher;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.ConfigurationFileProvider;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.FetchResult;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;

public final class CentralConfigurationManager extends AbstractConfigurationSource implements ConfigurationFileProvider {
    private static final String REFRESH_TIMEOUT_PREFERENCE_NAME = "central_configuration_refresh_timeout";
    private final Context context;
    private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
    private final Map<String, String> configs = new HashMap<>();
    private final Logger logger = Elog.getLogger();
    private final byte[] buffer = new byte[4096];
    private final PreferencesService preferences;
    private final SystemTimeProvider systemTimeProvider;
    private File configFile;

    public CentralConfigurationManager(Context context) {
        this(context, SystemTimeProvider.get());
    }

    @VisibleForTesting
    public CentralConfigurationManager(Context context, SystemTimeProvider systemTimeProvider) {
        this.context = context;
        this.systemTimeProvider = systemTimeProvider;
        preferences = ServiceManager.get().getService(Service.Names.PREFERENCES);
    }

    public Integer sync() throws IOException {
        if (getRefreshTimeoutMillis() > systemTimeProvider.getCurrentTimeMillis()) {
            logger.debug("Ignoring central config sync request");
            return null;
        }
        try {
            CentralConfigurationFetcher fetcher = new CentralConfigurationFetcher(Configurations.get(ConnectivityConfiguration.class),
                    this, preferences);
            FetchResult fetchResult = fetcher.fetch();
            if (fetchResult.configurationHasChanged) {
                notifyListeners();
            }
            Integer maxAgeInSeconds = fetchResult.maxAgeInSeconds;
            if (maxAgeInSeconds != null) {
                storeRefreshTimeoutTime(maxAgeInSeconds);
            }
            return maxAgeInSeconds;
        } catch (Throwable t) {
            logger.error("An error occurred while fetching the central configuration", t);
            throw t;
        }
    }

    private void notifyListeners() throws IOException {
        configs.putAll(readConfigs(getConfigurationFile()));
        logger.info("Notifying central config change");
        logger.debug("Central config params: {}", configs);
        Configurations.reload();
        configs.clear();
    }

    private Map<String, String> readConfigs(File configFile) throws IOException {
        try (InputStream is = new FileInputStream(configFile)) {
            JsonReader<Object> reader = dslJson.newReader(is, buffer);
            reader.startObject();
            return Collections.unmodifiableMap(MapConverter.deserialize(reader));
        }
    }

    private void storeRefreshTimeoutTime(int maxAgeInSeconds) {
        logger.debug("Storing central config max age seconds {}", maxAgeInSeconds);
        setRefreshTimeoutMillis(systemTimeProvider.getCurrentTimeMillis() + TimeUnit.SECONDS.toMillis(maxAgeInSeconds));
    }

    private long getRefreshTimeoutMillis() {
        return preferences.retrieveLong(REFRESH_TIMEOUT_PREFERENCE_NAME, 0);
    }

    private void setRefreshTimeoutMillis(long timeoutMillis) {
        preferences.store(REFRESH_TIMEOUT_PREFERENCE_NAME, timeoutMillis);
    }

    @Override
    public File getConfigurationFile() {
        if (configFile == null) {
            configFile = new File(context.getFilesDir(), "elastic_agent_configuration.json");
        }
        return configFile;
    }

    public void publishCachedConfig() {
        File configurationFile = getConfigurationFile();
        if (!configurationFile.exists()) {
            logger.debug("No cached central config found");
            return;
        }
        try {
            notifyListeners();
        } catch (Throwable t) {
            logger.error("Exception when publishing cached central config", t);
        }
    }

    @Override
    public String getValue(String key) {
        return configs.get(key);
    }

    @Override
    public String getName() {
        return "APM Server";
    }
}
