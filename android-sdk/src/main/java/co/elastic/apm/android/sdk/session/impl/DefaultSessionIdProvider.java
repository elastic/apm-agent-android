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
package co.elastic.apm.android.sdk.session.impl;

import androidx.annotation.NonNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.internal.providers.Provider;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;
import co.elastic.apm.android.sdk.session.SessionIdProvider;

/**
 * Provides an in-memory id that has a 30 mins timeout that gets reset on every call to
 * {@link SessionIdProvider#getSessionId()} - If 30 mins or more have passed since the last call,
 * then a new session id is generated.
 */
public class DefaultSessionIdProvider implements SessionIdProvider {
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_SESSION_ID_EXPIRATION_TIME = "session_id_expiration_time";
    private final SystemTimeProvider systemTimeProvider;
    private final Provider<PreferencesService> preferencesServiceProvider;
    private long expireTimeMillis;
    private String sessionId;
    private boolean storedDataChecked = false;

    DefaultSessionIdProvider(SystemTimeProvider systemTimeProvider, Provider<PreferencesService> preferencesServiceProvider) {
        this.systemTimeProvider = systemTimeProvider;
        this.preferencesServiceProvider = preferencesServiceProvider;
    }

    public DefaultSessionIdProvider() {
        this(SystemTimeProvider.get(), ElasticApmAgent.getServiceProvider(Service.Names.PREFERENCES));
    }

    @NonNull
    @Override
    public String getSessionId() {
        checkStoredData();
        verifySessionExpiration();
        if (sessionId == null) {
            sessionId = generateSessionId();
        }
        scheduleExpireTime();
        return sessionId;
    }

    private synchronized void checkStoredData() {
        if (!storedDataChecked) {
            storedDataChecked = true;
            expireTimeMillis = preferencesServiceProvider.get().retrieveLong(KEY_SESSION_ID_EXPIRATION_TIME, 0);
            sessionId = preferencesServiceProvider.get().retrieveString(KEY_SESSION_ID);
        }
    }

    private void verifySessionExpiration() {
        if (systemTimeProvider.getCurrentTimeMillis() >= expireTimeMillis) {
            sessionId = null;
        }
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    private void scheduleExpireTime() {
        expireTimeMillis = systemTimeProvider.getCurrentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
    }
}