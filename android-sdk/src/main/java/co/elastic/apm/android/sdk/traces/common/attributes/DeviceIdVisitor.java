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
package co.elastic.apm.android.sdk.traces.common.attributes;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import co.elastic.apm.android.sdk.BuildConfig;
import co.elastic.apm.android.sdk.attributes.AttributesBuilderVisitor;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class DeviceIdVisitor implements AttributesBuilderVisitor {

    private static final String DEVICE_ID_KEY = "device_id";
    private final Context appContext;

    public DeviceIdVisitor(Context appContext) {
        this.appContext = appContext;
    }

    @Override
    public void visit(AttributesBuilder builder) {
        builder.put(ResourceAttributes.DEVICE_ID, getId());
    }

    private String getId() {
        SharedPreferences sharedPreferences = getSharedPreferences(appContext);
        String deviceId = sharedPreferences.getString(DEVICE_ID_KEY, null);

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            storeDeviceId(sharedPreferences, deviceId);
        }

        return deviceId;
    }

    private void storeDeviceId(SharedPreferences sharedPreferences, String deviceId) {
        sharedPreferences.edit().putString(DEVICE_ID_KEY, deviceId).apply();
    }

    private SharedPreferences getSharedPreferences(Context appContext) {
        return appContext.getSharedPreferences(BuildConfig.LIBRARY_PACKAGE_NAME + ".prefs", Context.MODE_PRIVATE);
    }
}
