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
package co.elastic.apm.android.sdk.internal.time.ntp;

import android.content.Context;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.util.Date;

public class TrueTimeWrapper {
    private final Context context;

    public TrueTimeWrapper(Context context) {
        this.context = context;
    }

    public void initialize() throws IOException {
        TrueTime.build().initialize();
    }

    public void withSharedPreferencesCache() {
        TrueTime.build().withSharedPreferencesCache(context);
    }

    public void withRootDispersionMax(float rootDispersionMax) {
        TrueTime.build().withRootDispersionMax(rootDispersionMax);
    }

    public boolean isInitialized() {
        return TrueTime.isInitialized();
    }

    public Date now() throws IllegalStateException {
        return TrueTime.now();
    }

    public void clearCachedInfo() {
        TrueTime.clearCachedInfo();
    }
}
