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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;
import co.elastic.apm.android.sdk.session.impl.DefaultSessionIdGenerator;
import co.elastic.apm.android.sdk.testutils.BaseTest;

public class SessionManagerTest extends BaseTest implements Provider<PreferencesService> {
    private PreferencesService preferencesService;
    private SessionIdGenerator sessionIdGenerator;
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_SESSION_ID_EXPIRATION_TIME = "session_id_expiration_time";

    @Before
    public void setUp() {
        preferencesService = mock(PreferencesService.class);
        sessionIdGenerator = new DefaultSessionIdGenerator();
    }

    @Test
    public void whenSessionIdIsRequested_provideNonEmptyId() {
        SessionManager sessionManager = getSessionManager();

        String sessionId = sessionManager.getSessionId();

        assertNotNull(sessionId);
        assertFalse(sessionId.isEmpty());
    }

    @Test
    public void whenSessionIdIsRequestedMultipleTimes_provideSameId() {
        SessionManager sessionManager = getSessionManager();

        assertEquals(sessionManager.getSessionId(), sessionManager.getSessionId());
    }

    @Test
    public void whenSessionIdIsRequestedAfterForcingIdRefresh_provideNewId() {
        SessionManager sessionManager = getSessionManager();
        String firstSessionId = sessionManager.getSessionId();

        assertEquals(firstSessionId, sessionManager.getSessionId());

        sessionManager.forceRefreshId();

        assertNotEquals(firstSessionId, sessionManager.getSessionId());
        assertNotNull(sessionManager.getSessionId());
    }

    @Test
    public void whenSessionIdIsRequestedAgainAfter30Min_provideNewId_andKeepItWhenLessThan30MinsTimePassed() {
        int initialTime = 1_000_000;
        SystemTimeProvider systemTimeProvider = getSystemTimeProvider(initialTime);
        SessionManager sessionManager = getSessionManager(systemTimeProvider);

        String firstId = sessionManager.getSessionId();

        // Should change after 30 mins
        addTimeInMillis(systemTimeProvider, TimeUnit.MINUTES.toMillis(30));

        String secondId = sessionManager.getSessionId();

        assertNotEquals(firstId, secondId);

        // Should keep the new id for other 30 mins

        addTimeInMillis(systemTimeProvider, TimeUnit.MINUTES.toMillis(10));

        assertEquals(secondId, sessionManager.getSessionId());
    }

    private void addTimeInMillis(SystemTimeProvider systemTimeProvider, long extraTimeInMillis) {
        doReturn(systemTimeProvider.getCurrentTimeMillis() + extraTimeInMillis).when(systemTimeProvider).getCurrentTimeMillis();
    }

    private SystemTimeProvider getSystemTimeProvider(long initialCurrentTimeMillis) {
        SystemTimeProvider mock = mock(SystemTimeProvider.class);
        doReturn(initialCurrentTimeMillis).when(mock).getCurrentTimeMillis();
        return mock;
    }

    @Test
    public void whenSessionIdIsRequested_timeoutShouldResetToKeepTheSameIdForOther30mins() {
        int initialTime = 1_000_000;
        SystemTimeProvider systemTimeProvider = getSystemTimeProvider(initialTime);
        SessionManager sessionManager = getSessionManager(systemTimeProvider);

        String firstId = sessionManager.getSessionId();

        // Forward just before 30 mins.
        addTimeInMillis(systemTimeProvider, TimeUnit.MINUTES.toMillis(29));

        assertEquals(firstId, sessionManager.getSessionId());

        // Timeout should reset after the previous call to request the id.

        // Forward another 29 mins:
        addTimeInMillis(systemTimeProvider, TimeUnit.MINUTES.toMillis(29));

        assertEquals(firstId, sessionManager.getSessionId());
    }

    @Test
    public void whenThereIsASessionIdStored_andItHasNotExpired_reuseIt() {
        String existingSessionId = "abcd";
        long existingExpireTimeMillis = 1_000_000_000;
        long initialSystemTime = existingExpireTimeMillis - 1;
        SystemTimeProvider timeProvider = getSystemTimeProvider(initialSystemTime);
        doReturn(existingSessionId).when(preferencesService).retrieveString(KEY_SESSION_ID);
        doReturn(existingExpireTimeMillis).when(preferencesService).retrieveLong(eq(KEY_SESSION_ID_EXPIRATION_TIME), anyLong());

        SessionManager sessionManager = getSessionManager(timeProvider);

        assertEquals(existingSessionId, sessionManager.getSessionId());
    }

    @Test
    public void whenThereIsASessionIdStored_andItsExpired_createNewSessionId() {
        String existingSessionId = "12345";
        long existingExpireTimeMillis = 1_000_000_000;
        long initialSystemTime = existingExpireTimeMillis + 1;
        SystemTimeProvider timeProvider = getSystemTimeProvider(initialSystemTime);
        doReturn(existingSessionId).when(preferencesService).retrieveString(KEY_SESSION_ID);
        doReturn(existingExpireTimeMillis).when(preferencesService).retrieveLong(eq(KEY_SESSION_ID_EXPIRATION_TIME), anyLong());

        SessionManager sessionManager = getSessionManager(timeProvider);

        assertNotEquals(existingSessionId, sessionManager.getSessionId());
    }

    @Test
    public void whenANewSessionIdIsGenerated_storeItInPreferences() {
        long initialSystemTime = 1_000_000_000;
        SystemTimeProvider timeProvider = getSystemTimeProvider(initialSystemTime);

        SessionManager sessionManager = getSessionManager(timeProvider);

        String generatedSessionId = sessionManager.getSessionId();
        verify(preferencesService).store(KEY_SESSION_ID, generatedSessionId);
        verify(preferencesService).store(KEY_SESSION_ID_EXPIRATION_TIME, initialSystemTime + TimeUnit.MINUTES.toMillis(30));
    }

    private SessionManager getSessionManager() {
        return getSessionManager(SystemTimeProvider.get());
    }

    private SessionManager getSessionManager(SystemTimeProvider systemTimeProvider) {
        SessionManager sessionManager = new SessionManager(systemTimeProvider, LazyProvider.of(this), sessionIdGenerator);
        sessionManager.initialize();
        return sessionManager;
    }

    @Override
    public PreferencesService get() {
        return preferencesService;
    }
}
