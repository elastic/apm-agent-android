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
package co.elastic.apm.android.sdk.traces.tools;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

public final class ElasticTracer implements Tracer {
    private final Tracer tracer;

    public ElasticTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public static ElasticTracer create(@NonNull String name, @Nullable String version) {
        if (version == null) {
            return new ElasticTracer(GlobalOpenTelemetry.getTracer(name));
        } else {
            return new ElasticTracer(GlobalOpenTelemetry.getTracer(name, version));
        }
    }

    public static ElasticTracer create(String name) {
        return create(name, null);
    }

    public static ElasticTracer okhttp() {
        ApmMetadataService service = ServiceManager.get().getService(Service.Names.METADATA);
        return create("OkHttp", service.getOkHttpVersion());
    }

    public static ElasticTracer androidActivity() {
        return create("Android Activity");
    }

    public static ElasticTracer androidFragment() {
        return create("Android Fragment");
    }

    public static ElasticTracer coroutine() {
        return create("Kotlin Coroutine");
    }

    @Override
    public SpanBuilder spanBuilder(@NonNull String spanName) {
        return tracer.spanBuilder(spanName);
    }
}
