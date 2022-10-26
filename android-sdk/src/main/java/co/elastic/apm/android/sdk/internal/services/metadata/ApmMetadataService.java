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
package co.elastic.apm.android.sdk.internal.services.metadata;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import co.elastic.apm.android.common.ApmInfo;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.providers.LazyProvider;

public class ApmMetadataService implements Service {
    private final LazyProvider<Properties> apmInfoPropertiesProvider;

    public ApmMetadataService(Context appContext) {
        apmInfoPropertiesProvider = LazyProvider.of(() -> getApmInfoProperties(appContext));
    }

    @NonNull
    public String getServiceName() {
        return apmInfoPropertiesProvider.get().getProperty(ApmInfo.KEY_SERVICE_NAME);
    }

    @NonNull
    public String getServiceVersion() {
        return apmInfoPropertiesProvider.get().getProperty(ApmInfo.KEY_SERVICE_VERSION);
    }

    @NonNull
    public String getServerUrl() {
        return apmInfoPropertiesProvider.get().getProperty(ApmInfo.KEY_SERVER_URL);
    }

    @Nullable
    public String getServerToken() {
        return apmInfoPropertiesProvider.get().getProperty(ApmInfo.KEY_SERVER_TOKEN);
    }

    @NonNull
    public String getDeploymentEnvironment() {
        return apmInfoPropertiesProvider.get().getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT);
    }

    @Nullable
    public String getOkHttpVersion() {
        return apmInfoPropertiesProvider.get().getProperty(ApmInfo.KEY_SCOPE_OKHTTP_VERSION);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public String name() {
        return Service.Names.METADATA;
    }

    private Properties getApmInfoProperties(Context appContext) {
        try (InputStream propertiesFileInputStream = appContext.getAssets().open(ApmInfo.ASSET_FILE_NAME)) {
            Properties properties = new Properties();
            properties.load(propertiesFileInputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
