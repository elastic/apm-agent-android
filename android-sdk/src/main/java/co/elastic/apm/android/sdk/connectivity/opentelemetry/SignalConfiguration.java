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

import co.elastic.apm.android.sdk.connectivity.opentelemetry.custom.CustomSignalConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.custom.CustomSignalExporterConfiguration;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Provides an OpenTelemetry objects which handle the APM backend connectivity for all signals.
 */
public interface SignalConfiguration {
    /**
     * This function provides a convenient way of creating a common {@link SignalConfiguration} object that
     * requires and endpoint URL, and optionally a server secret token for Bearer authentication purposes.
     * <p>
     * An example of using this function to create a configuration with a secret token:
     *
     * <pre>
     *  {@code SignalConfiguration myConfiguration = SignalConfiguration.create("https://my.server.url").withSecretToken("my_bearer_token");}
     * </pre>
     */
    static DefaultSignalConfiguration create() {
        return new DefaultSignalConfiguration();
    }

    /**
     * This function provides a convenient way to create a {@link SignalConfiguration} with a custom OpenTelemetry's {@link SpanExporter}, {@link LogRecordExporter} and {@link MetricExporter}
     * which processors will be {@link BatchSpanProcessor}, {@link BatchLogRecordProcessor} and {@link PeriodicMetricReader} respectively.
     */
    static SignalConfiguration custom(SpanExporter spanExporter, LogRecordExporter logExporter, MetricExporter metricExporter) {
        return new CustomSignalExporterConfiguration(spanExporter, logExporter, metricExporter);
    }

    /**
     * This function provides a convenient way of creating a fully customized {@link SignalConfiguration} object by providing
     * a {@link SpanProcessor}, {@link LogRecordProcessor} and a {@link MetricReader} directly. This option might come in handy to avoid having to create a custom {@link SignalConfiguration} implementation.
     */
    static SignalConfiguration custom(SpanProcessor spanProcessor, LogRecordProcessor logProcessor, MetricReader metricReader) {
        return new CustomSignalConfiguration(spanProcessor, logProcessor, metricReader);
    }

    /**
     * This function provides a {@link SignalConfiguration} instance that uses the server parameters defined at compile time.
     */
    static SignalConfiguration getDefault() {
        return SignalConfiguration.create();
    }

    SpanProcessor getSpanProcessor();

    LogRecordProcessor getLogProcessor();

    MetricReader getMetricReader();
}
