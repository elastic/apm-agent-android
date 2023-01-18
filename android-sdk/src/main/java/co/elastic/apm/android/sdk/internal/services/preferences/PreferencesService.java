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
package co.elastic.apm.android.sdk.internal.services.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import co.elastic.apm.android.sdk.BuildConfig;
import co.elastic.apm.android.sdk.internal.services.Service;

public class PreferencesService implements Service {
    private final SharedPreferences preferences;

    public PreferencesService(Context context) {
        preferences = context.getSharedPreferences(BuildConfig.LIBRARY_PACKAGE_NAME + ".prefs", Context.MODE_PRIVATE);
    }

    @Override
    public void start() {
        // No op
    }

    @Override
    public void stop() {
        // No op
    }

    public void store(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    @Nullable
    public String retrieve(String key) {
        return preferences.getString(key, null);
    }

    @Override
    public String name() {
        return Names.PREFERENCES;
    }
}
