/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package co.elastic.otel.android.okhttp.internal.callback;

import net.bytebuddy.asm.Advice;

import co.elastic.otel.android.okhttp.internal.plugin.OkHttpCallbackAdviceHelper;
import co.elastic.otel.android.okhttp.internal.plugin.TracingCallback;
import io.opentelemetry.context.Context;
import okhttp3.Call;
import okhttp3.Callback;

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
