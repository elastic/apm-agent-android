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
package co.elastic.apm.android.sdk.connectivity;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.connectivity.custom.CustomConnectivity;
import co.elastic.apm.android.sdk.connectivity.custom.CustomExporterConnectivity;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.providers.LazyProvider;
import co.elastic.apm.android.sdk.providers.Provider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Provides an Open Telemetry {@link SpanProcessor} object which handles the APM backend connectivity.
 */
public interface Connectivity {

    /**
     * This function provides a convenient way of creating a common {@link Connectivity} object that
     * requires and endpoint URL, and optionally a server secret token for Bearer authentication purposes.
     * <p>
     * An example of using this function to create a connectivity with a secret token:
     *
     * <pre>
     *  {@code Connectivity myConnectivity = Connectivity.create("https://my.server.url").withSecretToken("my_bearer_token");}
     * </pre>
     *
     * @param endpoint - The APM server URL.
     */
    static CommonConnectivity create(String endpoint) {
        return new CommonConnectivity(endpoint);
    }

    /**
     * This function provides a convenient way to create a {@link Connectivity} with a custom Open Telemetry's {@link SpanExporter} and {@link MetricExporter}
     * which {@link SpanProcessor} will be a {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor}.
     */
    static Connectivity custom(SpanExporter spanExporter, MetricExporter metricExporter) {
        return new CustomExporterConnectivity(spanExporter, metricExporter);
    }

    /**
     * This function provides a convenient way of creating a fully customized {@link Connectivity} object by providing
     * a {@link SpanProcessor} and a {@link MetricReader} directly. This option might come in handy to avoid having to create a custom {@link Connectivity} implementation.
     */
    static Connectivity custom(SpanProcessor spanProcessor, MetricReader metricReader) {
        return new CustomConnectivity(spanProcessor, metricReader);
    }

    /**
     * This function provides a {@link Connectivity} instance that uses the server parameters defined at compile time.
     */
    static Provider<Connectivity> getDefault() {
        return LazyProvider.of(() -> {
            ApmMetadataService service = ElasticApmAgent.get().getService(Service.Names.METADATA);
            CommonConnectivity connectivity = Connectivity.create(service.getServerUrl());
            String secretToken = service.getSecretToken();
            if (secretToken != null) {
                connectivity.withSecretToken(secretToken);
            }
            return connectivity;
        });
    }

    SpanProcessor getSpanProcessor();

    MetricReader getMetricReader();
}
