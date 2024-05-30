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
package co.elastic.apm.android.sdk.connectivity.opentelemetry;

import androidx.annotation.NonNull;

import co.elastic.apm.android.sdk.connectivity.opentelemetry.base.DefaultSignalProcessorConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.sdk.internal.utilities.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public final class DefaultSignalConfiguration extends DefaultSignalProcessorConfiguration {
    private final static String AUTHORIZATION_HEADER_NAME = "Authorization";
    private final Provider<ConnectivityConfiguration> connectivity;

    public DefaultSignalConfiguration() {
        this(LazyProvider.of(() -> Configurations.get(ConnectivityConfiguration.class)));
    }

    DefaultSignalConfiguration(Provider<ConnectivityConfiguration> connectivity) {
        this.connectivity = connectivity;
    }

    @Override
    protected SpanExporter provideSpanExporter() {
        switch (getConnectivity().getExportProtocol()) {
            case GRPC:
                return getOtlpGrpcSpanExporter();
            case HTTP:
                return getOtlpHttpSpanExporter();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    protected LogRecordExporter provideLogExporter() {
        switch (getConnectivity().getExportProtocol()) {
            case GRPC:
                return getOtlpGrpcLogRecordExporter();
            case HTTP:
                return getOtlpHttpLogRecordExporter();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    protected MetricExporter provideMetricExporter() {
        switch (getConnectivity().getExportProtocol()) {
            case GRPC:
                return getOtlpGrpcMetricExporter();
            case HTTP:
                return getOtlpHttpMetricExporter();
            default:
                throw new IllegalArgumentException();
        }
    }

    @NonNull
    private OtlpGrpcSpanExporter getOtlpGrpcSpanExporter() {
        OtlpGrpcSpanExporterBuilder exporterBuilder = OtlpGrpcSpanExporter.builder().setEndpoint(getConnectivity().getEndpoint());
        if (getConnectivity().getAuthConfiguration() != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getAuthorizationHeaderValue());
        }
        return exporterBuilder.build();
    }

    @NonNull
    private OtlpGrpcLogRecordExporter getOtlpGrpcLogRecordExporter() {
        OtlpGrpcLogRecordExporterBuilder exporterBuilder = OtlpGrpcLogRecordExporter.builder().setEndpoint(getConnectivity().getEndpoint());
        if (getConnectivity().getAuthConfiguration() != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getAuthorizationHeaderValue());
        }
        return exporterBuilder.build();
    }

    @NonNull
    private OtlpGrpcMetricExporter getOtlpGrpcMetricExporter() {
        OtlpGrpcMetricExporterBuilder exporterBuilder = OtlpGrpcMetricExporter.builder()
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .setEndpoint(getConnectivity().getEndpoint());
        if (getConnectivity().getAuthConfiguration() != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getAuthorizationHeaderValue());
        }
        return exporterBuilder.build();
    }

    @NonNull
    private OtlpHttpSpanExporter getOtlpHttpSpanExporter() {
        OtlpHttpSpanExporterBuilder exporterBuilder = OtlpHttpSpanExporter.builder().setEndpoint(getHttpEndpoint("traces"));
        if (getConnectivity().getAuthConfiguration() != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getAuthorizationHeaderValue());
        }
        return exporterBuilder.build();
    }

    @NonNull
    private OtlpHttpLogRecordExporter getOtlpHttpLogRecordExporter() {
        OtlpHttpLogRecordExporterBuilder exporterBuilder = OtlpHttpLogRecordExporter.builder().setEndpoint(getHttpEndpoint("logs"));
        if (getConnectivity().getAuthConfiguration() != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getAuthorizationHeaderValue());
        }
        return exporterBuilder.build();
    }

    @NonNull
    private OtlpHttpMetricExporter getOtlpHttpMetricExporter() {
        OtlpHttpMetricExporterBuilder exporterBuilder = OtlpHttpMetricExporter.builder()
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .setEndpoint(getHttpEndpoint("metrics"));
        if (getConnectivity().getAuthConfiguration() != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getAuthorizationHeaderValue());
        }
        return exporterBuilder.build();
    }

    private ConnectivityConfiguration getConnectivity() {
        return connectivity.get();
    }

    @NonNull
    private String getAuthorizationHeaderValue() {
        return getConnectivity().getAuthConfiguration().asAuthorizationHeaderValue();
    }

    private String getHttpEndpoint(String signalId) {
        return String.format("%s/v1/%s", getConnectivity().getEndpoint(), signalId);
    }
}