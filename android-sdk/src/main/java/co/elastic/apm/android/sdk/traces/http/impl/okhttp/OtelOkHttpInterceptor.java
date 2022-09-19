/* 
Licensed to Elasticsearch B.V. under one or more contributor
license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright
ownership. Elasticsearch B.V. licenses this file to you under
the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License. 
*/
package co.elastic.apm.android.sdk.traces.http.impl.okhttp;

import java.io.IOException;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OtelOkHttpInterceptor implements Interceptor {

    private final OkHttpContextStore contextStore;

    public OtelOkHttpInterceptor(OkHttpContextStore contextStore) {
        this.contextStore = contextStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Context context = contextStore.get(request);
        if (context != null) {
            Request.Builder newRequestBuilder = request.newBuilder();
            TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
            propagator.inject(context, newRequestBuilder, new OtelOkhttpTextMapSetter());

            return chain.proceed(newRequestBuilder.build());
        }

        return chain.proceed(request);
    }

    static class OtelOkhttpTextMapSetter implements TextMapSetter<Request.Builder> {

        @Override
        public void set(Request.Builder carrier, String key, String value) {
            if (carrier == null) {
                return;
            }
            carrier.addHeader(key, value);
        }
    }
}