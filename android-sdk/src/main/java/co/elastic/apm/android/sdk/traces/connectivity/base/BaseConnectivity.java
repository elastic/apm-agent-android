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
package co.elastic.apm.android.sdk.traces.connectivity.base;

import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import co.elastic.apm.android.sdk.traces.otel.exporter.ElasticSpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public abstract class BaseConnectivity implements Connectivity {

    @Override
    public SpanProcessor getSpanProcessor() {
        SpanExporter original = provideSpanExporter();
        ElasticSpanExporter exporter;
        if (original instanceof ElasticSpanExporter) {
            exporter = (ElasticSpanExporter) original;
        } else {
            exporter = new ElasticSpanExporter(original);
        }
        return provideSpanProcessor(exporter);
    }

    protected abstract SpanProcessor provideSpanProcessor(SpanExporter exporter);

    protected abstract SpanExporter provideSpanExporter();
}
