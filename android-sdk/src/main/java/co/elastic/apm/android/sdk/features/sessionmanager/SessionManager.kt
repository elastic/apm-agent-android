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
package co.elastic.apm.android.sdk.features.sessionmanager

import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.session.Session
import co.elastic.apm.android.sdk.session.SessionProvider
import co.elastic.apm.android.sdk.tools.CacheHandler
import co.elastic.apm.android.sdk.tools.PreferencesLongCacheHandler
import co.elastic.apm.android.sdk.tools.PreferencesStringCacheHandler
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class SessionManager private constructor(
    private val cachedSessionId: CacheHandler<String>,
    private val cachedSessionIdExpireTime: CacheHandler<Long>,
    private val cachedSessionIdNextTimeForUpdate: CacheHandler<Long>,
    private val idGenerator: SessionIdGenerator,
    private val systemTimeProvider: SystemTimeProvider
) : SessionProvider {
    private val sessionId = AtomicReference<String?>(cachedSessionId.retrieve())
    private val nextTimeForUpdate = AtomicLong(cachedSessionIdNextTimeForUpdate.retrieve() ?: 0)
    private val sessionIdExpireTime = AtomicLong(cachedSessionIdExpireTime.retrieve() ?: 0)

    companion object {
        private val IDLE_TIME_LIMIT = TimeUnit.MINUTES.toMillis(30)
        private val MAX_SESSION_TIME = TimeUnit.HOURS.toMillis(4)

        fun create(
            serviceManager: ServiceManager,
            idGenerator: SessionIdGenerator,
            systemTimeProvider: SystemTimeProvider
        ): SessionManager {
            return SessionManager(
                PreferencesStringCacheHandler(
                    "session_id",
                    serviceManager.getPreferencesService()
                ),
                PreferencesLongCacheHandler(
                    "session_id_expire_time",
                    serviceManager.getPreferencesService()
                ),
                PreferencesLongCacheHandler(
                    "session_id_next_time_for_update",
                    serviceManager.getPreferencesService()
                ),
                idGenerator,
                systemTimeProvider
            )
        }
    }

    override fun getSession(): Session? {
        verifyIdleTime()
        return when (val existingId = sessionId.get()) {
            null -> generateAndStoreId()?.let { Session.create(it) }
            else -> Session.create(existingId)
        }.also { updateNextTimeForUpdate() }
    }

    internal fun clearSession() {
        sessionId.set(null)
        nextTimeForUpdate.set(0)
        sessionIdExpireTime.set(0)
        cachedSessionId.clear()
        cachedSessionIdNextTimeForUpdate.clear()
        cachedSessionIdExpireTime.clear()
    }

    private fun verifyIdleTime() {
        val currentTime = systemTimeProvider.getCurrentTimeMillis()
        if (currentTime >= nextTimeForUpdate.get() || currentTime >= sessionIdExpireTime.get()) {
            clearSession()
        }
    }

    private fun setSessionId(id: String) {
        sessionId.set(id)
        cachedSessionId.store(id)
    }

    private fun generateAndStoreId(): String? {
        val generatedId = idGenerator.generate()
        if (generatedId != null) {
            setSessionIdExpireTime()
            setSessionId(generatedId)
            return generatedId
        } else {
            clearSession()
            return null
        }
    }

    private fun setSessionIdExpireTime() {
        val expireTime = systemTimeProvider.getCurrentTimeMillis() + MAX_SESSION_TIME
        sessionIdExpireTime.set(expireTime)
        cachedSessionIdExpireTime.store(expireTime)
    }

    private fun updateNextTimeForUpdate() {
        val nextTime = systemTimeProvider.getCurrentTimeMillis() + IDLE_TIME_LIMIT
        nextTimeForUpdate.set(nextTime)
        cachedSessionIdNextTimeForUpdate.store(nextTime)
    }
}