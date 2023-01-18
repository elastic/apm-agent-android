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
package co.elastic.apm.android.sdk.connectivity.custom;

import co.elastic.apm.android.sdk.connectivity.base.DefaultProcessingConnectivity;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class CustomExporterConnectivity extends DefaultProcessingConnectivity {
    private final SpanExporter spanExporter;
    private final LogRecordExporter logExporter;
    private final MetricExporter metricExporter;

    public CustomExporterConnectivity(SpanExporter exporter, LogRecordExporter logExporter, MetricExporter metricExporter) {
        this.spanExporter = exporter;
        this.logExporter = logExporter;
        this.metricExporter = metricExporter;
    }

    @Override
    protected SpanExporter provideSpanExporter() {
        return spanExporter;
    }

    @Override
    protected LogRecordExporter provideLogExporter() {
        return logExporter;
    }

    @Override
    protected MetricExporter provideMetricExporter() {
        return metricExporter;
    }
}