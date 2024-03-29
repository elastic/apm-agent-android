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
package co.elastic.apm.android.sdk.connectivity.opentelemetry.base;

import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.ExporterVisitor;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.VisitableExporters;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public abstract class BaseSignalConfiguration implements SignalConfiguration, VisitableExporters {
    private ExporterVisitor exporterVisitor;

    @Override
    public SpanProcessor getSpanProcessor() {
        return provideSpanProcessor(visitExporter(provideSpanExporter()));
    }

    @Override
    public LogRecordProcessor getLogProcessor() {
        return provideLogProcessor(visitExporter(provideLogExporter()));
    }

    @Override
    public MetricReader getMetricReader() {
        return provideMetricReader(visitExporter(provideMetricExporter()));
    }

    protected abstract SpanProcessor provideSpanProcessor(SpanExporter exporter);

    protected abstract SpanExporter provideSpanExporter();

    protected abstract LogRecordProcessor provideLogProcessor(LogRecordExporter exporter);

    protected abstract LogRecordExporter provideLogExporter();

    protected abstract MetricReader provideMetricReader(MetricExporter exporter);

    protected abstract MetricExporter provideMetricExporter();

    protected <T> T visitExporter(T exporter) {
        if (exporterVisitor != null) {
            return exporterVisitor.visitExporter(exporter);
        }
        return exporter;
    }

    @Override
    public void setExporterVisitor(ExporterVisitor visitor) {
        this.exporterVisitor = visitor;
    }
}
