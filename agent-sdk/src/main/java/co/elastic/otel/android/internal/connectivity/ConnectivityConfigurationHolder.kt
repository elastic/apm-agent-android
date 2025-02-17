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
package co.elastic.otel.android.internal.connectivity

import co.elastic.otel.android.connectivity.ConnectivityConfiguration
import co.elastic.otel.android.provider.Provider
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

internal open class ConnectivityConfigurationHolder(initialValue: ConnectivityConfiguration) :
    Provider<ConnectivityConfiguration> {
    private val value = AtomicReference(initialValue)
    private val listeners = CopyOnWriteArrayList<Listener>()
    private val setLock = Any()

    override fun get(): ConnectivityConfiguration {
        return value.get()
    }

    fun set(value: ConnectivityConfiguration) = synchronized(setLock) {
        if (this.value.get() != value) {
            this.value.set(value)
            notifyChange()
        }
    }

    private fun notifyChange() {
        listeners.forEach {
            it.onConnectivityConfigurationChange()
        }
    }

    fun addListener(listener: Listener) {
        listeners.addIfAbsent(listener)
    }

    interface Listener {
        fun onConnectivityConfigurationChange()
    }
}