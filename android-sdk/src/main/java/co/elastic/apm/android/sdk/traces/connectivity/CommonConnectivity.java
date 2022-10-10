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
package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.traces.connectivity.base.BatchProcessingConnectivity;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class CommonConnectivity extends BatchProcessingConnectivity {
    private final String endpoint;
    private String token;

    CommonConnectivity(String endpoint) {
        this.endpoint = endpoint;
    }

    public CommonConnectivity withAuthToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    protected SpanExporter provideSpanExporter() {
        OtlpGrpcSpanExporterBuilder exporterBuilder = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint);
        if (token != null) {
            exporterBuilder.addHeader("Authorization", "Bearer " + token);
        }
        return exporterBuilder.build();
    }
}