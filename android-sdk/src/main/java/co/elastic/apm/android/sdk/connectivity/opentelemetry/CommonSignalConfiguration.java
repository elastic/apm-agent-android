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

import co.elastic.apm.android.sdk.connectivity.opentelemetry.base.DefaultSignalConfiguration;
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

public class CommonSignalConfiguration extends DefaultSignalConfiguration {
    private final static String AUTHORIZATION_HEADER_NAME = "Authorization";
    private final static String BEARER_TOKEN_FORMAT = "Bearer %s";
    private final String endpoint;
    private String token;

    CommonSignalConfiguration(String endpoint) {
        this.endpoint = endpoint;
    }

    public CommonSignalConfiguration withSecretToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    protected SpanExporter provideSpanExporter() {
        OtlpGrpcSpanExporterBuilder exporterBuilder = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint);
        if (token != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getBearerToken());
        }
        return exporterBuilder.build();
    }

    @Override
    protected LogRecordExporter provideLogExporter() {
        OtlpGrpcLogRecordExporterBuilder exporterBuilder = OtlpGrpcLogRecordExporter.builder().setEndpoint(endpoint);
        if (token != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getBearerToken());
        }
        return exporterBuilder.build();
    }

    @Override
    protected MetricExporter provideMetricExporter() {
        OtlpGrpcMetricExporterBuilder exporterBuilder = OtlpGrpcMetricExporter.builder()
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .setEndpoint(endpoint);
        if (token != null) {
            exporterBuilder.addHeader(AUTHORIZATION_HEADER_NAME, getBearerToken());
        }
        return exporterBuilder.build();
    }

    @NonNull
    private String getBearerToken() {
        return String.format(BEARER_TOKEN_FORMAT, token);
    }
}