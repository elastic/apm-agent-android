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

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.CentralConfigurationFetcher;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.ConfigurationFileProvider;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.FetchResult;
import co.elastic.apm.android.sdk.internal.features.centralconfig.worker.CentralConfigFetchWorker;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;

public final class CentralConfigurationManager implements ConfigurationFileProvider {
    private static final String MAX_AGE_PREFERENCE_NAME = "central_configuration_max_age";
    private static final int DEFAULT_POLL_DELAY_SEC = (int) TimeUnit.MINUTES.toSeconds(5);
    private static final String UNIQUE_PERIODIC_WORK_NAME = "central_config_periodic_work";
    private final Context context;
    private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
    private final Logger logger = Elog.getLogger(CentralConfigurationManager.class);
    private final byte[] buffer = new byte[4096];
    private final PreferencesService preferences;
    private File configFile;

    public CentralConfigurationManager(Context context) {
        this.context = context;
        preferences = ServiceManager.get().getService(Service.Names.PREFERENCES);
    }

    public static void scheduleSync(Context context, Integer timeIntervalInSeconds) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.METERED)
                .setRequiresCharging(true)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(CentralConfigFetchWorker.class,
                timeIntervalInSeconds, TimeUnit.SECONDS)
                .setInitialDelay(timeIntervalInSeconds, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest);

        Elog.getLogger().debug("Enqueued central config worker");
    }

    public void sync() throws IOException {
        try {
            CentralConfigurationFetcher fetcher = new CentralConfigurationFetcher(this, preferences);
            FetchResult fetchResult = fetcher.fetch();
            if (fetchResult.configurationHasChanged) {
                notifyConfigurationChanged(readConfigs(getConfigurationFile()));
            }
            storeMaxAgeTime(fetchResult.maxAgeInSeconds);
        } catch (Throwable t) {
            logger.error("An error occurred while fetching the central configuration", t);
            throw t;
        }
    }

    private void storeMaxAgeTime(Integer maxAgeInSeconds) {
        if (maxAgeInSeconds == null) {
            return;
        }
        logger.debug("Storing central config max age in seconds: {}", maxAgeInSeconds);
        preferences.store(MAX_AGE_PREFERENCE_NAME, maxAgeInSeconds);
    }

    private Map<String, String> readConfigs(File configFile) throws IOException {
        try (InputStream is = new FileInputStream(configFile)) {
            JsonReader<Object> reader = dslJson.newReader(is, buffer);
            reader.startObject();
            return Collections.unmodifiableMap(MapConverter.deserialize(reader));
        }
    }

    private void notifyConfigurationChanged(Map<String, String> configs) {
        for (CentralConfigurationListener listener : Configurations.findByType(CentralConfigurationListener.class)) {
            listener.onUpdate(configs);
        }
    }

    @Override
    public File getConfigurationFile() {
        if (configFile == null) {
            configFile = new File(context.getFilesDir(), "elastic_agent_configuration.json");
        }
        return configFile;
    }
}
