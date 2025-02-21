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
package co.elastic.otel.android.okhttp.internal.callback;

import net.bytebuddy.asm.Advice;

import co.elastic.otel.android.okhttp.internal.plugin.OkHttpCallbackAdviceHelper;
import co.elastic.otel.android.okhttp.internal.plugin.TracingCallback;
import io.opentelemetry.context.Context;
import okhttp3.Call;
import okhttp3.Callback;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class OkHttpCallbackAdvice {

    @Advice.OnMethodEnter
    public static void enter(
            @Advice.This Call call,
            @Advice.Argument(value = 0, readOnly = false) Callback callback) {
        if (OkHttpCallbackAdviceHelper.propagateContext(call)) {
            callback = new TracingCallback(callback, Context.current());
        }
    }
}
