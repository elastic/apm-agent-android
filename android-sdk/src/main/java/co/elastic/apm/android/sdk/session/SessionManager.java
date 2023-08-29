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
package co.elastic.apm.android.sdk.session;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.internal.api.Initializable;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;

/**
 * Provides an ID that has a 30 mins timeout that gets reset on every call to
 * {@link #getSessionId()} - If 30 mins or more have passed since the last call,
 * then a new session id is generated.
 * <p>
 * The session ID is persisted until the timeout completes, even after an app relaunch.
 */
public final class SessionManager implements Initializable {
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_SESSION_ID_EXPIRATION_TIME = "session_id_expiration_time";
    private static SessionManager instance;
    private final SystemTimeProvider systemTimeProvider;
    private final Provider<PreferencesService> preferencesServiceProvider;
    private final SessionIdGenerator sessionIdGenerator;
    private long expireTimeMillis;
    private String sessionId;

    public static SessionManager get() {
        if (instance == null) {
            throw new IllegalStateException("Session manager has not been set.");
        }
        return instance;
    }

    public static void set(SessionManager sessionManager) {
        if (instance != null) {
            throw new IllegalStateException("Session manager can be set only once.");
        }
        instance = sessionManager;
    }

    public static void resetForTest() {
        instance = null;
    }

    public SessionManager(SessionIdGenerator sessionIdGenerator) {
        this(SystemTimeProvider.get(), ServiceManager.getServiceProvider(Service.Names.PREFERENCES), sessionIdGenerator);
    }

    SessionManager(SystemTimeProvider systemTimeProvider, Provider<PreferencesService> preferencesServiceProvider, SessionIdGenerator sessionIdGenerator) {
        this.systemTimeProvider = systemTimeProvider;
        this.preferencesServiceProvider = preferencesServiceProvider;
        this.sessionIdGenerator = sessionIdGenerator;
    }

    @NonNull
    public synchronized String getSessionId() {
        verifySessionExpiration();
        if (sessionId == null) {
            sessionId = generateSessionId();
        }
        scheduleExpireTime();
        return sessionId;
    }

    public synchronized void forceRefreshId() {
        sessionId = null;
    }

    private void verifySessionExpiration() {
        if (systemTimeProvider.getCurrentTimeMillis() >= expireTimeMillis) {
            forceRefreshId();
        }
    }

    private String generateSessionId() {
        String generatedId = sessionIdGenerator.generate();
        persistSessionId(generatedId);
        return generatedId;
    }

    private void scheduleExpireTime() {
        expireTimeMillis = systemTimeProvider.getCurrentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
        persistExpirationTime(expireTimeMillis);
    }

    private void persistSessionId(String generatedId) {
        preferencesServiceProvider.get().store(KEY_SESSION_ID, generatedId);
    }

    private void persistExpirationTime(long expireTimeMillis) {
        preferencesServiceProvider.get().store(KEY_SESSION_ID_EXPIRATION_TIME, expireTimeMillis);
    }

    @Override
    public void initialize() {
        PreferencesService preferencesService = preferencesServiceProvider.get();
        expireTimeMillis = preferencesService.retrieveLong(KEY_SESSION_ID_EXPIRATION_TIME, 0);
        sessionId = preferencesService.retrieveString(KEY_SESSION_ID);
    }
}