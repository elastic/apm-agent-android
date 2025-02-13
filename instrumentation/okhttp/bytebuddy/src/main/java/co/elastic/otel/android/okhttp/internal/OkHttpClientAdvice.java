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
package co.elastic.otel.android.okhttp.internal;

import net.bytebuddy.asm.Advice;

import co.elastic.otel.android.okhttp.internal.plugin.OkHttp3Singletons;
import okhttp3.OkHttpClient;

public class OkHttpClientAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(0) OkHttpClient.Builder builder) {
        if (!builder.interceptors().contains(OkHttp3Singletons.CALLBACK_CONTEXT_INTERCEPTOR)) {
            builder.interceptors().add(0, OkHttp3Singletons.CALLBACK_CONTEXT_INTERCEPTOR);
            builder.interceptors().add(1, OkHttp3Singletons.RESEND_COUNT_CONTEXT_INTERCEPTOR);
            builder.interceptors().add(2, OkHttp3Singletons.CONNECTION_ERROR_INTERCEPTOR);
        }
        if (!builder.networkInterceptors().contains(OkHttp3Singletons.TRACING_INTERCEPTOR)) {
            builder.addNetworkInterceptor(OkHttp3Singletons.TRACING_INTERCEPTOR);
        }
    }
}
