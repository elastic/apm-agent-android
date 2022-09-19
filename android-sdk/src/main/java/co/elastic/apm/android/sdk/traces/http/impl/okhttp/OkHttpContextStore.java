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
package co.elastic.apm.android.sdk.traces.http.impl.okhttp;

import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap;

import io.opentelemetry.context.Context;
import okhttp3.Request;

public class OkHttpContextStore {

    private final WeakConcurrentMap<Request, Context> spanContexts = new WeakConcurrentMap.WithInlinedExpunction<>();

    public void put(Request request, Context spanContext) {
        spanContexts.put(request, spanContext);
    }

    public void remove(Request request) {
        spanContexts.remove(request);
    }

    public Context get(Request request) {
        return spanContexts.getIfPresent(request);
    }
}
