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
package co.elastic.apm.android.sdk.attributes.resources;

import java.util.UUID;

import co.elastic.apm.android.sdk.attributes.AttributesVisitor;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class DeviceIdVisitor implements AttributesVisitor {

    private static final String DEVICE_ID_KEY = "device_id";

    @Override
    public void visit(AttributesBuilder builder) {
        builder.put(ResourceAttributes.DEVICE_ID, getId());
    }

    private String getId() {
        PreferencesService preferences = ServiceManager.get().getService(Service.Names.PREFERENCES);
        String deviceId = preferences.retrieveString(DEVICE_ID_KEY);

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            preferences.store(DEVICE_ID_KEY, deviceId);
        }

        return deviceId;
    }
}
