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
package co.elastic.apm.android.sdk.tools

import co.elastic.apm.android.sdk.internal.services.Service
import co.elastic.apm.android.sdk.internal.services.ServiceManager
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService

class PreferencesCachedStringProvider(
    private val key: String,
    private val provider: StringProvider
) : CacheHandler<String>, StringProvider {
    private val preferences: PreferencesService by lazy {
        ServiceManager.get().getService(Service.Names.PREFERENCES)
    }

    override fun retrieve(): String? {
        return preferences.retrieveString(key)
    }

    override fun clear() {
        preferences.store(key, null)
    }

    override fun store(value: String) {
        preferences.store(key, value)
    }

    override fun get(): String {
        val retrieved = retrieve()
        if (retrieved != null) {
            return retrieved
        }

        val computed = provider.get()

        store(computed)

        return computed
    }
}