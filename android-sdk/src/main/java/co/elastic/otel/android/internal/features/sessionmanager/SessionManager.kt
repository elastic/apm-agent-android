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
package co.elastic.otel.android.internal.features.sessionmanager

import androidx.annotation.GuardedBy
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.utilities.cache.CacheHandler
import co.elastic.otel.android.internal.utilities.cache.PreferencesLongCacheHandler
import co.elastic.otel.android.internal.utilities.cache.PreferencesStringCacheHandler
import co.elastic.otel.android.session.Session
import co.elastic.otel.android.session.SessionProvider
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

class SessionManager private constructor(
    private val cachedSessionId: CacheHandler<String>,
    private val cachedSessionIdExpireTime: CacheHandler<Long>,
    private val cachedSessionIdIdleTime: CacheHandler<Long>,
    private val idGenerator: SessionIdGenerator,
    private val systemTimeProvider: SystemTimeProvider
) : SessionProvider {
    @GuardedBy("sessionLock")
    private var session: InnerSession? = null
    private val sessionLock = Any()
    private val listeners = CopyOnWriteArrayList<Listener>()

    companion object {
        private val IDLE_TIME_LIMIT = TimeUnit.MINUTES.toMillis(30)
        private val MAX_SESSION_TIME = TimeUnit.HOURS.toMillis(4)

        internal fun create(
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
                    "session_id_idle_time",
                    serviceManager.getPreferencesService()
                ),
                idGenerator,
                systemTimeProvider
            )
        }
    }

    internal fun initialize() {
        cachedSessionId.retrieve()?.let { sessionId ->
            synchronized(sessionLock) {
                session = InnerSession(
                    sessionId,
                    cachedSessionIdExpireTime.retrieve() ?: 0,
                    cachedSessionIdIdleTime.retrieve() ?: 0
                )
            }
        }
    }

    override fun getSession(): Session? = synchronized(sessionLock) {
        var currentSession = session
        if (currentSession == null || !isValid(currentSession)) {
            currentSession = generateSession()
            setSession(currentSession)
        }
        if (currentSession != null) {
            currentSession.idleTime = getNextIdleTime()
            cachedSessionIdIdleTime.store(currentSession.idleTime)
            return Session.create(currentSession.id)
        }
        return null
    }

    internal fun clearSession() {
        setSession(null)
    }

    internal fun addListener(listener: Listener) {
        listeners.addIfAbsent(listener)
    }

    private fun setSession(value: InnerSession?) = synchronized(sessionLock) {
        if (session?.id == value?.id) {
            return
        }
        session = value
        session?.let {
            cachedSessionId.store(it.id)
            cachedSessionIdExpireTime.store(it.expireTime)
            cachedSessionIdIdleTime.store(it.idleTime)
        } ?: {
            cachedSessionId.clear()
            cachedSessionIdExpireTime.clear()
            cachedSessionIdIdleTime.clear()
        }
        notifySessionChange()
    }

    private fun notifySessionChange() {
        listeners.forEach {
            it.onSessionChanged()
        }
    }

    private fun generateSession(): InnerSession? {
        val generatedId = idGenerator.generate()
        if (generatedId != null) {
            return InnerSession(generatedId, getSessionIdExpireTime(), getNextIdleTime())
        }
        return null
    }

    private fun isValid(session: InnerSession): Boolean {
        val currentTime = systemTimeProvider.getCurrentTimeMillis()
        return currentTime < session.idleTime && currentTime < session.expireTime
    }

    private fun getSessionIdExpireTime(): Long {
        return systemTimeProvider.getCurrentTimeMillis() + MAX_SESSION_TIME
    }

    private fun getNextIdleTime(): Long {
        return systemTimeProvider.getCurrentTimeMillis() + IDLE_TIME_LIMIT
    }

    interface Listener {
        fun onSessionChanged()
    }

    private data class InnerSession(val id: String, val expireTime: Long, var idleTime: Long)
}