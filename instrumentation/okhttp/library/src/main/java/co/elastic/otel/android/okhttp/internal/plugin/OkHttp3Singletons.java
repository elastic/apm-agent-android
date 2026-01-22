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
package co.elastic.otel.android.okhttp.internal.plugin;

import static io.opentelemetry.instrumentation.api.internal.HttpConstants.KNOWN_METHODS;

import co.elastic.otel.android.okhttp.internal.delegate.InterceptorDelegator;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.incubator.semconv.net.PeerServiceAttributesExtractor;
import io.opentelemetry.instrumentation.api.incubator.semconv.net.PeerServiceResolver;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.semconv.http.HttpClientRequestResendCount;
import io.opentelemetry.instrumentation.api.semconv.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.okhttp.v3_0.internal.ConnectionErrorSpanInterceptor;
import io.opentelemetry.instrumentation.okhttp.v3_0.internal.OkHttpAttributesGetter;
import io.opentelemetry.instrumentation.okhttp.v3_0.internal.OkHttpClientInstrumenterBuilderFactory;
import io.opentelemetry.instrumentation.okhttp.v3_0.internal.TracingInterceptor;
import java.util.Collections;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class OkHttp3Singletons {
  public static InterceptorDelegator CONNECTION_ERROR_INTERCEPTOR = InterceptorDelegator.create();
  public static InterceptorDelegator TRACING_INTERCEPTOR = InterceptorDelegator.create();

  public static void configure(OpenTelemetry openTelemetry) {
    Instrumenter<Interceptor.Chain, Response> instrumenter =
        OkHttpClientInstrumenterBuilderFactory.create(openTelemetry)
            .setKnownMethods(KNOWN_METHODS)
            .setSpanNameExtractorCustomizer(
                x -> HttpSpanNameExtractor.builder(OkHttpAttributesGetter.INSTANCE).build())
            .addAttributesExtractor(
                PeerServiceAttributesExtractor.create(
                    OkHttpAttributesGetter.INSTANCE,
                    PeerServiceResolver.create(Collections.emptyMap())))
            .setEmitExperimentalHttpClientTelemetry(false)
            .build();
    CONNECTION_ERROR_INTERCEPTOR.setDelegate(new ConnectionErrorSpanInterceptor(instrumenter));
    TRACING_INTERCEPTOR.setDelegate(
        new TracingInterceptor(instrumenter, openTelemetry.getPropagators()));
  }

  public static void reset() {
    CONNECTION_ERROR_INTERCEPTOR.reset();
    TRACING_INTERCEPTOR.reset();
  }

  public static final Interceptor CALLBACK_CONTEXT_INTERCEPTOR =
      chain -> {
        Request request = chain.request();
        Context context =
            OkHttpCallbackAdviceHelper.tryRecoverPropagatedContextFromCallback(request);
        if (context != null) {
          try (Scope ignored = context.makeCurrent()) {
            return chain.proceed(request);
          }
        }

        return chain.proceed(request);
      };

  public static final Interceptor RESEND_COUNT_CONTEXT_INTERCEPTOR =
      chain -> {
        try (Scope ignored =
            HttpClientRequestResendCount.initialize(Context.current()).makeCurrent()) {
          return chain.proceed(chain.request());
        }
      };

  private OkHttp3Singletons() {}
}
