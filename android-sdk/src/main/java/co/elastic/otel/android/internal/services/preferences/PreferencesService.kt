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
package co.elastic.otel.android.internal.services.preferences

import android.content.Context
import android.content.SharedPreferences
import co.elastic.otel.android.BuildConfig
import co.elastic.otel.android.internal.services.Service

internal class PreferencesService(context: Context) : Service {
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            BuildConfig.LIBRARY_PACKAGE_NAME + ".prefs",
            Context.MODE_PRIVATE
        )
    }

    fun store(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun store(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun store(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    fun retrieveString(key: String): String? {
        return preferences.getString(key, null)
    }

    fun retrieveInt(key: String, defaultValue: Int): Int {
        return preferences.getInt(key, defaultValue)
    }

    fun retrieveLong(key: String, defaultValue: Long): Long {
        return preferences.getLong(key, defaultValue)
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
}